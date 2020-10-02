package life.qbic.portal.qoffer2.web.views

import com.vaadin.data.Binder
import com.vaadin.data.ValidationResult
import com.vaadin.data.Validator
import com.vaadin.data.ValueContext
import com.vaadin.icons.VaadinIcons
import com.vaadin.server.UserError
import com.vaadin.shared.ui.ContentMode
import com.vaadin.ui.*
import life.qbic.portal.qoffer2.web.controllers.CreateAffiliationController
import life.qbic.portal.qoffer2.web.viewmodel.CreateAffiliationViewModel
import life.qbic.portal.qoffer2.web.viewmodel.ViewModel

/**
 * This class generates a Layout in which the user
 * can input the necessary information for the creation of a new affiliation
 *
 * CreateAffiliationView will be integrated into the qOffer 2.0 Portlet and provides an User Interface
 * with the intention of enabling a user the creation of a new Affiliation in the QBiC Database
 *
 * @since: 1.0.0
 */
class CreateAffiliationView extends VerticalLayout {
    final private ViewModel sharedViewModel
    final private CreateAffiliationViewModel createAffiliationViewModel
    final private CreateAffiliationController controller

    private TextField organisationField
    private TextField addressAdditionField
    private TextField streetField
    private TextField postalCodeField
    private TextField cityField
    private TextField countryField
    private ComboBox<String> affiliationCategoryField

    private Button submitButton

    CreateAffiliationView(ViewModel sharedViewModel, CreateAffiliationViewModel createAffiliationViewModel, CreateAffiliationController controller) {
        super()
        this.sharedViewModel = sharedViewModel
        this.createAffiliationViewModel = createAffiliationViewModel
        this.controller = controller
        initLayout()
        bindViewModel()
        setupFieldValidators()
        registerListeners()
    }

    private void initLayout() {
        this.organisationField = new TextField("Organisation Name")
        organisationField.setPlaceholder("name of the organisation")
        organisationField.setDescription("Please enter the name of the organisation e.g. Universität Tübingen.")

        this.addressAdditionField = new TextField("Department")
        addressAdditionField.setPlaceholder("optional department name")
        addressAdditionField.setDescription("In case the affiliation differs from the organisation you can further specify that here.")

        this.streetField = new TextField("Street")
        streetField.setPlaceholder("street name and street number ")
        this.postalCodeField = new TextField("Postal Code")
        postalCodeField.setPlaceholder("customer postal code")
        this.cityField = new TextField("City")
        cityField.setPlaceholder("name of the city")
        this.countryField = new TextField("Country")
        countryField.setPlaceholder("name of the country")
        this.affiliationCategoryField = new ComboBox<>("Affiliation Category")
        affiliationCategoryField.setPlaceholder("the affiliation category")
        affiliationCategoryField.setDescription("""We define three major business affiliation categories here
        - internal: 
            An affiliation we consider as within the University of Tübingen or University Hospital of Tübingen
        - external academic: 
            An outside affiliation but an academic institution (public research institutions)
        - external:
            An outside affiliation but not academic (i.e. private sector, companies, etc)""".stripIndent(), ContentMode.PREFORMATTED)
        this.submitButton = new Button("Create Affiliation")
        submitButton.enabled = allValuesValid()

        organisationField.setRequiredIndicatorVisible(true)
        addressAdditionField.setRequiredIndicatorVisible(false)
        streetField.setRequiredIndicatorVisible(true)
        postalCodeField.setRequiredIndicatorVisible(true)
        cityField.setRequiredIndicatorVisible(true)
        countryField.setRequiredIndicatorVisible(true)
        affiliationCategoryField.setRequiredIndicatorVisible(true)

        submitButton.setIcon(VaadinIcons.OFFICE)

        HorizontalLayout row1 = new HorizontalLayout(organisationField, addressAdditionField)
        row1.setSizeFull()
        HorizontalLayout row2 = new HorizontalLayout(streetField)
        row2.setSizeFull()
        HorizontalLayout row3 = new HorizontalLayout(postalCodeField, cityField)
        row3.setSizeFull()
        row3.setExpandRatio(postalCodeField, 1)
        row3.setExpandRatio(cityField,3) // leads to it being 3/4 of the width
        HorizontalLayout row4 = new HorizontalLayout(countryField)
        row4.setSizeFull()
        HorizontalLayout row5 = new HorizontalLayout(affiliationCategoryField, submitButton)
        row5.setComponentAlignment(affiliationCategoryField, Alignment.BOTTOM_LEFT)
        row5.setComponentAlignment(submitButton, Alignment.BOTTOM_RIGHT)
        row5.setSizeFull()

        organisationField.setSizeFull()
        addressAdditionField.setSizeFull()
        streetField.setSizeFull()
        postalCodeField.setSizeFull()
        cityField.setSizeFull()
        countryField.setSizeFull()
        affiliationCategoryField.setSizeFull()

        this.addComponents(row1, row2, row3, row4, row5)
        this.setSpacing(true)
    }

