package autor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Manager extends User {

	private final String[] manufacturers = { "Honda", "Nissan", "Toyota", "Lexus", "Infiniti" };

	/**
	 * Default constructor for Manager
	 * 
	 * @param user_type the Manager's type, calls User super constructor
	 */
	public Manager(UserType user_type) {
		super(user_type);
	}

	@Override
	public void displayMenuForUser() {
		Map<Integer, String> manager_options = new HashMap<Integer, String>();
		manager_options.put(1, "Add new employee");
		manager_options.put(2, "Select operational hours");
		manager_options.put(3, "Set up service prices");
		manager_options.put(4, "Logout");

		Integer selection = Utils.getInputFromMenu(manager_options);
		switch (selection) {
		case 1: {
			addEmployee();
			break;
		}
		case 2: {
			selectOperationalHrs();
			break;
		}
		case 3: {
			setUpServicePrices();
			break;
		}
		case 4: {
			returnToMainMenu();
			break;
		}
		case 5: {
			super.logOut();
			return;
		}
		default: {
			System.out.println("Invalid choice - please try again");
			return;
		}
		}
		displayMenuForUser();
	}

	/**
	 * Initializes a store. Store is available once it has three Mechanics and has
	 * operational hours and service prices
	 */

	// TODO Make a service center only appear active once it has 3 mechanics and 1
	// receptionist

	public void addEmployee() {

		System.out.println(Utils.getSeperator());
		Scanner scanner = Utils.getScanner();

		String employee_id = Utils.generateID();
		employee_id = employee_id.substring(employee_id.length() - 9);

		System.out.println("Enter employee first name : ");
		String employee_first_name = scanner.nextLine();

		System.out.println("Enter employee last name : ");
		String employee_last_name = scanner.nextLine();

		System.out.println("Enter employee address : ");
		String employee_address = scanner.nextLine();

		System.out.println("Enter employee role (receptionist or mechanic) : ");
		String role = scanner.nextLine();

		System.out.println("Enter the email address of the employee : ");
		String email = scanner.nextLine();

		System.out.println("Enter employee salary : ");
		Double salary = scanner.nextDouble();

		scanner.nextLine();
		System.out.println("Enter store id : ");
		String store_id = scanner.nextLine();

		addEmployeeImpl(employee_id, employee_first_name, employee_last_name, employee_address, email, role, salary, store_id);

		Utils.printSeperator();
	}

	/**
	 * Add an employee (Receptionist or Mechanic) to this Manger's store
	 * 
	 * @param userType Employee UserType
	 * @param pay      Employee salary or hourly pay
	 * @throws SQLException if any sql query fails
	 */
	public void addEmployeeImpl(String employee_id, String employee_first_name, String employee_last_name,
			String employee_address, String email, String role, double salary, String store_id) {

		ProjMain.getInstance().getConnectionManager();

		// Attempts to establish a connection
		Connection con = ConnectionManager.getDatabaseConnection();
		Statement stmt = null;

		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		/**
		 * If attempting to add a receptionist, first check if this store has a
		 * receptionist. The EMPLOYEES table is parsed for receptionists associated with
		 * this service center, and if the resulting table is empty, we can safely add
		 * another receptionist.
		 */
		if (role.equals("receptionist")) {
			// Retrieve the list of receptionists at this store, if any
			ResultSet receptionistsSet = null;
			String receptionistSelect = String.format(
					"select * from EMPLOYEES where role = 'receptionist' AND service_center_id = '%s'", store_id);
			try {
				receptionistsSet = stmt.executeQuery(receptionistSelect);
			} catch (SQLException e) {
				System.out.println("Couldn't retrieve receptionists");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}

			/**
			 * Check if there are any rows in the results set. If no receptionist was
			 * present at this store, the ResultSet should be empty
			 */
			try {
				while (receptionistsSet.next()) {
					if (receptionistsSet.getString("role").equals("receptionist")) {
						System.out.println("ERROR: This store already has a receptionist");
						return;
					}
				}
				
				// if (receptionistsSet.isBeforeFirst()) {
				// 	System.out.println("ERROR: This store already has a receptionist");
				// 	return;
				// }
			} catch (SQLException e) {
				System.out.println("Something went wrong with the receptionist ResultSet");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}

		/**
		 * Checking if the salary falls between the min and max wage for this store. The
		 * SERVICE_CENTER table is queried for the min and max wage
		 */
		String salarySelect = String
				.format("select min_wage, max_wage from SERVICE_CENTER where service_center_id = '%s'", store_id);
		ResultSet wagesSet = null;
		try {
			wagesSet = stmt.executeQuery(salarySelect);
		} catch (SQLException e) {
			System.out.println("Couldn't retrieve min and max wages");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		try {
			wagesSet.next();
			float min_wage = wagesSet.getFloat("min_wage");
			float max_wage = wagesSet.getFloat("max_wage");
			if (min_wage > salary) {
				System.out.println("Salary cannot be less than the minmum wage for this store");
				return;
			}
			if (max_wage < salary) {
				System.out.println("Salary cannot be more than the maximum wage for this store");
				return;
			}
		} catch (SQLException e) {
			System.out.println("Couldn't parse wagesSet for min and max wage");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		// Insert this employee first into the credentials table
		String credentialInsert = String.format("insert into CREDENTIALS values('%s', '%s', '%s', '%s')", employee_id,
				employee_first_name, employee_last_name, "employees");

		try {
			stmt.executeUpdate(credentialInsert);
		} catch (SQLException e) {
			System.out.println("Credential value insertion failed");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		// If everything is good to go, insert this employee into the table
		String employeeInsert = String.format("insert into EMPLOYEES values('%s', '%s', '%s', '%s', '%s', '%f', '%s', '%s')",
				employee_id, employee_first_name, employee_last_name, employee_address, role, salary, store_id, email);
		try {
			stmt.executeUpdate(employeeInsert);
		} catch (SQLException e) {
			System.out.println("Employee value insertion failed");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		System.out.println("New employee added successfully!");

	}

	/**
	 * Sets whether this store is open on Saturdays. Queries the user and calls
	 * selectOperationalHoursImpl with the supplied truth value
	 */
	public void selectOperationalHrs() {
		System.out.println(Utils.getSeperator());
		Scanner scanner = Utils.getScanner();

		System.out.println("Enter store id : ");
		String store_id = scanner.nextLine();

		System.out.println("Is this store open on Saturday? (Y/N) : ");
		String open_on_saturday = "";
		while (!open_on_saturday.equals("Y") && !open_on_saturday.equals("N")) {
			open_on_saturday = scanner.nextLine();
			if (open_on_saturday.equals("Y") || open_on_saturday.equals("N")) {
				selectOperationalHrsImpl(open_on_saturday, store_id);
			} else {
				System.out.println("Please enter either Y or N");
			}
		}

		System.out.println("Hours updates successfully!");
		Utils.printSeperator();
	}

	/**
	 * Set whether this Manager's store is open on Saturday or not
	 * 
	 * @param openOnSaturday Whether the store is open on Saturday
	 */
	public void selectOperationalHrsImpl(String open_on_saturday, String store_id) {

		/**
		 * open_on_saturday needs to be converted into an int since Oracle SQL doesn't
		 * support boolean values. 1 = True, 0 = False
		 */
		int openOnSaturday = (open_on_saturday == "Y") ? 1 : 0;

		ProjMain.getInstance().getConnectionManager();

		// Attempts to establish a connection
		Connection con = ConnectionManager.getDatabaseConnection();
		PreparedStatement stmt;

		String saturdayUpdate = String.format(
				"update SERVICE_CENTER set saturday = '%d' where service_center_id = '%s'", openOnSaturday, store_id);
		try {
			stmt = con.prepareStatement(saturdayUpdate);
			stmt.executeUpdate(saturdayUpdate);
		} catch (SQLException e) {
			System.out.println("Saturday hours update failed");
			e.printStackTrace();
		}
	}

	/**
	 * Assign prices for individual services
	 */
	public void setUpServicePrices() {

		System.out.print("Enter the id of your store : ");
		Scanner reader = Utils.getScanner();
		String store_id = reader.nextLine();
		String query = "select * from SERVICE_COST where service_center_id = ?";
		Connection conn = ConnectionManager.getDatabaseConnection();
		PreparedStatement stmt;
		List<String> services = new ArrayList<String>(); // List of services for this store
		// Try to get and display the list of services for this store
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, store_id);
			ResultSet rs = stmt.executeQuery();
			int idx = 1;
			while (rs.next()) {
				System.out.println(Utils.getSeperator());
				String message = "SERVICE: " + Integer.toString(idx) + "\n";
				String service_id = rs.getString(1);
				services.add(service_id);
				message += "SERVICE ID: " + service_id + "\n";
				message += "PRICE: " + rs.getString(2) + "\n";
				message += "MANUFACTURER: " + rs.getString(3) + "\n";
				System.out.println(message);
				idx++;
			}
		} catch (SQLException e) {
			System.out.println("Something went wrong with retreiving service prices");
			System.out.println(e.getMessage());
			e.printStackTrace();
			return;
		}

		// Allows the user to update individual service prices, or exit by entering 'N'
		String serviceChoice = "";
		String manufacturerChoice = "";
		while (!serviceChoice.equals("N")) {
			// Query for service ID
			System.out.println("Enter the Service ID of the service you would like to change, or N to exit : ");
			serviceChoice = reader.nextLine();

			// Query for manufacturer
			System.out.println(
					"Enter the manufacturer to which this service applies.\nValid manufacturers are Honda, Nissan, Toyota, Lexus, and Infiniti");
			manufacturerChoice = reader.nextLine();

			// Make sure the user entered a valid manufacturer
			List<String> manuList = Arrays.asList(manufacturers);
			if (!manuList.contains(manufacturerChoice)) {
				System.out.println("Please enter a valid manufacturer");
				continue;
			}

			// Make sure the user entered a valid service id, if so query for price
			int priceChoice = 0;
			if (services.contains(serviceChoice)) {
				priceChoice = 0;
				System.out.println("Enter the integer price you would like to assign this service : ");
				priceChoice = reader.nextInt();
				// Make sure the user didn't enter a negative price
				if (priceChoice < 0) {
					System.out.println("Please enter a positive price");
					continue;
				}
			} else {
				System.out.println("Please enter a valid service id for this store");
				continue;
			}
			// Try update the SERVICE_COST table with the new service price
			String priceUpdate = "update SERVICE_COST set amount = ? where service_id = ? and manufacturerName = ? and service_center_id = ?";
			try {
				stmt = conn.prepareStatement(priceUpdate);
				stmt.setInt(1, priceChoice);
				stmt.setString(2, serviceChoice);
				stmt.setString(3, manufacturerChoice);
				stmt.setString(4, store_id);
				int matches = stmt.executeUpdate();
				System.out.println(matches + " prices updated");
			} catch (SQLException e) {
				System.out.println("Something went wrong with updating prices");
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			System.out.println();
		}

	}

	public void returnToMainMenu() {
		ProjMain.getInstance().displayMainMenu();
		return;
	}
};
