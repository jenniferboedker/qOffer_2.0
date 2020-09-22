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

    private ViewModel viewModel
    private Presenter presenter

    private CustomerDbGateway customerDbGateway
    private CreateCustomer createCustomer
    private CreateCustomerController createCustomerController

    private PortletView portletView
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
        // The ORDER in which the methods below are called MUST NOT CHANGE
        setupDbConnections()
        setupViewModels()
        setupPresenters()
        setupUseCaseInteractors()
        setupControllers()
        setupViews()
    }

    protected PortletView getPortletView() {
        return this.portletView
    }

    private void setupViewModels() {
        // setup view models
        try {
            this.viewModel = new ViewModel()
            viewModel.affiliations.addAll(customerDbGateway.allAffiliations)
        } catch (Exception e) {
            log.error("Unexpected excpetion during ${ViewModel.getSimpleName()} view model setup.", e)
            throw e
        }
    }

    private void setupDbConnections() {
        try {
            DatabaseSession.create()
            CustomerDatabaseQueries queries = new CustomerDatabaseQueries(DatabaseSession.INSTANCE)
            customerDbGateway = new CustomerDbConnector(queries)
        } catch (Exception e) {
            log.error("Unexpected exception during customer database connection.", e)
            throw e
        }
    }

    private void setupPresenters() {
        try {
            this.presenter = new Presenter(this.viewModel)
        } catch (Exception e) {
            log.error("Unexpected exception during ${Presenter.getSimpleName()} setup." , e)
            throw e
        }
    }

    private void setupUseCaseInteractors() {
        this.createCustomer = new CreateCustomer(presenter, customerDbGateway)
    }

    private void setupControllers() {
        try {
            this.createCustomerController = new CreateCustomerController(this.createCustomer)
        } catch (Exception e) {
            log.error("Unexpected exception during ${CreateCustomerController.getSimpleName()} setup.", e)
            throw e
        }
    }

    private void setupViews() {
        CreateCustomerView createCustomerView
        try {
            createCustomerView = new CreateCustomerView(this.createCustomerController, this.viewModel)
        } catch (Exception e) {
            log.error("Could not create ${CreateCustomerView.getSimpleName()} view.", e)
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
    }


}
