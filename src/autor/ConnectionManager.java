package autor;

import autor.Utils.*;
import java.sql.*;

public class ConnectionManager {

	// URL for the oracle server
	private final static String kDatabaseConnectionURL = "jdbc:oracle:thin:@ora.csc.ncsu.edu:1521:orcl01";

	private static Connection __database_connection = null;

	/**
	 * Default constructor for Connection Manager
	 * 
	 * @throws Exception if the connection is not established
	 */
	public ConnectionManager() throws Exception {
		initializeDatabaseConnection();
	}

	public String getUserName() {

		String username = null;
		if (username == null) {
			username = Utils.getStringInput("Please enter username for connecting to database : ");
		}
		return username;
	}

	public String getPassword() {

		String password = null;
		if (password == null) {
			password = Utils.getStringInput("Please enter password for connecting to database : ");
		}
		return password;
	}

	/**
	 * Initializes a connection to the JDBC with a given username and password
	 * 
	 * @throws Exception if the connection is not established
	 */
	public void initializeDatabaseConnection() throws Exception {
		System.out.println("Establishing database connection");

		Class.forName("oracle.jdbc.OracleDriver");

		// Establish connection using credentials above.
		__database_connection = DriverManager.getConnection(kDatabaseConnectionURL, getUserName(), getPassword());

		System.out.println("Connection established successfully.");
	}

	/**
	 * Returns the current database connection
	 * 
	 * @return __database_connection Current database connection
	 */
	public static Connection getDatabaseConnection() {
		return __database_connection;
	}

	/**
	 * Closes the current database connection
	 * 
	 * @throws SQLException if the connection cannot be close
	 */
	public static void closeDatabaseConnection() throws SQLException {
		System.out.println("WARNING - CLOSING DATABASE CONNECTION");
		__database_connection.close();
		return;
	}
};
