package life.qbic.portal.offermanager.dataresources.projects

import groovy.util.logging.Log4j2
import life.qbic.datamodel.dtos.general.Person
import life.qbic.business.projects.spaces.CreateProjectSpaceDataSource
import life.qbic.business.projects.spaces.ProjectSpaceExistsException
import life.qbic.business.projects.create.CreateProjectDataSource
import life.qbic.business.projects.create.ProjectExistsException

import life.qbic.datamodel.dtos.business.*
import life.qbic.datamodel.dtos.projectmanagement.*

import life.qbic.business.exceptions.DatabaseQueryException

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

import life.qbic.openbis.openbisclient.OpenBisClient

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.CreateProjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;

/**
 * Provides operations on QBiC project data
 *
 * This class implements the data sources of the different use cases and is responsible for transferring data to the project/customer db and openBIS
 *
 * @since: 1.0.0
 *
 */
@Log4j2
class ProjectMainConnector implements CreateProjectDataSource, CreateProjectSpaceDataSource {

  /**
   * A connection to the project (and customer) database used to create queries.
   */
  private final ProjectDbConnector projectDbConnector
  private final OpenBisClient openbisClient

  /**
   * Constructor for a ProjectMainConnector
   * @param projectDbConnector a connector enabling interaction with the project database
   * @param openbisClient an openBIS client API object
   * 
   * @see Connection
   */
  ProjectMainConnector(ProjectDbConnector projectDbConnector, OpenBisClient openbisClient) {
    this.projectDbConnector = projectDbConnector
    this.openbisClient = openbisClient
  }

  @Override
  void createProjectSpace(ProjectSpace projectSpace) throws ProjectSpaceExistsException, DatabaseQueryException {
    String spaceName = projectSpace.getName()
    if(openbisClient.spaceExists(spaceName)) {
      throw new ProjectSpaceExistsException("Project space "+spaceName+" could not be created, as it exists in openBIS already!")
    }
    try {
      //we don't provide a description in our data model for now, but it's optional anyway
      createOpenbisSpace(spaceName, "")

    } catch (Exception e) {
      log.error(e.message)
      log.error(e.stackTrace.join("\n"))
      throw new DatabaseQueryException("Could not create project space.")
    }
  }

  private void createOpenbisSpace(String spaceName, String description) {
    IApplicationServerApi api = openbisClient.getV3()
    
    SpaceCreation space = new SpaceCreation()
    space.setCode(spaceName)

    space.setDescription(description);

    IOperation operation = new CreateSpacesOperation(space)
    api.handleOperations(operation)
  }

  private void createOpenbisProject(ProjectSpace space, ProjectCode projectCode, String description) {
    IApplicationServerApi api = openbisClient.getV3()

    ProjectCreation project = new ProjectCreation();
    project.setCode(projectCode.toString());
    project.setSpaceId(new SpacePermId(space.toString()));
    project.setDescription(description);

    IOperation operation = new CreateProjectsOperation(project);
    api.handleOperations(operation);
  }

  @Override
  Project createProject(ProjectApplication projectApplication) throws ProjectExistsException, DatabaseQueryException {
    //collect infos needed for openBIS
    ProjectSpace space = projectApplication.getProjectSpace()
    ProjectCode projectCode = projectApplication.getProjectCode()
    String description = projectApplication.getProjectObjective()

    ProjectIdentifier projectIdentifier = new ProjectIdentifier(space, projectCode)

    //if the space does not exist, an error shall be thrown
    if (!openbisClient.spaceExists(space.toString())) {
      throw new DatabaseQueryException("Could not create project because of non-existent space: "+space.toString())
    }
    if (openbisClient.projectExists(space.toString(), projectCode.toString())) {
      throw new ProjectExistsException("Project "+projectIdentifier.toString()+" could not be created, as it exists in openBIS already!")
    }
    try {
      createOpenbisProject(space, projectCode, description)
    } catch (Exception e) {
      log.error(e.message)
      log.error(e.stackTrace.join("\n"))
      throw new DatabaseQueryException("Could not create project.")
    }

    return projectDbConnector.addProjectAndConnectPersonsInUserDB(projectIdentifier, projectApplication)
  }
  
}
