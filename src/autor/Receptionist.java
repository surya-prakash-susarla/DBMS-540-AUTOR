package autor;

import autor.User;

import autor.Utils.*;
import java.util.*;
import java.sql.*;
import java.util.stream.*;

public class Receptionist extends User {

	/**
	 * Default constructor for Receptionist
	 * 
	 * @param user_type the Receptionist's type, calls User super constructor
	 */
	public Receptionist(UserType user_type) {
		super(user_type);
	}

	@Override
	public void displayMenuForUser() {
		Map<Integer, String> options = new HashMap<Integer, String>();
		options.put(1, "Add new customer profile");
		options.put(2, "Find customers with pending invoices");
		options.put(3, "Logout");
		Integer selection = Utils.getInputFromMenu(options);
		switch (selection) {
			case 1: {
				addCustomerProfile();
				break;
			}
			case 2: {
				findCustomersWithPendingInvoices();
				break;
			}
			case 3: {
				super.logOut();
				return;
			}
			default: {
				System.out.println("Invalid input, please try again");
			}
		}
		displayMenuForUser();
		return;
	}

	public void addCustomerProfile() {
		Utils.printSeperator();
		String role = "customer"; // for role of credentials table, set to customer by default.

		Scanner scanner = Utils.getScanner();
		System.out.println("Please enter the information of the customer ");
		System.out.println("Enter first name of customer : ");
		String first_name = scanner.nextLine();

		System.out.println("Enter last name of customer : ");
		String last_name = scanner.nextLine();

		System.out.println("Enter address of customer : ");
		String address = scanner.nextLine();

		System.out.println("Enter email of customer : ");
		String email = scanner.nextLine();


		System.out.println("Enter customer id : ");
		String customer_id = scanner.nextLine();

		System.out.println("Enter customer status (0 for inactive, 1 for good standing, and 0 for not good standing) : ");
		// clean leftover
		int status = scanner.nextInt();

		System.out.println("Enter ID of the Service Center : ");
		int service_center_id = scanner.nextInt();

		System.out.println("Please enter the credentials of the customer ");
		System.out.println("Enter username of the customer : ");
		// clean leftover
		scanner.nextLine();
		String username = scanner.nextLine();

		System.out.println("Enter password of the customer : ");
		String pwd = scanner.nextLine();

		// Car needs vin(string), cid(string), manufacture(string), mileage(int),
		// year(int),lastmantenace mileage(int)
		System.out.println("Please enter the vehicle information of the customer ");
		System.out.println("Enter vin associated with customer : ");
		String vin = scanner.nextLine();

		System.out.println("Enter manufacturer of the vehicle : ");
		String manufacturer = scanner.nextLine();

		System.out.println("Enter mileage of the vehicle : ");
		Integer mileage = scanner.nextInt();

		System.out.println("Enter year of the vehicle : ");
		Integer year = scanner.nextInt();

		System.out.println("Enter last maintenance service of the vehicle(0,1,2) : ");
		Integer lastMileage = scanner.nextInt();
		// Create the credentials
		addNewCredentialsImpl(customer_id, username, pwd, role);
		// Create a new customer
		addNewCustomerProfileImpl(first_name, last_name, customer_id, status, service_center_id, email, address);
		// Create the related car
		addNewCarImpl(vin, customer_id, manufacturer, mileage, year, lastMileage);

		System.out.println("New customer added successfully!");
		Utils.printSeperator();
	}

	/**
	 * Creates a Customer account with the given info
	 * 
	 * @param firstName         Customer first name
	 * @param lastName          Customer last name
	 * @param id                Customer unique id
	 * @param status            Customer status
	 * @param service_center_id Id of the service center the customer belongs
	 */
	public void addNewCustomerProfileImpl(String firstName, String lastName, String id, int status,
			int service_center_id, String email, String address) {

		try {
			Connection connection = ConnectionManager.getDatabaseConnection();
			String add_new_customer_query = "INSERT INTO CUSTOMER VALUES(?,?,?,?,?,?,?)";
			PreparedStatement statement = connection.prepareStatement(add_new_customer_query);
			statement.setString(1, id);
			statement.setString(2, firstName);
			statement.setString(3, lastName);
			statement.setString(4, address);
			statement.setString(5, email);
			statement.setInt(6, status);
			statement.setInt(7, service_center_id);
			int rowNumber = statement.executeUpdate();
			System.out.print(rowNumber + "customer added.");

		} catch (SQLException e) {
			Utils.printSeperator();
			System.out.println("ERROR while adding a new customer, details : " + e.getMessage());
		}

	}

