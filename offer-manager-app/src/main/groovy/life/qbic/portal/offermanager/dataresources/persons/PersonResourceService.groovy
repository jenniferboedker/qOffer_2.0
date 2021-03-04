package life.qbic.portal.offermanager.dataresources.persons

import life.qbic.datamodel.dtos.general.Person
import life.qbic.portal.offermanager.communication.EventEmitter
import life.qbic.portal.offermanager.communication.Subscription
import life.qbic.portal.offermanager.dataresources.ResourcesService

/**
 * Person service that holds resources about available person entries
 *
 * This service holds resources about person information and can be used to subscribe to any
 * update event of the underlying resource data.
 *
 * @since 1.0.0
 */
class PersonResourceService implements ResourcesService<Person>{

    private final PersonDbConnector personDbConnector

    private final List<Person> availablePersonEntries

    private final EventEmitter<Person> eventEmitter

    PersonResourceService(PersonDbConnector personDbConnector) {
        this.personDbConnector = Objects.requireNonNull(personDbConnector, "Database connector " +
                "must not be null.")
        this.availablePersonEntries = personDbConnector.fetchAllActivePersons()
        this.eventEmitter = new EventEmitter<>()
    }

    @Override
    void reloadResources() {

    }

    @Override
    void subscribe(Subscription<Person> subscription) {
        this.eventEmitter.register(subscription)
    }

    @Override
    void unsubscribe(Subscription<Person> subscription) {
        this.eventEmitter.unregister(subscription)
    }

    @Override
    void addToResource(Person resourceItem) {
        this.availablePersonEntries.add(resourceItem)
        this.eventEmitter.emit(resourceItem)
    }

    @Override
    void removeFromResource(Person resourceItem) {
        this.availablePersonEntries.remove(resourceItem)
        this.eventEmitter.emit(resourceItem)
    }

    @Override
    Iterator<Person> iterator() {
        return new ArrayList<>(this.availablePersonEntries).iterator()
    }
}
