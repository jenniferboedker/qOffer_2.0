package life.qbic.portal.qoffer2.web.presenters

import life.qbic.portal.portlet.customers.create.CreateCustomerOutput
import life.qbic.portal.qoffer2.web.viewmodel.ViewModel
import life.qbic.portal.qoffer2.web.viewmodel.CreateCustomerViewModel

/**
 * <short description>
 *
 * <detailed description>
 *
 * @since: <versiontag>
 */
class CreateCustomerPresenter implements CreateCustomerOutput{
    private final ViewModel viewModel
    private final CreateCustomerViewModel createCustomerViewModel

    CreateCustomerPresenter(ViewModel viewModel, CreateCustomerViewModel createCustomerViewModel) {
        this.viewModel = viewModel
        this.createCustomerViewModel = createCustomerViewModel
    }

    private void clearCustomerData() {
        createCustomerViewModel.academicTitle = null
        createCustomerViewModel.firstName = null
        createCustomerViewModel.lastName = null
        createCustomerViewModel.email = null
        createCustomerViewModel.affiliation = null
    }

    @Override
    void successNotification(String notification) {
        viewModel.successNotifications.add(notification)
        clearCustomerData()
    }

    @Override
    void failNotification(String notification) {
        viewModel.failureNotifications.add(notification)
        clearCustomerData()
    }
}
