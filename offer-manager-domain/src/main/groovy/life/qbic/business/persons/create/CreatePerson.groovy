package life.qbic.business.persons.create

import life.qbic.business.persons.update.UpdatePerson
import life.qbic.business.persons.update.UpdateCustomerOutput
import life.qbic.business.logging.Logger
import life.qbic.business.logging.Logging
import life.qbic.business.exceptions.DatabaseQueryException
import life.qbic.datamodel.dtos.business.Customer
import life.qbic.datamodel.dtos.general.Person

/**
 * This use case creates a customer in the system
 *
 * Information on persons such as affiliation and names can be added to the user database.
 *
 * @since: 1.0.0
 */
class CreatePerson implements CreatePersonInput, UpdateCustomerOutput {

  private CreatePersonDataSource dataSource
  private CreatePersonOutput output
  private UpdatePerson updatePerson

  private final Logging log = Logger.getLogger(CreatePerson.class)


    CreatePerson(CreatePersonOutput output, CreatePersonDataSource dataSource){
    this.output = output
    this.dataSource = dataSource
    this.updatePerson = new UpdatePerson(this,dataSource)
  }

  @Override
  void createPerson(Person person) {
    try {
      dataSource.addPerson(person)
      try {
        output.personCreated(person)
      } catch (Exception ignored) {
        log.error(ignored.stackTrace.toString())
      }
    } catch(DatabaseQueryException databaseQueryException){
      output.failNotification(databaseQueryException.message)
    } catch(Exception unexpected) {
      println "-------------------------"
      println "Unexpected Exception ...."
      println unexpected.message
      println unexpected.stackTrace.join("\n")
      output.failNotification("Could not create new customer")
    }
  }

  @Override
  void updatePerson(Person oldPerson, Person newPerson) {
    //todo at which point do we distinguish between person and customer? we need to have a concreate instance at one point
    // and person is abstract
    int customerId = dataSource.findCustomer((Customer) oldPerson).get()
    updatePerson.updatePerson(customerId,newPerson)
  }

  @Override
  void personUpdated(Person person) {
    output.personCreated(person)
  }

  @Override
  void failNotification(String notification) {
    output.failNotification(notification)
  }
}