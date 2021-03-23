package life.qbic.portal.offermanager.components.offer.overview.projectcreation

import com.vaadin.data.provider.DataProvider
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.ui.Layout
import groovy.beans.Bindable
import life.qbic.datamodel.dtos.business.Offer
import life.qbic.datamodel.dtos.projectmanagement.ProjectCode
import life.qbic.datamodel.dtos.projectmanagement.ProjectSpace
import life.qbic.portal.offermanager.dataresources.projects.ProjectResourceService
import life.qbic.portal.offermanager.dataresources.projects.ProjectSpaceResourceService

/**
 * <h1>Holds the create project state information and logic</h1>
 *
 * <p>View model for the {@link CreateProjectView}.</p>
 *
 * @since 1.0.0
 */
class CreateProjectViewModel {

    /**
     * Flag that indicates when a project has been created
     */
    @Bindable Boolean projectCreated

    /**
     * Saves the layout from which the create project component
     * has been initiated.
     * This view is set to visible again, if the user decides to navigate back.
     */
    Optional<Layout> startedFromView

    /**
     * The selected offer that holds the information
     * for the projected to be created by the user.
     */
    @Bindable Optional<Offer> selectedOffer

    @Bindable Boolean createProjectEnabled

    enum SPACE_SELECTION {
        NEW_SPACE, EXISTING_SPACE
    }
    DataProvider spaceSelectionDataProvider

    @Bindable String resultingSpaceName

    @Bindable String desiredSpaceName

    @Bindable SPACE_SELECTION spaceSelection

    DataProvider availableSpaces

    @Bindable String desiredProjectCode

    @Bindable String resultingProjectCode

    List<ProjectCode> existingProjects

    @Bindable String projectCodeValidationResult

    @Bindable Boolean codeIsValid

    private final ProjectSpaceResourceService projectSpaceResourceService

    private final ProjectResourceService projectResourceService

    CreateProjectViewModel(ProjectSpaceResourceService projectSpaceResourceService,
                           ProjectResourceService projectResourceService) {
        this.projectSpaceResourceService = projectSpaceResourceService
        this.projectResourceService = projectResourceService

        spaceSelectionDataProvider = new ListDataProvider<>([SPACE_SELECTION.NEW_SPACE,
                                                             SPACE_SELECTION.EXISTING_SPACE])

        availableSpaces = new ListDataProvider(projectSpaceResourceService.iterator().toList())
        existingProjects = projectResourceService.iterator().collect {it.projectCode}
        startedFromView = Optional.empty()
        initFields()
        setupListeners()
    }

    private void setupListeners() {
        this.addPropertyChangeListener("desiredSpaceName", {
            ProjectSpace space = new ProjectSpace(desiredSpaceName)
            this.setResultingSpaceName(space.name)
        })
        this.addPropertyChangeListener("desiredProjectCode", {
            validateProjectCode()
            evaluateProjectCreation()
        })
        this.addPropertyChangeListener("resultingSpaceName",  {
            evaluateProjectCreation()
        })
        this.addPropertyChangeListener("projectCreated", {
            resetModel()
        })
    }

    private void initFields() {
        resultingSpaceName = ""
        desiredSpaceName = ""
        desiredProjectCode = ""
        resultingProjectCode = ""
        projectCodeValidationResult = ""
        codeIsValid = false
        createProjectEnabled = false
        projectCreated = false
        selectedOffer = Optional.empty()
    }

    private void resetModel() {
        initFields()
    }

    private void validateProjectCode() {
        try {
            ProjectCode code = new ProjectCode(desiredProjectCode.toUpperCase())
            this.setResultingProjectCode(code.code)
            if (code in existingProjects) {
                this.setCodeIsValid(false)
                this.setProjectCodeValidationResult("Project with code $resultingProjectCode " +
                        "already exists.")
            } else {
                this.setCodeIsValid(true)
                this.setProjectCodeValidationResult("Project code is valid.")
            }
        } catch (IllegalArgumentException e) {
            this.setCodeIsValid(false)
            this.setProjectCodeValidationResult("${desiredProjectCode} is not a valid QBiC " +
                    "project code.")
        }
    }

    private void evaluateProjectCreation() {
        this.setCreateProjectEnabled(codeIsValid && resultingSpaceName)
    }
}