	/**
	 * Creates credentials for the new created customer with the given info
	 * 
	 * @param id       Customer unique id
	 * @param username Cusotmer unsername
	 * @param pwd      Customer's associated vehicle,
	 * @param role     the role in the credentials table
	 */
	public void addNewCredentialsImpl(String id, String username, String pwd, String role) {
		try {
			Connection connection = ConnectionManager.getDatabaseConnection();
			String add_new_CREDENTIALS_query = "INSERT INTO CREDENTIALS VALUES(?,?,?,?)";
			PreparedStatement statement = connection.prepareStatement(add_new_CREDENTIALS_query);
			statement.setString(1, id);
			statement.setString(2, username);
			statement.setString(3, pwd);
			statement.setString(4, role);
			int rowNumber = statement.executeUpdate();
			System.out.print(rowNumber + "credentioals added.");
		} catch (SQLException e) {
			Utils.printSeperator();
			System.out.println("ERROR while adding new credentials, details : " + e.getMessage());
		}
	}

	/**
	 * Creates the vehicle for the new created customer with the given info
	 * 
	 * @param vin             Vehicle identification number
	 * @param id              Customer unique id
	 * @param manufacture     Manufacture of the vehicle
	 * @param mileage         Vehicle's mileage
	 * @param year            the year of the vehicle
	 * @param lastmaintenance the mileage from last maintenance
	 */
	public void addNewCarImpl(String vin, String id, String manufacture, int mileage, int year, int lastmaintenance) {
		try {
			Connection connection = ConnectionManager.getDatabaseConnection();
			String add_new_CREDENTIALS_query = "INSERT INTO CAR VALUES(?,?,?,?,?,?)";
			PreparedStatement statement = connection.prepareStatement(add_new_CREDENTIALS_query);
			statement.setString(1, vin);
			statement.setString(2, id);
			statement.setString(3, manufacture);
			statement.setInt(4, mileage);
			statement.setInt(5, year);
			statement.setInt(6, lastmaintenance);
			int rowNumber = statement.executeUpdate();
			System.out.print(rowNumber + "car added.");

		} catch (SQLException e) {
			Utils.printSeperator();
			System.out.println("ERROR while adding new vehicle, details : " + e.getMessage());
		}
	}

	/**
	 * Finds customers with pending invoices and displays their info
	 */
	public void findCustomersWithPendingInvoices() {
		try {
			Connection connection = ConnectionManager.getDatabaseConnection();
			String add_new_customer_query = "Select * from CUSTOMER c INNER JOIN INVOICE i ON c.cid = i.cid Where i.status = 0 Order by c.cid";
			PreparedStatement statement = connection.prepareStatement(add_new_customer_query);
			ResultSet results = statement.executeQuery();
			System.out.println(
					"cid--firstName--lastName--service center id--invoiceID--invoiceDate--invoiceStatus--mechanics--serviceString--costs--vin");
			while (results.next()) {
				String cid = results.getString("cid");
				String firstName = results.getString("firstName");
				String lastName = results.getString("lastName");
				String service_center = results.getString("service_center_id");
				String invoiceID = results.getString("invoice_id");
				String invoiceDate = results.getString("invoice_date");
				// 0 for unpaid - by default; 1 for paid;
				int invoiceStatus = results.getInt("status");
				String mechanics = results.getString("mechanics");
				String serviceString = results.getString("service_string");
				float costs = results.getFloat("costs");
				String vin = results.getString("vin");

				if (invoiceStatus == 1) {
					// when status == 1 the invoice is paid
					System.out.println(cid + " " + firstName + " " + lastName + " " +
							service_center + " " + invoiceID + " " + invoiceDate + " paid " +
							mechanics + " " + serviceString + " " + costs + " " + vin);
				} else {
					// when status == 0 the invoice is unpaid
					System.out.println(cid + " " + firstName + " " + lastName + " " +
							service_center + " " + invoiceID + " " + invoiceDate + " unpaid " +
							mechanics + " " + serviceString + " " + costs + " " + vin);
				}
			}
		} catch (SQLException e) {
			Utils.printSeperator();
			System.out.println("ERROR while finding customers with pending invoices, details : " + e.getMessage());
		}
	}
};