    private void bindViewModel() {
        Binder<CreateAffiliationViewModel> binder = new Binder<>()

        // by binding the fields to the view model, the model is updated when the user input changed
        binder.setBean(this.createAffiliationViewModel)

        binder.forField(this.addressAdditionField).bind({ it.addressAddition }, { it, updatedValue -> it.setAddressAddition(updatedValue) })
        binder.forField(this.affiliationCategoryField).bind({ it.affiliationCategory }, { it, updatedValue -> it.setAffiliationCategory(updatedValue) })
        binder.forField(this.cityField).bind({ it.city }, { it, updatedValue -> it.setCity(updatedValue) })
        binder.forField(this.countryField).bind({ it.country }, { it, updatedValue -> it.setCountry(updatedValue) })
        binder.forField(this.organisationField).bind({ it.organisation }, { it, updatedValue -> it.setOrganisation(updatedValue) })
        binder.forField(this.postalCodeField).bind({ it.postalCode }, { it, updatedValue -> it.setPostalCode(updatedValue) })
        binder.forField(this.streetField).bind({ it.street }, { it, updatedValue -> it.setStreet(updatedValue) })

        /*
        Here we setup a listener to the viewModel that hold displayed information.
        The listener is needed since Vaadin bindings only work one-way

        Please NOTE: we cannot use the binder.readBean(binder.getBean) refresh here since it would
        overwrite all validators attached to the fields. We furthermore cannot use the
        BinderBuilder#withValidator method since this would prevent the form from showing invalid
        information that is stored within the viewModel. We want the view to reflect the view model
        at all times!
         */
        createAffiliationViewModel.addPropertyChangeListener({
            switch (it.propertyName) {
                case "addressAddition":
                    String newValue = it.newValue as String
                    addressAdditionField.value = newValue ?: addressAdditionField.emptyValue
                    break
                case "affiliationCategory":
                    String newValue = it.newValue as String
                    affiliationCategoryField.selectedItem = newValue ?: affiliationCategoryField.emptyValue
                    break
                case "city":
                    String newValue = it.newValue as String
                    cityField.value = newValue ?: cityField.emptyValue
                    break
                case "country":
                    String newValue = it.newValue as String
                    countryField.value = newValue ?: countryField.emptyValue
                    break
                case "organisation":
                    String newValue = it.newValue as String
                    organisationField.value = newValue ?: organisationField.emptyValue
                    break
                case "postalCode":
                    String newValue = it.newValue as String
                    postalCodeField.value = newValue ?: postalCodeField.emptyValue
                    break
                case "street":
                    String newValue = it.newValue as String
                    streetField.value = newValue ?: streetField.emptyValue
                    break
                default:
                    break
            }
        })

        /*
        we listen to the valid properties. whenever the presenter resets values in the viewmodel
        and resets the valid properties the component error on the respective component is removed
        */
        createAffiliationViewModel.addPropertyChangeListener({
            switch (it.propertyName) {
                case "addressAdditionValid":
                    if (it.newValue || it.newValue == null) {
                        addressAdditionField.componentError = null
                    }
                    break
                case "affiliationCategoryValid":
                    if (it.newValue || it.newValue == null) {
                        affiliationCategoryField.componentError = null
                    }
                    break
                case "cityValid":
                    if (it.newValue || it.newValue == null) {
                        cityField.componentError = null
                    }
                    break
                case "countryValid":
                    if (it.newValue || it.newValue == null) {
                        countryField.componentError = null
                    }
                    break
                case "organisationValid":
                    if (it.newValue || it.newValue == null) {
                        organisationField.componentError = null
                    }
                    break
                case "postalCodeValid":
                    if (it.newValue || it.newValue == null) {
                        postalCodeField.componentError = null
                    }
                    break
                case "streetValid":
                    if (it.newValue || it.newValue == null) {
                        streetField.componentError = null
                    }
                    break
                default:
                    break
            }
            submitButton.enabled = allValuesValid()
        })
    }

