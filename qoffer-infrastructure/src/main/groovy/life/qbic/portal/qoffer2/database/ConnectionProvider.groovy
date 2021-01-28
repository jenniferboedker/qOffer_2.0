package life.qbic.portal.qoffer2.database

import java.sql.Connection
import java.sql.SQLException

/**
 * Provides the ability to connect to a SQL ressource
 *
 * @since: 1.0.0
 */
interface ConnectionProvider {
    Connection connect() throws SQLException
}