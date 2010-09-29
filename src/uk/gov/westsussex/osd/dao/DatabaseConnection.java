package uk.gov.westsussex.osd.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import uk.gov.westsussex.osd.dao.properties.DatabaseProperties;

/**
 * No pooling or fancy stuff here - just get a database connection. 
 * Once supplied this class doesn't care about the connection.
 * 
 * @author awux7820
 *
 */
public class DatabaseConnection {
    
    private static Logger log = Logger.getLogger(DatabaseConnection.class);
    
    private DatabaseConnection() {}
    
    public static Connection getConnection(DatabaseProperties properties) {
        
        Connection connection = null;

        try {
        	log.debug("Getting connection: " + properties.getDbUrl() +
        		", " + properties.getAccount());
        	
            Class.forName(properties.getDriver());
            connection = DriverManager.getConnection(
                properties.getDbUrl(), 
                properties.getAccount(), 
                properties.getPassword());
            connection.setAutoCommit(false);

            return connection;
            
        } catch(ClassNotFoundException cnfe) {
            log.error("Caught class not found Exception:", cnfe);
            throw new RuntimeException(cnfe.getMessage());
        } catch(SQLException sqle) {
            log.error("Caught SQL Exception:", sqle);
            throw new RuntimeException(sqle.getMessage());
        }
    }    
}
