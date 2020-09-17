package life.qbic.portal.qoffer2.web.views


import com.vaadin.ui.Button
import com.vaadin.ui.ComboBox
import com.vaadin.ui.FormLayout
import groovy.util.logging.Log4j2
import life.qbic.portal.qoffer2.web.ViewModel
import life.qbic.datamodel.persons.Affiliation
import com.vaadin.ui.TextField

/**
 * This class generates a Form Layout in which the user
 * can input the necessary information for the creation of a new customer
 *
 * CreateCustomerView will be integrated into the qOffer 2.0 Portlet and provides an User Interface
 * with the intention of enabling a user the creation of a new Customer in the QBiC Database
 *
 * @since: 1.0.0
 */

@Log4j2
class CreateCustomerView extends FormLayout {
    final private ViewModel viewModel

    String firstName
    String lastName
    String email
    Affiliation affiliation
    HashMap customerInfo

    TextField firstNameField
    TextField lastNameField
    TextField emailField
    ComboBox affiliationComboBox
    Button submitButton

    private boolean firstNameValidity
    private boolean lastNameValidity
    private boolean emailValidity
    private boolean affiliationValidity

    CreateCustomerView(ViewModel viewModel) {
        super()
        this.viewModel = viewModel
        this.customerInfo = new HashMap()
        initLayout()
    }

    /**
     * Generates a vaadin Form Layout as an UserInterface consisting of vaadin components
     * to enable user input for Customer creation
     */
    private def initLayout() {

        //Generate FormLayout and the individual components
        FormLayout createCustomerForm = new FormLayout()

        this.firstNameField = new TextField("First Name")
        firstNameField.setPlaceholder("First Name")

        this.lastNameField = new TextField("Last Name")
        lastNameField.setPlaceholder("Last Name")

        this.emailField = new TextField("Email Address")
        emailField.setPlaceholder("Email Address")

        generateAffiliationSelector(viewModel.affiliations)
        affiliationComboBox.emptySelectionAllowed = false

        this.submitButton = new Button("Create Customer")

        //Add the components to the FormLayout
        createCustomerForm.addComponent(firstNameField)
        createCustomerForm.addComponent(lastNameField)
        createCustomerForm.addComponent(emailField)
        createCustomerForm.addComponent(affiliationComboBox)
        createCustomerForm.addComponent(submitButton)

        //Add Validators to the components
        this.addComponent(createCustomerForm)
    }

    /**
     * Generates a Combobox, which can be used for Affiliation selection by the user
     * @param affiliationList :
     * @return Vaadin Combobox component
     */
    private void generateAffiliationSelector(List<Affiliation> affiliationList) {

        this.affiliationComboBox =
                new ComboBox<>("Select an Affiliation")
        affiliationComboBox.setPlaceholder("Select Affiliation")
        affiliationComboBox.setItems(affiliationList)
        affiliationComboBox.setItemCaptionGenerator({ Affiliation af -> af.groupName })
        affiliationComboBox.setEmptySelectionAllowed(false)
    }

}
