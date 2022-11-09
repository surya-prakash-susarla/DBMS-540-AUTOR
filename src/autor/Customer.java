package autor;

import autor.User;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Customer extends User {

	/**
	 * Default constructor for Customer
	 * 
	 * @param user_type the Customer's type, calls User super constructor
	 */
	public String cid, firstName, lastName, service_center_id, address, email;
	public int status;

	public Customer(UserType user_type, HashMap<String, String> userDetMap) {
		super(user_type);
		this.email = userDetMap.get("EMAILADDRESS");
		this.address = userDetMap.get("ADDRESS");
		this.cid = userDetMap.get("CID");
		this.firstName = userDetMap.get("FIRSTNAME");
		this.lastName = userDetMap.get("LASTNAME");
		this.service_center_id = userDetMap.get("SERVICE_CENTER_ID");
		this.status = Integer.parseInt(userDetMap.get("STATUS"));
	}

	@Override
	public void displayMenuForUser() {
		HashMap<Integer, String> options = new HashMap<>();
		options.put(1, "View Profile");
		options.put(2, "Add car to profile");
		options.put(3, "Delete car from profile");
		options.put(4, "View service history");
		options.put(5, "Schedule service");
		options.put(6, "View invoice details");
		options.put(7, "Pay invoices");
        options.put(8, "Back to main menu");
		while (true) {
			int selection = Utils.getInputFromMenu(options);
			System.out.println("Selected option : " + selection);
			switch (selection) {
				case 1:
					viewProfile();
					break;
				case 2:
					addCar();
					break;
				case 3:
					deleteCar();
					break;
				case 4:
					viewServiceHistory();
					break;
				case 5:
					scheduleServiceWrapper();
					break;
				case 6:
					viewInvoiceDetails();
					break;
				case 7:
					payInvoicesWrapper();
					break;
                case 8:
                    ProjMain.getInstance().displayMainMenu();
                    return;
				default:
					try {
						System.out.println("Going back to main menu");
						ProjMain.getInstance().displayMainMenu();
					} catch (Exception e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
					break;
			}
		}
	}

	private void payInvoicesWrapper() {
		Scanner reader = Utils.getScanner();
		String invoicenumber;
		System.out.println("Enter the invoice number:");
		invoicenumber = reader.nextLine();
		this.payInvoices(invoicenumber);
	}

	private void scheduleServiceWrapper() {
		Scanner reader = Utils.getScanner();
		String vin;
		System.out.println("Enter the vin of your car: ");
		vin = reader.nextLine();

		System.out.println("Here are the services we offer: ");
		ArrayList<ServiceInfo> services = Utils.getListOfServices();
		for ( ServiceInfo service : services ) {
			System.out.println(service.service_id + " ------- " + service.service_name);
		}

		System.out.println("Enter the service codes separated by spaces:");
		String serviceCodesString = reader.nextLine();

		Integer input = Utils.getIntegerInput("Do you want to perform maintenance for the chosen car (0 or 1)?");

		if ( input == 1 ) {
			serviceCodesString = serviceCodesString + " " + Utils.getNextMaintenanceID(vin);
			Utils.updateMaintenanceOffsetForCar(vin);
		}

		String[] serviceCodes = serviceCodesString.split(" ");

		try {
			this.scheduleService(serviceCodes, vin);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private void deleteCar() {
		System.out.println(Utils.getSeperator());
		Scanner reader = Utils.getScanner();
		String vin;
		System.out.println("Enter the vin of your car: ");
		vin = reader.nextLine();
		this.deleteCarFromProfile(vin);
	}

	/**
	 * 
	 */
	private void addCar() {
		System.out.println(Utils.getSeperator());
		Scanner reader = Utils.getScanner();
		String vin, manufacturer;
		int mileage;
		System.out.println("Enter the vin of your car: ");
		vin = reader.nextLine();
		System.out.println("Enter the manufacturer of your car: ");
		manufacturer = reader.nextLine();
		System.out.println("Enter the mileage of your car: ");
		mileage = Integer.parseInt(reader.nextLine());
		this.addCarToProfile(vin, manufacturer, mileage);
		try{Connection conn = ConnectionManager.getDatabaseConnection();
		PreparedStatement st = conn.prepareStatement("update CUSTOMER set status = 1 where cid = ?");
		st.setString(1,this.cid);
		st.executeUpdate();}
		catch(Exception e){
			System.out.println("Error changing status"+e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Displays the Customer's profile info
	 */
	public void viewProfile() {
		System.out.println(Utils.getSeperator());
		System.out.println("*****    User Details:   *******");
		System.out.println(String.format("CID: %s", this.cid));
		System.out.println(String.format("First Name: %s", this.firstName));
		System.out.println(String.format("Status: %d", this.status));
		System.out.println(String.format("Last Name: %s", this.lastName));
		System.out.println(String.format("Address: %s", this.address));
		System.out.println(String.format("Email Address: %s", this.email));

		System.out.println(String.format("Service center ID: %s", this.service_center_id));
	
	
		try {
			Connection connection = ConnectionManager.getDatabaseConnection();
			String add_new_customer_query = "Select * from CAR C Where C.cid = ?";
			PreparedStatement statement = connection.prepareStatement(add_new_customer_query);

			statement.setString(1, this.cid);
			ResultSet results = statement.executeQuery();
			System.out.println(
					"vin--cid--manufacturerName--mileage--year--lastmaintenance");
			while (results.next()) {
				String vin = results.getString("vin");
				String cid = results.getString("cid");
				String manufacturerName = results.getString("manufacturerName");
				int mileage = results.getInt("mileage");
				int year = results.getInt("year");
				int lastmaintenance = results.getInt("lastmaintenance");
				System.out.println("\n" + vin + " " + cid + " " + manufacturerName + " " + mileage + " " + year + " " + lastmaintenance);

			}
		} catch (SQLException e) {
			Utils.printSeperator();
			System.out.println("ERROR while finding cars, details : " + e.getMessage());
		}
	
	
	}

	/**
	 * Adds a Car with the given info to this Customer's profile
	 * 
	 * @param vin          Globally unique 8 character string
	 * @param manufacturer Car's manufacturer
	 * @param mileage      Car's mileage
	 */
	public void addCarToProfile(String vin, String manufacturer, Integer mileage) {
		Connection conn = ConnectionManager.getDatabaseConnection();
		Statement st;
		// TODO: year? what is it?? change lsatmaintenance according to spec
		try {
			st = conn.createStatement();
			String query = String.format("insert into CAR values('%s', '%s', '%s',%d,'%s',%d)",
					vin, cid, manufacturer, mileage,
					"1999", 0);
			st.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Removes the given car from this Customer's profile
	 * 
	 * @param vin Globally unique 8 character string
	 */
	public void deleteCarFromProfile(String vin) {
		Connection conn = ConnectionManager.getDatabaseConnection();
		Statement st;
		try {
			st = conn.createStatement();
			String query = String.format("delete from CAR where vin = '%s'", vin);
			st.executeUpdate(query);
			{
				query = String.format("select * from CAR where cid = ?");
				PreparedStatement pst = conn.prepareStatement(query);
				pst.setString(1,this.cid);
				ResultSet rs = pst.executeQuery();
				if(rs.next() == false){
					query = "update CUSTOMER set status = 0 where cid = ?";
					pst = conn.prepareStatement(query);
					pst.setString(1,this.cid);
					pst.executeUpdate();
				}
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Displays the service history of the given car
	 * 
	 * @param vin Globally unique 8 character String
	 */
	public void viewServiceHistory() {
		System.out.print("Enter the vin of your car: ");
		Scanner reader = Utils.getScanner();
		String vin = reader.nextLine();
		String query = "select * from INVOICE where vin = ? and cid = ?";
		Connection conn = ConnectionManager.getDatabaseConnection();
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, vin);
			stmt.setString(2, this.cid);
			ResultSet rs = stmt.executeQuery();
			int idx = 1;
			while (rs.next()) {
				System.out.println(Utils.getSeperator());
				String message = "SERVICE: " + Integer.toString(idx) + "\n";
				message += "SERVICES TAKEN: " + rs.getString(2) + "\n";
				message += "INVOICE DATE: " + rs.getString(3) + "\n";
				message += "INVOICE ID: " + rs.getString(4) + "\n";
				message += "MECHANIC: " + rs.getString(5) + "\n";
				message += "STATUS: " + rs.getString(6).toString() + "\n";
				message += "COST: " + rs.getString(7).toString() + "\n";
				message += "VIN: " + rs.getString(8) + "\n";
				System.out.println(message);
				idx++;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Schedules Services from the list of serviceCodes on the given car
	 * 
	 * @param serviceCodes List of Services to perform on the car
	 * @param vin          Globally unique 8 character String
	 * @throws Exception
	 */
	public void scheduleService(String[] serviceCodes, String vin) throws Exception {
		Connection conn = ConnectionManager.getDatabaseConnection();
		Statement st;
		List<String> serviceCodesList = Arrays.asList(serviceCodes);
		String services = serviceCodesList.stream().map(e -> String.format("'%s'", e)).collect(Collectors.joining(","));
		try {
			st = conn.createStatement();
			String query = String.format("select manufacturerName from CAR where vin = '%s'", vin);
			ResultSet rs = st.executeQuery(query);
			if (rs.next() == false) {
				throw new Exception(String.format("Car with vin = %s does not exist", vin));
			}
			String manufacturer = rs.getString(1);
			query = String.format(
					"select sum(time) from SERVICE_TIME where manufacturerName = '%s' and service_id in (%s)",
					manufacturer, services);
			// for (String service_code : serviceCodes) {
			// query = String.format("select time from SERVICE_TIME where manufacturer =
			// '%s' and service_id = '%s'",
			// manufacturer, service_code);
			// rs = st.executeQuery(query);
			// if (rs.next() == false) {
			// throw new Exception(
			// String.format("SERVICE_TIME table error: no time for man = %s and serviceid =
			// %s",
			// manufacturer, service_code));
			// }
			// duration += rs.getInt(0);
			// }
			rs = st.executeQuery(query);
			if (!rs.next()) {
				throw new Exception("SUM(TIME) could not be done... no rows");
			}
			int duration = rs.getInt(1);
			ArrayList<ScheduleInfo> availableList = ScheduleUtil.getAvailabilityForServiceCenter(this.service_center_id,
					duration);
			if (availableList.size() == 0) {
				System.out.println("Sorry, no slots right now!!! Please come later");
				return;
			}
			query = String.format(
					"select sum(amount) from SERVICE_COST where service_id in (%s) and manufacturerName = ? and service_center_id = ?",
					services);
			PreparedStatement cost_stmt = conn.prepareStatement(query);
			cost_stmt.setString(1, manufacturer);
			cost_stmt.setString(2, this.service_center_id);
			rs = cost_stmt.executeQuery();
			rs.next();
			float total_cost = rs.getFloat(1);
			String new_invoice_id = Utils.generateID();
			System.out.println("Pick an available slot: ");
			ScheduleInfo.printSlots(availableList);
			Scanner reader = Utils.getScanner();
			int index = reader.nextInt();
			reader.nextLine();
			index--;
			ScheduleInfo assignedSlot = availableList.get(index);
			query = "insert into INVOICE values (?,?,?,?,?,?,?,?)";
			PreparedStatement statement = conn.prepareStatement(query);
			statement.setString(1, this.cid);
			statement.setString(2, services);
			String dateEntry = "Week: " + Integer.toString(assignedSlot.week_id) + "-";
			dateEntry += "Day: " + Integer.toString(assignedSlot.day_id) + "-";
			dateEntry += "from: " + Integer.toString(assignedSlot.slot_start) + "-";
			dateEntry += "to: " + Integer.toString(assignedSlot.slot_end);
			statement.setString(3, dateEntry);
			statement.setString(4, new_invoice_id);
			statement.setString(5, assignedSlot.mechanic_id);
			statement.setInt(6, 0);
			statement.setFloat(7, total_cost);
			statement.setString(8, vin);
			statement.executeUpdate();
			ScheduleUtil.assignSlotToMechanic(assignedSlot, new_invoice_id);
			System.out.println("Done assigning slots!!!");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Displays a numbered list of invoices
	 */
	public void viewInvoiceDetails() {
		// Display invoices whether paid or not
		String query = "select * from INVOICE where cid = ?";
		Connection conn = ConnectionManager.getDatabaseConnection();
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, this.cid);
			ResultSet rs = stmt.executeQuery();
			int idx = 1;
			while (rs.next()) {
				System.out.println(Utils.getSeperator());
				String message = "SERVICE: " + Integer.toString(idx) + "\n";
				message += "SERVICES TAKEN: " + rs.getString(2) + "\n";
				message += "INVOICE DATE: " + rs.getString(3) + "\n";
				message += "INVOICE ID: " + rs.getString(4) + "\n";
				message += "MECHANIC: " + rs.getString(5) + "\n";
				message += "STATUS: " + rs.getString(6).toString() + "\n";
				message += "COST: " + rs.getString(7).toString() + "\n";
				message += "VIN: " + rs.getString(8) + "\n";
				System.out.println(message);
				idx++;
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Pays for the invoice given by invoiceId
	 * 
	 * @param invoiceId id of the invoice to pay
	 */
	public void payInvoices(String invoiceId) {
		Connection conn = ConnectionManager.getDatabaseConnection();
		Statement st;
		try {
			st = conn.createStatement();
			String query = String.format("update INVOICE set status = 1 where invoice_id = '%s'", invoiceId);
			st.executeUpdate(query);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}
}
