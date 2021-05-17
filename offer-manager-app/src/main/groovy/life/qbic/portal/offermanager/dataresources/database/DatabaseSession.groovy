package life.qbic.portal.offermanager.dataresources.database

import groovy.util.logging.Log4j2
import org.apache.commons.dbcp2.BasicDataSource

import java.sql.Connection
import java.sql.SQLException

/**
 * Creates a connection to the user database
 *
 * A class for setting up the connection to the user database. It should be used when data needs to be retrieved from the
 * DB or written into it.
 *
 * @since: 1.0.0
 * @author: Jennifer Bödker
 *
 */
@Log4j2
class DatabaseSession implements ConnectionProvider {

    protected static DatabaseSession INSTANCE

    private BasicDataSource dataSource

    private DatabaseSession() {
        //This is a private Singleton constructor
        dataSource = null
    }


    /**
     * Initiates the database connection
     * The instance is only created if there is no other existing
     * @param user the user to use for the database
     * @param password the password to use for the database connection
     * @param host the database host
     * @param port the port on which the database is hosted
     * @param sqlDatabase the name of the database
     */
    static void init(String user,
                     String password,
                     String host,
                     String port,
                     String sqlDatabase) {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseSession()

            Class.forName("com.mysql.jdbc.Driver")
            String url = "jdbc:mysql://${host}:${port}/${sqlDatabase}"

            BasicDataSource basicDataSource = new BasicDataSource()
           
            basicDataSource.setTestOnBorrow(true)
            basicDataSource.setValidationQueryTimout(5)
            basicDataSource.setUrl(url)
            basicDataSource.setUsername(user)
            basicDataSource.setPassword(password)
            basicDataSource.setMinIdle(5)
            basicDataSource.setMaxIdle(10)
            basicDataSource.setMaxOpenPreparedStatements(100)
            INSTANCE.dataSource = basicDataSource
        } else {
            log.warn("Skipped overwrite existing connection to $host:$port with $host:$port.")
        }
    }

    /**
     * Creates a database connection by login into the database based on the given credentials
     *
     * @return Connection, otherwise null if connecting to the database fails
     * @throws SQLException if a database access error occurs or the url is {@code null}
     */
    Connection connect() throws SQLException {
      log.info("connect called")
      log.info("dataSource:")
      log.info(dataSource)
      try {
        return dataSource.getConnection()
      } catch (Exception e) {
        log.error("timeout")
        log.error(e)
        e.printStackTrace()
        return null
      }      
    }

    /**
     * Returns the current DatabaseSession object
     * @return
     */
    static DatabaseSession getInstance() {
        if (! INSTANCE) {
            throw new AssertionError("Call the init method first. Instance has not been initialized.")
        } else {
            return INSTANCE
        }
    }
}
