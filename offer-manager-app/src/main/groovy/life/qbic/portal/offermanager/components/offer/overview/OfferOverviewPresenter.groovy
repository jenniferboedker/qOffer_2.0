package life.qbic.portal.offermanager.components.offer.overview

import life.qbic.business.offers.fetch.FetchOfferOutput
import life.qbic.datamodel.dtos.business.Offer
import life.qbic.portal.offermanager.components.AppViewModel


/**
 * AppPresenter for the FetchOffer Use Case
 *
 * This presenter handles the output of the FetchOffer use case and prepares it for a view.
 *
 * @since: 1.0.0
 */
class OfferOverviewPresenter implements FetchOfferOutput {

    private final AppViewModel viewModel
    private final OfferOverviewModel offerOverviewModel

    OfferOverviewPresenter(AppViewModel viewModel, OfferOverviewModel offerOverviewModel){
        this.viewModel = viewModel
        this.offerOverviewModel = offerOverviewModel
    }

    @Override
    void fetchedOffer(Offer fetchedOffer) {
        this.offerOverviewModel.offer = Optional.ofNullable(fetchedOffer)
    }

    @Override
    void failNotification(String notification) {
        this.viewModel.failureNotifications.add(notification)
    }

}
