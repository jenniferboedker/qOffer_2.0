package life.qbic.portal.offermanager.components.offer.overview

import groovy.util.logging.Log4j2
import life.qbic.business.offers.fetch.FetchOfferInput
import life.qbic.datamodel.dtos.business.OfferId

/**
 * Controller class adapter from view information into use case input interface
 *
 * This class translates the information that was received from the OfferOverView into method calls to the use case
 *
 * @since: 1.0.0
 *
 */
@Log4j2
class OfferOverviewController {

    private final FetchOfferInput input

    OfferOverviewController(FetchOfferInput input){
        this.input = input
    }

    /**
     * This method calls the Fetch Offer use case with the provided OfferId
     *
     * @param offerId The OfferId of the Offer to be retrieved
     */
    void fetchOffer(OfferId offerId){
      log.info("-fetchoffer before")
        this.input.fetchOffer(offerId)
        log.info("-fetchoffer after")
    }
}
