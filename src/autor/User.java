package autor;

import java.util.*;
// import autor.ConnectionManager;
import java.sql.*;

public abstract class User {

	/* List of possible User roles */
	public enum UserType {
		ADMIN, MANAGER, RECEPTIONIST, CUSTOMER, MECHANIC
	};

	public enum EmployeeFields {
		// NOTE: Role isn't a user input field and that's the reason why it isn't
		// included here.
		EMPLOYEE_ID("Employee ID"), FIRST_NAME("First Name"), LAST_NAME("Last Name"), USER_NAME("User Name"),
		PASSWORD("Password"), SALARY("Salary"), ADDRESS("Address"), SERVICE_CENTER_ID("Service Center ID"), EMAIL("Email"),
		ROLE("Role");

		private String field;

		private EmployeeFields(String field) {
			this.field = field;
		}

		public String toString() {
			return this.field;
		}
	};

	private UserType __user_type;

	/**
	 * Default constructor for User
	 * 
	 * @param user_type the User's type
	 */
	public User(UserType user_type) {
		__user_type = user_type;
	}

	/**
	 * Returns the User's role
	 * 
	 * @return __user_type the User's type
	 */
	public UserType getUserType() {
		return __user_type;
	}

	public void logOut() {

	}

	private static User __current_user = null;

	/**
	 * Displays the list of menu options available for this user.
	 */
	public abstract void displayMenuForUser();

	public static User loadUser(String username, String password) throws Exception {
		// add db validation & related steps
		UserType user_type = UserType.ADMIN;
		HashMap<String, String> userDetails = new HashMap<String, String>();
		String query = String.format("select * from CREDENTIALS where userid = '%s'", username);
		Connection conn = ConnectionManager.getDatabaseConnection();
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(query);
		if (rs.next() == false) {
			System.out.println("No user found with this username, please try again!");
			throw new Exception("user id not found in the database");
		} else {
			String role_from_DB, pass_from_DB, userid_from_DB;
			role_from_DB = rs.getString("role");
			userid_from_DB = rs.getString("userid");
			pass_from_DB = rs.getString("pwd");
			System.out.println(role_from_DB + userid_from_DB + pass_from_DB);
			if (!pass_from_DB.equals(password)) {
				throw new Exception("Incorrect password... Try again");
			}
			if (role_from_DB == null) {
				String message = String.format("No role found for username: %s", username);
				throw new Exception(message);
			}
			role_from_DB = role_from_DB.toLowerCase();
			if (role_from_DB.equals("customer")) {
				user_type = UserType.CUSTOMER;
				String retrieveCustomerQuery = String.format("select * from CUSTOMER where cid = '%s'", userid_from_DB);
				rs = st.executeQuery(retrieveCustomerQuery);
				if (rs.next() == false) {
					throw new Exception("Customer table: No such customer" + userid_from_DB);
				}
				ResultSetMetaData md = rs.getMetaData();
				int columns = md.getColumnCount();
				do {
					for (int i = 1; i <= columns; ++i) {
						if (rs.getString(md.getColumnName(i)) != null)
							userDetails.put(md.getColumnName(i), rs.getString(md.getColumnName(i)).toString());
						else {
							userDetails.put(md.getColumnName(i), "null");
						}
					}
				} while (rs.next());
			} else if (role_from_DB.equals("employees")) {
				// set the correct type by selecting the employee from employees table
				// Improvement: Can pass the user's details from the table through here
				String retrieveEmployeequery = String.format("select * from EMPLOYEES where employee_id = '%s'",
						userid_from_DB);
				ResultSet rs1 = st.executeQuery(retrieveEmployeequery);
				if (rs1.next() == false) {
					throw new Exception("Employees table: No such employee" + userid_from_DB);
				}
				ResultSetMetaData md = rs1.getMetaData();
				int columns = md.getColumnCount();
				do {
					for (int i = 1; i <= columns; ++i) {
						userDetails.put(md.getColumnName(i), rs1.getString(i));
					}
				} while (rs1.next());
				role_from_DB = userDetails.get("ROLE");
				role_from_DB = role_from_DB.toLowerCase();
				if (role_from_DB == null) {
					String message = String.format("No role found for userid: %s", userid_from_DB);
					throw new Exception(message);
				}
				switch (role_from_DB) {
					case "receptionist":
						user_type = UserType.RECEPTIONIST;
						break;
					case "mechanics":
						user_type = UserType.MECHANIC;
						break;
					case "manager":
						user_type = UserType.MANAGER;
						break;
					default:
						user_type = UserType.ADMIN;
				}
			}
		}
		switch (user_type) {
			case ADMIN:
				return new Admin(user_type);
			case RECEPTIONIST:
				return new Receptionist(user_type);
			case MANAGER:
				return new Manager(user_type);
			case CUSTOMER:
				return new Customer(user_type, userDetails);
			case MECHANIC:
				return new Mechanic(user_type, username);
			default: {
				System.out.println("ERROR - UNKNOWN USER TYPE ENCOUNTERED " + user_type);
				Utils.printInvalidSelectionMessage();
				performLogin();
				return __current_user;
			}
		}
	}

	public static void performLogin() {
		Utils.printSeperator();
		Scanner scanner = Utils.getScanner();
		System.out.println("Enter user id: ");
		String user_id = scanner.nextLine();

		System.out.println("Enter password : ");
		String password = scanner.nextLine();

		try {
			__current_user = loadUser(user_id, password);
			__current_user.displayMenuForUser();
		} catch (Exception e) {
			System.out.println("Login failed, please try again. Details : " + e.getMessage());
			ProjMain.getInstance().displayMainMenu();
		}

	}

	public static Map<EmployeeFields, String> getEmployeeInfo(String service_center_id, String role) {
		Scanner scanner = Utils.getScanner();

		Map<EmployeeFields, String> details = new HashMap<EmployeeFields, String>();

		System.out.println("Enter first name : ");
		details.put(EmployeeFields.FIRST_NAME, scanner.nextLine());

		System.out.println("Enter last name : ");
		details.put(EmployeeFields.LAST_NAME, scanner.nextLine());

		System.out.println("Enter username : ");
		details.put(EmployeeFields.USER_NAME, scanner.nextLine());

		System.out.println("Enter password : ");
		details.put(EmployeeFields.PASSWORD, scanner.nextLine());

		System.out.println("Enter employee ID : ");
		details.put(EmployeeFields.EMPLOYEE_ID, scanner.nextLine());

		System.out.println("Enter address : ");
		details.put(EmployeeFields.ADDRESS, scanner.nextLine());

		System.out.println("Enter salary : ");
		details.put(EmployeeFields.SALARY, scanner.nextLine());

		System.out.println("Enter email : ");
		details.put(EmployeeFields.EMAIL, scanner.nextLine());

		details.put(EmployeeFields.SERVICE_CENTER_ID, service_center_id);
		details.put(EmployeeFields.ROLE, role);

		return details;
	}

};
