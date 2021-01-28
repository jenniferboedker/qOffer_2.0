package life.qbic.portal.offermanager

import groovy.util.logging.Log4j2
import life.qbic.datamodel.dtos.business.AcademicTitle
import life.qbic.datamodel.dtos.business.AffiliationCategory
import life.qbic.business.customers.affiliation.create.CreateAffiliation
import life.qbic.business.customers.affiliation.list.ListAffiliations
import life.qbic.business.customers.create.CreateCustomer
import life.qbic.business.customers.search.SearchCustomer
import life.qbic.business.offers.create.CreateOffer
import life.qbic.portal.offermanager.dataresources.customers.AffiliationResourcesService
import life.qbic.portal.offermanager.dataresources.customers.CustomerDbConnector
import life.qbic.portal.offermanager.dataresources.customers.PersonResourcesService
import life.qbic.portal.offermanager.dataresources.database.DatabaseSession
import life.qbic.portal.offermanager.dataresources.offers.OfferDbConnector
import life.qbic.portal.offermanager.dataresources.offers.OfferResourcesService
import life.qbic.portal.offermanager.dataresources.products.ProductsDbConnector
import life.qbic.portal.offermanager.dataresources.products.ProductsResourcesService
import life.qbic.portal.offermanager.dataresources.offers.OfferUpdateService
import life.qbic.portal.offermanager.dataresources.database.OverviewService
import life.qbic.portal.offermanager.components.affiliation.create.CreateAffiliationController
import life.qbic.portal.offermanager.components.person.create.CreatePersonController
import life.qbic.portal.offermanager.components.offer.create.CreateOfferController
import life.qbic.portal.offermanager.web.controllers.ListAffiliationsController
import life.qbic.portal.offermanager.web.controllers.SearchCustomerController
import life.qbic.portal.offermanager.components.affiliation.create.CreateAffiliationPresenter
import life.qbic.portal.offermanager.components.person.create.CreatePersonPresenter
import life.qbic.portal.offermanager.components.offer.create.CreateOfferPresenter
import life.qbic.portal.offermanager.web.presenters.ListAffiliationsPresenter
import life.qbic.portal.offermanager.components.AppPresenter
import life.qbic.portal.offermanager.web.presenters.SearchCustomerPresenter
import life.qbic.portal.offermanager.components.affiliation.create.CreateAffiliationViewModel
import life.qbic.portal.offermanager.components.person.create.CreatePersonViewModel
import life.qbic.portal.offermanager.components.offer.create.CreateOfferViewModel
import life.qbic.portal.offermanager.components.offer.overview.OfferOverviewModel
import life.qbic.portal.offermanager.web.viewmodel.SearchCustomerViewModel
import life.qbic.portal.offermanager.components.offer.update.UpdateOfferViewModel
import life.qbic.portal.offermanager.components.AppViewModel
import life.qbic.portal.offermanager.components.affiliation.create.CreateAffiliationView
import life.qbic.portal.offermanager.components.person.create.CreatePersonView
import life.qbic.portal.offermanager.components.offer.create.CreateOfferView
import life.qbic.portal.offermanager.components.offer.overview.OfferOverviewView
import life.qbic.portal.offermanager.components.AppView
import life.qbic.portal.offermanager.web.views.SearchCustomerView
import life.qbic.portal.utils.ConfigurationManager
import life.qbic.portal.utils.ConfigurationManagerFactory

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

    private AppViewModel viewModel
    private CreatePersonViewModel createCustomerViewModel
    private CreateAffiliationViewModel createAffiliationViewModel
    private SearchCustomerViewModel searchCustomerViewModel
    private CreateOfferViewModel createOfferViewModel
    private CreateOfferViewModel updateOfferViewModel
    private OfferOverviewModel offerOverviewModel

    private AppPresenter presenter
    private CreatePersonPresenter createCustomerPresenter
    private CreateAffiliationPresenter createAffiliationPresenter
    private ListAffiliationsPresenter listAffiliationsPresenter
    private SearchCustomerPresenter searchCustomerPresenter
    private CreateOfferPresenter createOfferPresenter
    private CreateOfferPresenter updateOfferPresenter

    private CustomerDbConnector customerDbConnector
    private OfferDbConnector offerDbConnector
    private ProductsDbConnector productsDbConnector

    private CreateCustomer createCustomer
    private CreateAffiliation createAffiliation
    private ListAffiliations listAffiliations
    private SearchCustomer searchCustomer
    private CreateOffer createOffer
    private CreateOffer updateOffer

    private CreatePersonController createCustomerController
    private CreateAffiliationController createAffiliationController
    private SearchCustomerController searchCustomerController
    private ListAffiliationsController listAffiliationsController
    private CreateOfferController createOfferController
    private CreateOfferController updateOfferController

    private CreatePersonView createCustomerView
    private CreatePersonView createCustomerViewNewOffer
    private CreateAffiliationView createAffiliationView
    private AppView portletView
    private ConfigurationManager configurationManager

    private OverviewService overviewService
    private OfferUpdateService offerUpdateService
    private PersonResourcesService customerService
    private AffiliationResourcesService affiliationService
    private OfferResourcesService offerService
    private ProductsResourcesService productsResourcesService

    /**
     * Public constructor.
     *
     * This constructor creates a dependency manager with all the instances of required classes.
     * It ensures that the {@link #portletView} field is set.
     */
    DependencyManager() {
        configurationManager = ConfigurationManagerFactory.getInstance()
        initializeDependencies()
    }

    private void initializeDependencies() {
        // The ORDER in which the methods below are called MUST NOT CHANGE
        setupDbConnections()
        setupServices()
        setupViewModels()
        setupPresenters()
        setupUseCaseInteractors()
        setupControllers()
        setupViews()
    }

    protected AppView getPortletView() {
        return this.portletView
    }

    private void setupDbConnections() {
        try {

            String user = Objects.requireNonNull(configurationManager.getMysqlUser(), "Mysql user missing.")
            String password = Objects.requireNonNull(configurationManager.getMysqlPass(), "Mysql password missing.")
            String host = Objects.requireNonNull(configurationManager.getMysqlHost(), "Mysql host missing.")
            String port = Objects.requireNonNull(configurationManager.getMysqlPort(), "Mysql port missing.")
            String sqlDatabase = Objects.requireNonNull(configurationManager.getMysqlDB(), "Mysql database name missing.")

            DatabaseSession.init(user, password, host, port, sqlDatabase)
            customerDbConnector = new CustomerDbConnector(DatabaseSession.getInstance())
            productsDbConnector = new ProductsDbConnector(DatabaseSession.getInstance())
            offerDbConnector = new OfferDbConnector(DatabaseSession.getInstance(), customerDbConnector, productsDbConnector)

        } catch (Exception e) {
            log.error("Unexpected exception during customer database connection.", e)
            throw e
        }
    }

    private void setupServices() {
        this.offerService = new OfferResourcesService()
        this.overviewService = new OverviewService(offerDbConnector, offerService)
        this.offerUpdateService = new OfferUpdateService()
        this.customerService = new PersonResourcesService(customerDbConnector)
        this.productsResourcesService = new ProductsResourcesService(productsDbConnector)
        this.affiliationService = new AffiliationResourcesService(customerDbConnector)
    }

    private void setupViewModels() {
        // setup view models
        try {
            this.viewModel = new AppViewModel(affiliationService)
        } catch (Exception e) {
            log.error("Unexpected excpetion during ${AppViewModel.getSimpleName()} view model setup.", e)
            throw e
        }

        try {
            this.createCustomerViewModel = new CreatePersonViewModel(customerService)
            createCustomerViewModel.academicTitles.addAll(AcademicTitle.values().collect {it.value})

        } catch (Exception e) {
            log.error("Unexpected excpetion during ${CreatePersonViewModel.getSimpleName()} view model setup.", e)
            throw e
        }

        try {
            this.createAffiliationViewModel = new CreateAffiliationViewModel(affiliationService)
            createAffiliationViewModel.affiliationCategories.addAll(AffiliationCategory.values().collect{it.value})
        } catch (Exception e) {
            log.error("Unexpected excpetion during ${CreateAffiliationViewModel.getSimpleName()} view model setup.", e)
            throw e
        }
        try {
            this.searchCustomerViewModel = new SearchCustomerViewModel()

        } catch (Exception e) {
            log.error("Unexpected excpetion during ${SearchCustomerViewModel.getSimpleName()} view model setup.", e)
            throw e
        }

        try {
            this.createOfferViewModel = new CreateOfferViewModel(customerService, productsResourcesService)
            //todo add affiliations, customers and project managers to the model
        } catch (Exception e) {
            log.error("Unexpected exception during ${CreateOfferViewModel.getSimpleName()} view model setup.", e)
            throw e
        }

        try {
            this.updateOfferViewModel = new UpdateOfferViewModel(customerService, productsResourcesService,
                    offerUpdateService)
        } catch (Exception e) {
            log.error("Unexpected excpetion during ${CreateOfferViewModel.getSimpleName()} view model setup.", e)
            throw e
        }

        try {
            this.offerOverviewModel = new OfferOverviewModel(overviewService, offerDbConnector,
                    viewModel)
        } catch (Exception e) {
            log.error("Unexpected excpetion during ${OfferOverviewModel.getSimpleName()} view model setup.", e)
        }
    }

    private void setupPresenters() {
        try {
            this.presenter = new AppPresenter(this.viewModel)
        } catch (Exception e) {
            log.error("Unexpected exception during ${AppPresenter.getSimpleName()} setup." , e)
            throw e
        }

        try {
            this.createCustomerPresenter = new CreatePersonPresenter(this.viewModel, this.createCustomerViewModel)
        } catch (Exception e) {
            log.error("Unexpected exception during ${CreatePersonPresenter.getSimpleName()} setup." , e)
            throw e
        }

        try {
            this.createAffiliationPresenter = new CreateAffiliationPresenter(this.viewModel, this.createAffiliationViewModel)
        } catch (Exception e) {
            log.error("Unexpected exception during ${CreateAffiliationPresenter.getSimpleName()} setup." , e)
            throw e
        }

        try {
            this.listAffiliationsPresenter = new ListAffiliationsPresenter(this.viewModel)
        } catch (Exception e) {
            log.error("Unexpected exception during ${ListAffiliationsPresenter.getSimpleName()} setup", e)
        }

        try {
            this.searchCustomerPresenter = new SearchCustomerPresenter(this.viewModel, this.searchCustomerViewModel)
        } catch (Exception e) {
            log.error("Unexpected exception during ${SearchCustomerPresenter.getSimpleName()} setup", e)
        }

        try {
            this.createOfferPresenter = new CreateOfferPresenter(this.viewModel,
                    this.createOfferViewModel, this.offerService)
        } catch (Exception e) {
            log.error("Unexpected exception during ${CreateOfferViewModel.getSimpleName()} setup", e)
        }

        try {
            this.updateOfferPresenter = new CreateOfferPresenter(this.viewModel,
                    this.updateOfferViewModel, this.offerService)
        } catch (Exception e) {
            log.error("Unexpected exception during ${CreateOfferViewModel.getSimpleName()} setup", e)
        }
    }

    private void setupUseCaseInteractors() {
        this.createCustomer = new CreateCustomer(createCustomerPresenter, customerDbConnector)
        this.createAffiliation = new CreateAffiliation(createAffiliationPresenter, customerDbConnector)
        this.listAffiliations = new ListAffiliations(listAffiliationsPresenter, customerDbConnector)
        this.createOffer = new CreateOffer(offerDbConnector, createOfferPresenter)
        this.updateOffer = new CreateOffer(offerDbConnector, updateOfferPresenter)
        this.searchCustomer = new SearchCustomer(searchCustomerPresenter, customerDbConnector)
    }

    private void setupControllers() {
        try {
            this.createCustomerController = new CreatePersonController(this.createCustomer)
        } catch (Exception e) {
            log.error("Unexpected exception during ${CreatePersonController.getSimpleName()} setup.", e)
            throw e
        }
        try {
            this.createAffiliationController = new CreateAffiliationController(this.createAffiliation)
        } catch (Exception e) {
            log.error("Unexpected exception during ${CreatePersonController.getSimpleName()} setup.", e)
            throw e
        }
        try {
            this.searchCustomerController = new SearchCustomerController(this.searchCustomer)
        } catch (Exception e) {
            log.error("Unexpected exception during ${SearchCustomerController.getSimpleName()} setup.", e)
            throw e
        }
        try {
            this.listAffiliationsController = new ListAffiliationsController(this.listAffiliations)
        } catch (Exception e) {
            log.error("Unexpected exception during ${ListAffiliationsController.getSimpleName()} setup", e)
        }
        try {
            this.createOfferController = new CreateOfferController(this.createOffer,this.createOffer)
        } catch (Exception e) {
            log.error("Unexpected exception during ${CreateOfferController.getSimpleName()} setup", e)
        }
        try {
            this.updateOfferController = new CreateOfferController(this.updateOffer,this.updateOffer)
        } catch (Exception e) {
            log.error("Unexpected exception during ${CreateOfferController.getSimpleName()} setup", e)
        }
    }

    private void setupViews() {

        try {
            this.createCustomerView = new CreatePersonView(this.createCustomerController, this.viewModel, this.createCustomerViewModel)
            listAffiliationsController.listAffiliations()
        } catch (Exception e) {
            log.error("Could not create ${CreatePersonView.getSimpleName()} view.", e)
            throw e
        }

        try {
            this.createCustomerViewNewOffer = new CreatePersonView(this.createCustomerController, this.viewModel, this.createCustomerViewModel)
            listAffiliationsController.listAffiliations()
        } catch (Exception e) {
            log.error("Could not create ${CreatePersonView.getSimpleName()} view.", e)
            throw e
        }

        try {
            this.createAffiliationView = new CreateAffiliationView(this.viewModel, this.createAffiliationViewModel, this.createAffiliationController)
        } catch (Exception e) {
            log.error("Could not create ${CreateAffiliationView.getSimpleName()} view.", e)
            throw e
        }

        SearchCustomerView searchCustomerView

        try {
            searchCustomerView = new SearchCustomerView(this.searchCustomerController, this.viewModel, this.searchCustomerViewModel)
        } catch (Exception e) {
            log.error("Could not create ${SearchCustomerView.getSimpleName()} view.", e)
            throw e
        }

        CreateOfferView createOfferView
        try {
            createOfferView = new CreateOfferView(
                    this.viewModel,
                    this.createOfferViewModel,
                    this.createOfferController,
                    this.createCustomerViewNewOffer,
                    this.createAffiliationView,
                    this.offerService)
        } catch (Exception e) {
            log.error("Could not create ${CreateOfferView.getSimpleName()} view.", e)
            throw e
        }

        CreateOfferView updateOfferView
        try {
            updateOfferView = new CreateOfferView(
                    this.viewModel,
                    this.updateOfferViewModel,
                    this.updateOfferController,
                    this.createCustomerView,
                    this.createAffiliationView,
                    this.offerService)
        } catch (Exception e) {
            log.error("Could not create ${CreateOfferView.getSimpleName()} view.", e)
            throw e
        }

        OfferOverviewView overviewView
        try {
            overviewView = new OfferOverviewView(offerOverviewModel, offerUpdateService)
        } catch (Exception e) {
            log.error("Could not create ${OfferOverviewView.getSimpleName()} view.", e)
            throw e
        }

        AppView portletView
        try {
            CreatePersonView createCustomerView2 = new CreatePersonView(createCustomerController, this
                    .viewModel, createCustomerViewModel)
            CreateAffiliationView createAffiliationView2 = new CreateAffiliationView(this.viewModel,
                    createAffiliationViewModel, createAffiliationController)
            portletView = new AppView(this.viewModel, createCustomerView2,
                    createAffiliationView2,
                    searchCustomerView,
                    createOfferView,
                    overviewView,
                    updateOfferView
            )
            this.portletView = portletView
        } catch (Exception e) {
            log.error("Could not create ${AppView.getSimpleName()} view.", e)
            throw e
        }
    }


}