    private void setupFieldValidators() {
        Validator<String> nonEmptyStringValidator =  Validator.from({ String value -> (value && !value.trim().empty)}, "Empty input not supported.")
        Validator<? extends Object> selectionValidator = Validator.from({o -> o != null}, "Please make a selection.")

        this.addressAdditionField.addValueChangeListener({event ->
            // we do not require this field
            createAffiliationViewModel.addressAdditionValid = true
        })
        this.affiliationCategoryField.addValueChangeListener({event ->
            ValidationResult result = selectionValidator.apply(event.getValue(), new ValueContext(this.affiliationCategoryField))
            if (result.isError()) {
                createAffiliationViewModel.affiliationCategoryValid = false
                UserError error = new UserError(result.getErrorMessage())
                affiliationCategoryField.setComponentError(error)
            } else {
                createAffiliationViewModel.affiliationCategoryValid = true
            }
        })
        this.cityField.addValueChangeListener({event ->
            ValidationResult result = nonEmptyStringValidator.apply(event.getValue(), new ValueContext(this.cityField))
            if (result.isError()) {
                createAffiliationViewModel.cityValid = false
                UserError error = new UserError(result.getErrorMessage())
                cityField.setComponentError(error)
            } else {
                createAffiliationViewModel.cityValid = true
            }
        })
        this.countryField.addValueChangeListener({event ->
            ValidationResult result = nonEmptyStringValidator.apply(event.getValue(), new ValueContext(this.countryField))
            if (result.isError()) {
                createAffiliationViewModel.countryValid = false
                UserError error = new UserError(result.getErrorMessage())
                countryField.setComponentError(error)
            } else {
                createAffiliationViewModel.countryValid = true
            }
        })
        this.organisationField.addValueChangeListener({event ->
            ValidationResult result = nonEmptyStringValidator.apply(event.getValue(), new ValueContext(this.organisationField))
            if (result.isError()) {
                createAffiliationViewModel.organisationValid = false
                UserError error = new UserError(result.getErrorMessage())
                organisationField.setComponentError(error)
            } else {
                createAffiliationViewModel.organisationValid = true
            }
        })
        this.postalCodeField.addValueChangeListener({event ->
            ValidationResult result = nonEmptyStringValidator.apply(event.getValue(), new ValueContext(this.postalCodeField))
            if (result.isError()) {
                createAffiliationViewModel.postalCodeValid = false
                UserError error = new UserError(result.getErrorMessage())
                postalCodeField.setComponentError(error)
            } else {
                createAffiliationViewModel.postalCodeValid = true
            }
        })
        this.streetField.addValueChangeListener({event ->
            ValidationResult result = nonEmptyStringValidator.apply(event.getValue(), new ValueContext(this.streetField))
            if (result.isError()) {
                createAffiliationViewModel.streetValid = false
                UserError error = new UserError(result.getErrorMessage())
                streetField.setComponentError(error)
            } else {
                createAffiliationViewModel.streetValid = true
            }
        })
    }

    private boolean allValuesValid() {
        return createAffiliationViewModel.affiliationCategoryValid \
            && createAffiliationViewModel.cityValid \
            && createAffiliationViewModel.countryValid \
            && createAffiliationViewModel.organisationValid \
            && createAffiliationViewModel.postalCodeValid \
            && createAffiliationViewModel.streetValid
    }

    private void registerListeners() {
        submitButton.addClickListener({
            String category = createAffiliationViewModel.affiliationCategory
            String city = createAffiliationViewModel.city
            String organisation = createAffiliationViewModel.organisation
            String postalCode = createAffiliationViewModel.postalCode
            String street = createAffiliationViewModel.street

            this.controller.createAffiliation(organisation, street, postalCode, city, category) })
    }
}
