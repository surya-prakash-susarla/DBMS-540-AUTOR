package autor;

import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Admin extends User {
	public static Admin __instance = null;

	/**
	 * Default constructor for Admin
	 * 
	 * @param user_type the Admin's type, calls User super constructor
	 */
	public Admin(User.UserType user_type) {
		super(user_type);
	}

	/**
	 * Displays a list of Admin
	 */
	@Override
	public void displayMenuForUser() {
		Map<Integer, String> admin_options = new HashMap<Integer, String>();
		admin_options.put(1, "System Set Up");
		admin_options.put(2, "Add new store");
		admin_options.put(3, "Add new service");
		admin_options.put(4, "Logout");

		Integer selection = Utils.getInputFromMenu(admin_options);
		switch (selection) {
			case 1: {
				systemSetUp();
				break;
			}
			case 2: {
				addNewStore();
				break;
			}
			case 3: {
				addNewService();
				break;
			}
			case 4: {
				returnToMainMenu();
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
	 * Queries the user for variables used to set up a store
	 */
	private void systemSetUp() {
		Utils.printSeperator();

		// 1. load individual service information.
		// 2. load bundle service information.
		// 3. load store information.

		loadStoreInformationFromFile();
		System.out.println("ADMIN - store information loaded from file!");

		loadServiceInfoFromFile();
		System.out.println("ADMIN - services  loaded from file!");

		loadServiceBundlesFromFile();
		System.out.println("ADMIN - service bundles loaded from file!");

		

		System.out.println("ADMIN - System setup complete!");
	}

	public ArrayList<String> getManufacturers() {
		ArrayList<String> mans = new ArrayList<String>();
		Connection conn = ConnectionManager.getDatabaseConnection();
		String query = "select manufacturerName from MANUFACTURER";
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				mans.add(rs.getString("manufacturerName"));
			}
		} catch (SQLException e) {
			System.out.println("Error in getting manufacturers: " + e.getMessage());
			e.printStackTrace();
		}
		return mans;
	}

	private void loadServiceInfoFromFile() {
		ArrayList<Map<String, String>> services = DataReader.readServiceInfoFromFile();
		for (Map<String, String> service : services) {
			insertIndividualService(service.get(DataReader.kServiceInfoFields[0]),
					service.get(DataReader.kServiceInfoFields[1]));
			insertTimeForAllMans(service.get("service_id"),
					service.get("time"));
			insertCostforAllSCAndMans(service.get("service_id"),
					service.get("price_tier"));
		}
	}

	public ArrayList<String> getStores() {
		ArrayList<String> stores = new ArrayList<String>();
		Connection conn = ConnectionManager.getDatabaseConnection();
		String query = "select service_center_id from SERVICE_CENTER";
		Statement st;
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				stores.add(rs.getString("service_center_id"));
			}
		} catch (SQLException e) {
			System.out.println("Error in getting all service centers: " + e.getMessage());
			e.printStackTrace();
		}
		return stores;
	}

	private void insertSCUtil(String serviceid, String manufacturerName, String service_center_id, int amount) {
		try {
			Connection conn = ConnectionManager.getDatabaseConnection();
			String query = "insert into SERVICE_COST values(?,?,?,?)";
			// System.out.println("This is the serviceid " + serviceid);
			PreparedStatement st = conn.prepareStatement(query);
			st.setString(1, serviceid);
			st.setInt(2, amount);
			st.setString(3, manufacturerName);
			st.setString(4, service_center_id);
			st.executeUpdate();
			// System.out.println("Executed Update on Service Cost");
		} catch (SQLException e) {
			System.out.println("Error in inserting records to service center" + e.getMessage());
			e.printStackTrace();
		}
	}

	private void insertCostforAllSCAndMans(String serviceid, String price) {
		// System.out.println("We're here!" + serviceid + ", " + price);
		int int_price = Integer.parseInt(price);
		ArrayList<String> mans = getManufacturers();
		// System.out.println(mans.size());
		ArrayList<String> ServiceCenters = getStores();
		// System.out.println(ServiceCenters.size());
		for (String man : mans) {
			for (String scid : ServiceCenters) {
				// System.out.println(serviceid);
				insertSCUtil(serviceid, man, scid, int_price);
			}
		}
	}

	private void loadServiceBundlesFromFile() {
		ArrayList<Map<String, String>> bundles = DataReader.readServiceBundleFromFile();
		for (Map<String, String> bundle : bundles) {
			// System.out.println("inserting service_id" + bundle.get("Service_ID"));
			insertMaintenanceService(bundle.get("Service_ID"),
					bundle.get("Services"));
			insertTimeForAllMans(bundle.get("Service_ID"), bundle.get("Time"));
			insertCostforAllSCAndMans(bundle.get("Service_ID"),
					bundle.get("PriceTier"));
		}
	}

	private void insertTimeForAllMans(String serviceID, String time) {
		ArrayList<String> mans = getManufacturers();
		int int_time = Integer.parseInt(time);
		for (String man : mans) {
			try {
				Connection conn = ConnectionManager.getDatabaseConnection();
				String query = "insert into SERVICE_TIME values (?, ?,?)";
				PreparedStatement st = conn.prepareStatement(query);
				st.setString(1, man);
				st.setString(2, serviceID);
				st.setInt(3, int_time);
				st.executeUpdate();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void insertMaintenanceService(String service_id, String Services) {
		try {
			Connection connection = ConnectionManager.getDatabaseConnection();
			String insert_into_services_query = "insert into SERVICES(service_id) values(?)";
			PreparedStatement statement = connection.prepareStatement(insert_into_services_query);
			statement.setString(1, service_id);
			int row_count = statement.executeUpdate();
			if (row_count == 0) {
				System.out.println("Failed to insert service into the parent service table!");
				return;
			}
			String insert_into_maintenance_services_query = "insert into MAINTENANCE_SERVICE(service_id, service_names) values(?, ?)";
			statement = connection.prepareStatement(insert_into_maintenance_services_query);
			statement.setString(1, service_id);
			statement.setString(2, Services);
			row_count = statement.executeUpdate();
			if (row_count == 0) {
				System.out.println("Failed to insert service into the maintenance services table");
				return;
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void loadStoreInformationFromFile() {
		ArrayList<Map<String, String>> stores = DataReader.readeStoreInfoFromFile();
		// System.out.println("The total number of stores found : " + stores.size());
		for (Map<String, String> store : stores) {
			Integer is_open_saturday;
			if (store.get(DataReader.kStoreInfoFields[3]) == "Y")
				is_open_saturday = 1;
			else
				is_open_saturday = 0;
			insertStoreData(
					store.get(DataReader.kStoreInfoFields[0]),
					store.get(DataReader.kStoreInfoFields[1]),
					store.get(DataReader.kStoreInfoFields[4]),
					Double.parseDouble(store.get(DataReader.kStoreInfoFields[5])),
					Double.parseDouble(store.get(DataReader.kStoreInfoFields[6])),
					is_open_saturday);
		}
	}

	private void insertIndividualService(String service_name, String service_id) {
		// System.out.println("Service ID is: " + service_id);
		try {
			Connection connection = ConnectionManager.getDatabaseConnection();
			// 1. insert into the parent "SERVICES" table.
			{
				String insert_into_services_query = "insert into SERVICES(service_id) values(?)";
				PreparedStatement statement = connection.prepareStatement(insert_into_services_query);
				statement.setString(1, service_id);
				int row_count = statement.executeUpdate();

				if (row_count == 0) {
					System.out.println("Failed to insert service into the parent service table!");
					return;
				}
			}
			// 2. insert into "INDIVIDUAL_SERVICES" table.
			{
				String insert_into_individual_services_query = "insert into INDIVIDUAL_SERVICES(service_name, service_id) values(?, ?)";
				PreparedStatement statement = connection.prepareStatement(insert_into_individual_services_query);
				statement.setString(1, service_name);
				statement.setString(2, service_id);
				int row_count = statement.executeUpdate();
				if (row_count == 0) {
					System.out.println("Failed to insert service into the individual services table");
					return;
				}
			}
		} catch (SQLException e) {
			Utils.printSeperator();
			System.out.println("ERROR while trying to insert individual service, details : " + e.getMessage());
		}
	}

	private void insertServiceDuration(String manufacturer, String service_id, Integer duration) {

	}

	/**
	 * Adds a new store by querying the user for values and then calling
	 * addNewStoreImpl with those values.
	 */
	private void addNewStore() {
		Scanner scanner = Utils.getScanner();

		Utils.printSeperator();

		System.out.println("Enter store id : ");
		String store_id = scanner.nextLine();

		System.out.println("Enter store address : ");
		String store_address = scanner.nextLine();

		System.out.println("Enter store telephone number : ");
		String telephone_number = scanner.nextLine();

		System.out.println("Enter min wage for mechanics : ");
		double min_wage = scanner.nextDouble();

		System.out.println("Enter max wage for mechanics : ");
		double max_wage = scanner.nextDouble();

		scanner.nextLine();
		Map<User.EmployeeFields, String> manager_info = User.getEmployeeInfo(store_id, "manager");

		System.out.println("Enter if the store is open on Saturday (0 or 1) : ");
		Integer saturday_status = scanner.nextInt();

		addNewStoreImpl(store_id, store_address, telephone_number, manager_info, min_wage, max_wage, saturday_status);

		insertZeroCostToAllServices(store_id);
		
		System.out.println("New store added successfully!");
		Utils.printSeperator();
	}

	/**
	 * Creates a new store with the given info. Automatically creates a Manager
	 * account for the store based on the data in mangerInfo
	 * 
	 * @param storeID     Store's unique ID, auto-generated if not given
	 * @param address     Store's address
	 * @param managerInfo Map<String, String> containing the Manager's info,
	 *                    including firstName, lastName, userName, password, salary,
	 *                    and employeeId.
	 * @param minWage     The minimum hourly wage for mechanics
	 * @param maxWage     The maximum hourly wage for mechanics
	 */
	private void addNewStoreImpl(String storeID, String address, String telephone,
			Map<User.EmployeeFields, String> managerInfo, double minWage, double maxWage, Integer saturday_status) {

		String managerId = managerInfo.get(User.EmployeeFields.EMPLOYEE_ID);
		String managerFirstName = managerInfo.get(User.EmployeeFields.FIRST_NAME);
		String managerLastName = managerInfo.get(User.EmployeeFields.LAST_NAME);
		String managerAddress = managerInfo.get(User.EmployeeFields.ADDRESS);
		String role = managerInfo.get(User.EmployeeFields.ROLE);
		double salary = Double.parseDouble(managerInfo.get(User.EmployeeFields.SALARY));
		ProjMain.getInstance().getConnectionManager();

		// Insert into service center table.
		insertStoreData(storeID, address, telephone, minWage, maxWage, saturday_status);

		// Attempts to establish a connection
		Connection con = ConnectionManager.getDatabaseConnection();

		Statement stmt = null;
		try {
			stmt = con.createStatement();
		} catch (SQLException e) {
			System.out.println("Couldn't create statement");
			e.printStackTrace();
		}

		String credentialsInsert = String.format("insert into CREDENTIALS values ('%s','%s','%s','%s')", managerId,
				managerFirstName, managerLastName, "employees");
		String managerInsert = String.format("insert into EMPLOYEES values('%s','%s','%s','%s','%s',%f,'%s')",
				managerId, managerFirstName, managerLastName, managerAddress, role, salary, storeID);
		try {
			stmt.executeUpdate(credentialsInsert);
			stmt.executeUpdate(managerInsert);
		} catch (SQLException e) {
			System.out.println("Manager value insertion failed");
			e.printStackTrace();
		}

		return;
	}

	private ArrayList<String> getServices() {
		ArrayList<String> services = new ArrayList<String>();
		try {
			Connection conn = ConnectionManager.getDatabaseConnection();
			String query = "select service_id from SERVICES";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				services.add(rs.getString(1));
			}
		} catch (Exception e) {
			System.out.println("Error in getting list of services: " + e.getMessage());
			e.printStackTrace();
		}
		return services;
	}

	public void insertServiceCostUtil(String service_id, int amount, String manufacturerName,
			String service_center_id) {
		try {
			Connection conn = ConnectionManager.getDatabaseConnection();
			String query = String.format("insert into SERVICE_COST values('%s', %d, '%s', '%s')", service_id, amount,
					manufacturerName, service_center_id);
			Statement st = conn.createStatement();
			st.execute(query);
		} catch (Exception e) {
			System.out.println("Error in inserting service cost table" + e.getMessage());
			e.printStackTrace();
		}
	}

	private void insertZeroCostToAllServices(String service_center_id) {
		ArrayList<String> mans = getManufacturers();
		ArrayList<String> services = getServices();
		for (String man : mans) {
			for (String service : services) {
				insertServiceCostUtil(service, 0, man, service_center_id);
			}
		}
	}

	private void insertStoreData(String service_center_id, String address, String telephone, Double min_wage,
			Double max_wage, Integer saturday_status) {
		try {
			Connection connection = ConnectionManager.getDatabaseConnection();
			String store_insertion_query = "insert into SERVICE_CENTER(service_center_id, address, telephone, min_wage, max_wage, saturday) values(?,?,?,?,?,?)";
			PreparedStatement statement = connection.prepareStatement(store_insertion_query);
			statement.setString(1, service_center_id);
			statement.setString(2, address);
			statement.setString(3, telephone);
			statement.setDouble(4, min_wage);
			statement.setDouble(5, max_wage);
			statement.setInt(6, saturday_status);
			// System.out.println("preparedStmt" + statement.toString());
			int row_count = statement.executeUpdate();
			if (row_count == 0) {
				throw new SQLException("Failed to insert store details!");
			}
		} catch (SQLException e) {
			Utils.printSeperator();
			System.out.println("ERROR while inserting store data, details : " + e.getMessage());
		}
	}

	/**
	 * Adds an individual service, which is assigned a service category when added
	 */
	private void addNewService() {
		Utils.printSeperator();

		Scanner scanner = Utils.getScanner();

		System.out.println("Enter service id : ");
		double service_id = scanner.nextDouble();

		System.out.println("Enter service category : ");
		String service_category = scanner.nextLine();

		System.out.println("Enter service name : ");
		String service_name = scanner.nextLine();

		System.out.println("Enter service manufacturer name : ");
		String service_manufacturer = scanner.nextLine();

		System.out.println("Enter service duration : ");
		double service_duration = scanner.nextDouble();

		try {
			addNewServiceImpl(service_id, service_category, service_name, service_manufacturer, service_duration);
		} catch (Exception e) {
			System.out.println("Error occured while processing the service details, please try again. Details : "
					+ e.getMessage());
			addNewService();
		}
	}

	/**
	 * Creates a new service with the given information.
	 * 
	 * @param service_category Service category
	 * @param service_name     Service name
	 * @param service_duration Service duration
	 * @throws Exception
	 */
	private void addNewServiceImpl(double service_id, String service_category, String service_name, String manufacturer,
			double service_duration) throws Exception {

		ProjMain.getInstance().getConnectionManager();

		Connection con = ConnectionManager.getDatabaseConnection();
		Statement stmt = con.createStatement();

		String serviceInsert = "";

		// TODO Finish this
		if (service_category == "maintenance") {
			serviceInsert = "INSERT INTO MAINTENANCE_SERVICE VALUES('" + service_id + "', '" + service_name + "', '"
					+ service_id + "')";
		} else {
			serviceInsert = "INSERT INTO INDIVIDUAL_SERVICES VALUES('" + service_name + "', '" + service_id + "')";
		}
		// Inserting data in database

		stmt.executeUpdate(serviceInsert);
		ArrayList<String> scs = getStores();
		ArrayList<String> mans = getManufacturers();
		for (String man : mans) {
			String timeInsert = "INSERT INTO SERVICE_TIME VALUES('" + man + "', '" + service_id + "', "
					+ service_duration + ")";
			stmt.executeUpdate(timeInsert);
			for (String service_center_id : scs) {
				String costInsert = "insert into SERVICE_COST values('" + service_id + "', 0, '" + man + "', '"
						+ service_center_id + "')";
				stmt.executeUpdate(costInsert);
			}
		}
		ConnectionManager.closeDatabaseConnection();

		// TODO: Implement service handling.
	}

	public void returnToMainMenu() {
		ProjMain.getInstance().displayMainMenu();
		return;
	}

	public static void handleAdminPageSelection() {
		// This is a singleton so the re-entry scenario is handled below.
		if (__instance == null) {
			__instance = new Admin(UserType.ADMIN);
		}
		__instance.displayMenuForUser();
	}
};