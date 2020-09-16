package life.qbic.portal.portlet;

import life.qbic.portal.qoffer2.qOfferPortlet;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link qOfferPortlet}.
 */
public class qOfferPortletTest {

    @Test
    public void mainUIExtendsQBiCPortletUI() {
        assertTrue("The main UI class must extend life.qbic.portlet.QBiCPortletUI", 
            QBiCPortletUI.class.isAssignableFrom(qOfferPortlet.class));
    }

    @Test
    public void mainUIIsNotQBiCPortletUI() {
        assertFalse("The main UI class must be different to life.qbic.portlet.QBiCPortletUI", 
            QBiCPortletUI.class.equals(qOfferPortlet.class));
    }
}