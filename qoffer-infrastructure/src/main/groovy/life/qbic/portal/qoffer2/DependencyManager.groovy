package life.qbic.portal.qoffer2

import groovy.util.logging.Log4j2
import life.qbic.portal.portlet.customers.CustomerDbGateway
import life.qbic.portal.portlet.customers.create.CreateCustomer
import life.qbic.portal.qoffer2.customers.CustomerDatabaseQueries
import life.qbic.portal.qoffer2.customers.CustomerDbConnector
import life.qbic.portal.qoffer2.database.DatabaseSession
import life.qbic.portal.qoffer2.web.PortletView
import life.qbic.portal.qoffer2.web.Presenter
import life.qbic.portal.qoffer2.web.ViewModel
import life.qbic.portal.qoffer2.web.controllers.CreateCustomerController
import life.qbic.portal.qoffer2.web.views.CreateCustomerView

/**
 * Class that manages all the dependency injections and class instance creations
 *
 * This class has access to all classes that are instantiated at setup. It is responsible to construct
 * and provide every instance with it's dependencies injected. The class should only be accessed once upon
 * portlet creation and shall not be used later on in the control flow.
 *
 * @since: 1.0.0
 */

@Log4j2
class DependencyManager {

    private PortletView portletView
    private ViewModel viewModel
    private Presenter presenter

    private CustomerDbGateway customerDbGateway
    private CreateCustomerController createCustomerController
    private CreateCustomer createCustomer

    /**
     * Public constructor.
     *
     * This constructor creates a dependency manager with all the instances of required classes.
     * It ensures that the {@link #portletView} field is set.
     */
    DependencyManager() {
        initializeDependencies()
    }

    private void initializeDependencies() {

        // setup view models
        try {
            this.viewModel = new ViewModel()
        } catch (Exception e) {
            log.error("Unexpected excpetion during ${ViewModel.getSimpleName()} view model setup.", e)
            throw e
        }

        CreateCustomerView createCustomerView
        try {
            createCustomerView = new CreateCustomerView(this.viewModel)
        } catch (Exception e) {
            log.error("Could not create ${createCustomerView.toString()} view.", e)
            throw e
        }

        PortletView portletView
        try {
            portletView = new PortletView(this.viewModel, createCustomerView)
            this.portletView = portletView
        } catch (Exception e) {
            log.error("Could not create ${PortletView.getSimpleName()} view.", e)
            throw e
        }

        // setup presenter
        presenter = new Presenter(viewModel)

        //setup database
        setupCustomerDbConnection()

        // setup CreateCustomer use case
        createCustomer = new CreateCustomer(presenter, customerDbGateway)
        //add affiliations to the view model
        viewModel.addAffiliationList(customerDbGateway.allAffiliations)
        println viewModel.affiliations

        try {
            this.createCustomerController = new CreateCustomerController(createCustomerView,
                    createCustomer)
        } catch (Exception e) {
            log.error("Unexpected exception during ${createCustomerController.getSimpleName()} setup.", e)
            throw e
        }
    }

    protected PortletView getPortletView() {
        return this.portletView
    }

    /**
     * Sets up the database session
     * @return
     */
    private setupCustomerDbConnection(){
        DatabaseSession.create()
        CustomerDatabaseQueries queries = new CustomerDatabaseQueries(DatabaseSession.INSTANCE)
        customerDbGateway = new CustomerDbConnector(queries)
    }


}
