package life.qbic.portal.portlet.customers.create

import life.qbic.business.customers.create.CreateCustomer
import life.qbic.business.customers.create.CreateCustomerDataSource
import life.qbic.business.customers.create.CreateCustomerOutput
import life.qbic.datamodel.dtos.business.AcademicTitle
import life.qbic.datamodel.dtos.business.Customer
import spock.lang.Specification

/**
 * This test class tests for the use case functionality
 *
 * Given information about a customer a user wants to create the customer in the system
 *
 * @since: 1.0.0
 * @author: Tobias Koch
 */
class CreateCustomerSpec extends Specification {
    CreateCustomerOutput output
    CreateCustomerDataSource dataSource


    def setup() {
        output = Mock()
        dataSource = Mock()
    }

    def "given full information add the customer using a mocked data source"(){
        given: "A new create customer use case instance"
        CreateCustomer useCase = new CreateCustomer(output, dataSource)

        when: "The use case method is called"
        useCase.createCustomer(customer)

        then: "The customer is added using the data source"
        1 * dataSource.addCustomer(customer)

        where:
        customer = new Customer.Builder("Test", "user", "test").title(AcademicTitle.NONE).build()
    }

    def "datasource throwing an exception leads to fail notification on output"() {
        given: "a data source that throws an exception"
        dataSource.addCustomer(_ as Customer) >> { throw new Exception("Something went wrong.") }
        CreateCustomer useCase = new CreateCustomer(output, dataSource)

        when: "the use case is executed"
        useCase.createCustomer(customer)

        then: "the output receives a failure notification"
        1 * output.failNotification(_ as String)
        0 * output.successNotification(_ as String)

        where:
        customer = new Customer.Builder("Test", "user", "test").title(AcademicTitle.NONE).build()
    }

}