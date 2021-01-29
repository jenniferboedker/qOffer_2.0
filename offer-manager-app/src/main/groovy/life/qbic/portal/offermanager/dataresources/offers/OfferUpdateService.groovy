package life.qbic.portal.offermanager.dataresources.offers

import life.qbic.datamodel.dtos.business.Offer
import life.qbic.portal.offermanager.communication.EventEmitter
import life.qbic.portal.offermanager.communication.Subscription
import life.qbic.portal.offermanager.dataresources.ResourcesService

/**
 * Service that represents an offer to be updated.
 *
 * This service offers an EventEmitter property that can be
 * used for inter component communication, when a new offer
 * source is available for update.
 *
 * @since 1.0.0
 */
class OfferUpdateService implements ResourcesService {

    EventEmitter<Offer> offerForUpdateEvent

    OfferUpdateService() {
        this.offerForUpdateEvent = new EventEmitter<>()
    }

    @Override
    void reloadResources() {
        // Nothing to reload
    }

    @Override
    void subscribe(Subscription subscription) {

    }

    @Override
    void unsubscribe(Subscription subscription) {

    }

    @Override
    void addToResource(Object resourceItem) {

    }

    @Override
    void removeFromResource(Object resourceItem) {

    }

    @Override
    Iterator iterator() {
        return null
    }
}
