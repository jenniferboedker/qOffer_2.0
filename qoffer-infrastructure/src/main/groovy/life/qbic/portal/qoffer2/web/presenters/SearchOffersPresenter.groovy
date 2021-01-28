package life.qbic.portal.qoffer2.web.presenters

import life.qbic.datamodel.dtos.business.Offer
import life.qbic.portal.portlet.offers.search.SearchOffersOutput

/**
 * Presenter for the SearchOffers
 *
 * This presenter handles the output of the Search Offers use case and prepares it for an appropriate view.
 *
 * @since: 1.0.0
 */
class SearchOffersPresenter implements SearchOffersOutput {
    @Override
    void matchingOffers(List<Offer> offers) {
        //TODO implement
    }
}