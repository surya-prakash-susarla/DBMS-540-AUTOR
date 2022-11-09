package autor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Utils {
    static Scanner input_scanner;

    public static String generateID() {
        return UUID.randomUUID().toString();
    }

    public static String getSeperator() {
        return "*".repeat(20);
    }

    public static void printSeperator() {
        System.out.println(getSeperator());
        System.out.println(getSeperator());
        return;
    }

    public static void printInvalidSelectionMessage() {
        System.out.println("Invalid selection, please try again.");
        return;
    }

    public static int getInputFromMenu(Map<Integer, String> options) {
        System.out.println(getSeperator());
        for (Map.Entry<Integer, String> opt : options.entrySet()) {
            System.out.println(opt.getKey() + ": " + opt.getValue());
        }

        Scanner reader = getScanner();
        System.out.println("Enter an option : ");
        int n = reader.nextInt();
        reader.nextLine();
        // TODO: Sanitize input.

        return n;
    }

    public static Scanner getScanner() {
        if (input_scanner == null) {
            input_scanner = new Scanner(System.in);
        }
        return input_scanner;
    }

    public static void closeScanner() {
        System.out.println("WARNING - CLOSING SCANNER!");
        input_scanner.close();
    }

    public static Integer getIntegerInput(String query) {
        System.out.println(query);
        Integer input = getScanner().nextInt();
        getScanner().nextLine();
        return input;
    }

    public static String getStringInput(String query) {
        System.out.println(query);
        String input = getScanner().nextLine();
        return input;
    }

    private static void q_service_cost_most_customers() {
        try {
            Connection connection = ConnectionManager.getDatabaseConnection();
            String query = "select service_center_id from Customer group by(service_center_id) having count(*) = (select max(count(*)) from customer group by service_center_id)";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                System.out.println("The service center is : " + results.getString(1));
            }

        } catch (SQLException e) {
            System.out.println("ERROR, details : " + e.getMessage());
        }
        return;
    }

    private static void q_avg_price_evaporator_repair() {
        try {
            Connection connection = ConnectionManager.getDatabaseConnection();
            String query = "select avg(amount) from service_cost where service_id=112 and manufacturerName='Honda'";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                System.out.println("The average price : " + result.getDouble(1));
            }
        } catch (SQLException e) {
            System.out.println("ERROR, details : " + e.getMessage());
        }
        return;
    }

    private static void q_unpaid_customer_30001() {
        String query = "select c.cid,c.firstName,c.lastName from customer c, invoice i where i.cid = c.cid and c.service_center_id = '30003' and i.status = 1";
        try {
            Connection connection = ConnectionManager.getDatabaseConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                System.out.println(" Customer ID : " + result.getString(1) + " Customer First Name: "
                        + result.getString(2) + " Customer Last Name: " + result.getString(3));
            }
        } catch (SQLException e) {
            System.out.println("ERROR, details : " + e.getMessage());
        }
        return;

    }

    private static void q_list_service_both_maintenance_and_repair() {
        String query = "select service_name from individual_services i where exists (select * from maintenance_service m where m.service_names like '%'||i.service_name||'%')";
        System.out.println("Services which are common are: ");
        try {
            Connection connection = ConnectionManager.getDatabaseConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                System.out.println(" Service Name : " + result.getString(1));
            }
        } catch (SQLException e) {
            System.out.println("ERROR, details : " + e.getMessage());
        }
        return;
    }

    private static void q_difference_between_belt_replacement_schedule_at_30001_30002() {
        Connection connection = ConnectionManager.getDatabaseConnection();
        String query = "select temp1.cost - temp2.cost from (select sum(amount) as cost from SERVICE_COST where (service_id = '113' or service_id = '101') and service_center_id = '30001' and manufacturerName = 'Toyota') temp1, (select sum(amount) as cost from SERVICE_COST where (service_id = '113' or service_id = '101') and service_center_id = '30002' and manufacturerName = 'Toyota') temp2";

        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                System.out.println("The difference in price : " + result.getDouble(1));
            }
        } catch (Exception e) {
            System.out.println("Error in query 5" + e.getMessage());
            e.printStackTrace();
        }

    }

    private static void q_next_eligible_maintenance_for_vin() {
        try {
            Connection connection = ConnectionManager.getDatabaseConnection();
            String query = "select service_id, service_names from MAINTENANCE_SERVICE, (select MOD(lastmaintenance+1,temp.ct) + 113 as sid from CAR,(select count(*) as ct from MAINTENANCE_SERVICE) temp where vin = '34KLE19D') temp2 where service_id = temp2.sid;";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                System.out.println(results.getString(1) + "," + results.getString(2));
            }
        } catch (SQLException e) {
            System.out.println("ERROR, details : " + e);
        }
        return;
    }

    public static void handleExamQueries() {
        Map<Integer, String> options = new HashMap<Integer, String>();
        options.put(1, "Which service center has most number of customers?");
        options.put(2, "What is the average price of an Evaporator Repair for Hondas across all service centers?");
        options.put(3, "Which customers have unpaid in Service Center 30003?");
        options.put(4, "List all services that are maintained as both repair and maintenance services globally");
        options.put(5,
                "What is the difference between the cost of doing the belt replacement + schedule A on Toyota at center 30001 vs 30002?");
        options.put(6, "What is the next eligible maintenance schedule for the car with VIN 34KLE19D?");
        Integer selection = getInputFromMenu(options);
        switch (selection) {
            case 1: {
                q_service_cost_most_customers();
                return;
            }
            case 2: {
                q_avg_price_evaporator_repair();
                return;
            }
            case 3: {
                q_unpaid_customer_30001();
                return;
            }
            case 4: {
                q_list_service_both_maintenance_and_repair();
                return;
            }
            case 5: {
                q_difference_between_belt_replacement_schedule_at_30001_30002();
                return;
            }
            case 6: {
                q_next_eligible_maintenance_for_vin();
                return;
            }
            default: {
                System.out.println("Invalid selection for exam query, try again!");
                return;
            }
        }
    }

    public static ArrayList<ServiceInfo> getListOfServices() {
        ArrayList<ServiceInfo> services = new ArrayList<ServiceInfo>();
        try {
            Connection connection = ConnectionManager.getDatabaseConnection();
            String query = "select service_id, service_name from individual_services";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                ServiceInfo info = new ServiceInfo();
                info.service_id = results.getString(1);
                info.service_name = results.getString(2);
                services.add(info);
            }
        } catch (SQLException e) {
            System.out.println("ERROR while getting list of services, details : " + e.getMessage());
        }
        return services;
    }

    public static String getNextMaintenanceID(String vin) {
        try {
            Connection connection = ConnectionManager.getDatabaseConnection();
            String query = "select lastmaintenance from car where vin=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, vin);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                Integer next_id = 113 + Integer.parseInt(results.getString(1));
                return next_id.toString();
            }
        } catch (SQLException e) {
            System.out.println("ERROR while getting the next maintenance service, details : " + e.getMessage());
        }
        return null;
    }

    public static void updateMaintenanceOffsetForCar(String vin) {
        try {
            Connection connection = ConnectionManager.getDatabaseConnection();
            Integer offset = 0;
            {
                String query = "select MOD(lastmaintenance+1,temp.ct) as sid from CAR,(select count(*) as ct from MAINTENANCE_SERVICE) temp where vin = ?";

                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, vin);

                ResultSet results = statement.executeQuery();
                if (results.next()) {
                    offset = results.getInt(1);
                }
            }
            {
                String query = "update car set lastmaintenance=? where vin=?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setInt(1, offset);
                statement.setString(2, vin);
                int row_count = statement.executeUpdate();
                if (row_count == 0) {
                    System.out.println("Failed to update maintenance offset!");
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR while trying to update maintenance offset, details : " + e.getMessage());
        }
    }

};
