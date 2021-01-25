package life.qbic.portal.portlet.customers.affiliation.create

import life.qbic.datamodel.dtos.business.Affiliation
import life.qbic.portal.portlet.UseCaseFailure

/**
 * Output interface for the {@link life.qbic.portal.portlet.customers.affiliation.create.CreateAffiliation} use
 * case
 *
 * @since: 1.0.0
 */
interface CreateAffiliationOutput extends UseCaseFailure {
    /**
     * This method informs the output that the provided affiliation was created
     * @param affiliation the affiliation that was created.
     * @since 1.0.0
     */
    void affiliationCreated(Affiliation affiliation)

    /**
     * This method informs the output that the afiliation was created successfully
     * @param message
     */
    void successNotification(String message)
}
