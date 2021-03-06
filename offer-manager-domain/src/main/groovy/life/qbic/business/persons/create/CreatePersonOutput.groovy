package life.qbic.business.persons.create

import life.qbic.business.UseCaseFailure
import life.qbic.datamodel.dtos.general.Person

/**
 * Output interface for the {@link CreatePerson} use
 * case
 *
 * @since: 1.0.0
 * @author: Tobias Koch
 */
interface CreatePersonOutput extends UseCaseFailure {

    /**
     * Is called by the use case, when a new customer has been created
     * @param message
     * @deprecated Use the more explicit #customerCreated(Person person) method
     */
    @Deprecated
    void personCreated(String message)

    /**
     * Is called by the use case, when a new customer resource has been created
     * @param person The newly created person resource
     */
    void personCreated(Person person)

}