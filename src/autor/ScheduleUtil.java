package autor;

import java.sql.*;
import java.util.*;
import java.util.stream.*;

public class ScheduleUtil {

  static int MAX_SLOTS_PER_DAY = 10;
  static int MAX_WEEKLY_WORKING_HOURS = 50;
  static String TIME_OFF_INVOICE_ID = "-1";

  static enum SWAP_STATUS {
    REQUESTED,
    ACCEPTED,
    REJECTED
  };

  private static boolean worksSaturday(String service_center_id) {
    boolean sat = false;
    try {
      Connection con = ConnectionManager.getDatabaseConnection();
      String query = "select saturday from SERVICE_CENTER where service_center_id = ?";
      PreparedStatement st = con.prepareStatement(query);
      st.setString(1, service_center_id);
      ResultSet rs = st.executeQuery();
      if (rs.next() == false) {
        throw new Exception("Error in getting saturday, no rows present");
      }
      sat = (rs.getInt("saturday") == 1);
    } catch (Exception e) {
      System.out.println("Error getting saturday status for service center: " + e.getMessage());
      e.printStackTrace();
    }
    return sat;
  }

  public static ArrayList<ScheduleInfo> getAvailabilityForServiceCenter(String service_center_id, Integer duration) {

    ArrayList<String> mechanics = getMechanicsForServiceCenter(service_center_id);

    int[] weeks = IntStream.rangeClosed(1, 4).toArray();

    // TODO: Check if store works on saturday before adding 6.
    int weekLength = 5;
    if (worksSaturday(service_center_id)) {
      weekLength++;
    }
    int[] days = IntStream.rangeClosed(1, weekLength).toArray();
    int[] slots = IntStream.rangeClosed(1, MAX_SLOTS_PER_DAY - duration).toArray();

    ArrayList<ScheduleInfo> available_slots = new ArrayList<ScheduleInfo>();
    for (String mechanic : mechanics) {
      for (Integer week : weeks) {
        for (Integer day : days) {
          for (Integer slot : slots) {
            if (isMechanicAvailableForGivenDuration(service_center_id, mechanic, week, day, slot, slot + duration)) {
              ScheduleInfo info = new ScheduleInfo();
              info.service_center_id = service_center_id;
              info.mechanic_id = mechanic;
              info.week_id = week;
              info.day_id = day;
              info.slot_start = slot;
              info.slot_end = slot + duration;
              available_slots.add(info);
            }
          }
        }
      }
    }

    return available_slots;
  }

  // TODO: REFACTOR WITH NEWER QUERY USING SCHEDULE_INFO BELOW.
  private static boolean isMechanicAvailableForGivenDuration(String service_center_id, String mechanic_id,
      Integer week_id, Integer day_id, Integer slot_start, Integer slot_end) {
    try {
      // TODO: Check for <50hr & time-off (if any) related constraints.
      String query_to_find_if_mechanic_is_working = "select s.emp_id from SCHEDULE s where s.service_center_id=? and s.emp_id=? and s.week=? and s.day=? and ((s.slot_start between ? and ?) or (s.slot_end between ? and ?))";
      Connection connection = ConnectionManager.getDatabaseConnection();
      PreparedStatement statement = connection.prepareStatement(query_to_find_if_mechanic_is_working);
      statement.setString(1, service_center_id);
      statement.setString(2, mechanic_id);
      statement.setInt(3, week_id);
      statement.setInt(4, day_id);
      statement.setInt(5, slot_start);
      statement.setInt(6, slot_end);
      statement.setInt(7, slot_start);
      statement.setInt(8, slot_end);

      ResultSet results = statement.executeQuery();

      // There should be now rows since existence would indicate assigned work during
      // the given period.
      boolean answer = results.next();
      results.close();
      return !answer;
    } catch (SQLException exception) {
      Utils.printSeperator();
      System.out
          .println("ERROR while checking if mechanic is working in a time slot, error : " + exception.getMessage());
    }

    return false;
  }

  private static ArrayList<String> getMechanicsForServiceCenter(String service_center_id) {
    ArrayList<String> mechanics = new ArrayList<String>();
    try {
      String query_for_mechanics_in_service_center = "select employee_id from EMPLOYEES where service_center_id=?";
      Connection connection = ConnectionManager.getDatabaseConnection();
      PreparedStatement statement = connection.prepareStatement(query_for_mechanics_in_service_center);
      statement.setString(1, service_center_id);
      ResultSet results = statement.executeQuery();

      while (results.next()) {
        mechanics.add(results.getString(1));
      }

    } catch (SQLException e) {
      Utils.printSeperator();
      System.out.println("ERROR while finding mechanics associated with service center, details : " + e.getMessage());
    }

    return mechanics;
  }

  public static boolean isMechanicFreeForSlot(ScheduleInfo slot_info) {
    boolean result = false;
    try {
      Connection connection = ConnectionManager.getDatabaseConnection();

      String is_current_slot_free_query = "select emp_id from SCHEDULE where service_center_id=? and week=? and day=? and emp_id = ? and not (slot_start between ? and ?) and not (slot_end between ? and ?)";
      PreparedStatement statement = connection.prepareStatement(is_current_slot_free_query);
      statement.setString(1, slot_info.service_center_id);
      statement.setInt(2, slot_info.week_id);
      statement.setInt(3, slot_info.day_id);
      statement.setString(4, slot_info.mechanic_id);
      statement.setInt(5, slot_info.slot_start);
      statement.setInt(6, slot_info.slot_end);
      statement.setInt(7, slot_info.slot_start);
      statement.setInt(8, slot_info.slot_end);
      ResultSet results = statement.executeQuery();
      result = !results.next();
    } catch (SQLException e) {
      Utils.printSeperator();
      System.out.println("ERROR while finding if mechanic is free for given slot, details : " + e.getMessage());
    }
    return result;
  }

  public static boolean isMechanicAvailableForSwap(ScheduleInfo source, ScheduleInfo target) {
    try {
      // 1. check if target slot is the perfectly occupied for other mechanic.
      // 2. check if current slot is also free for target mechanic (double-booking
      // scenario).
      // 3. check if overall hours are <50.
      // 4. check if time-off (covered implicitly).
      Connection connection = ConnectionManager.getDatabaseConnection();

      // 1.
      {
        String is_target_slot_available_query = "select emp_id from SCHEDULE where service_center_id=? and week=? and day=? and slot_start=? and slot_end=? and emp_id=?";
        PreparedStatement statement = connection.prepareStatement(is_target_slot_available_query);
        statement.setString(1, target.service_center_id);
        statement.setInt(2, target.week_id);
        statement.setInt(3, target.day_id);
        statement.setInt(4, target.slot_start);
        statement.setInt(5, target.slot_end);
        statement.setString(6, target.mechanic_id);
        ResultSet results = statement.executeQuery();

        System.out.println("service center : " + target.service_center_id);

        System.out.println("checking for mechanic : " + target.mechanic_id);
        System.out.println("size of mechanic string : " + target.mechanic_id.length());
        System.out.println("week : " + target.week_id);
        System.out.println("day : " + target.day_id);
        System.out.println("slot start : " + target.slot_start);
        System.out.println("slot end : " + target.slot_end);

        if (results.next() == false) {
          System.out.println("The requested mechanic does not work on the slot provided!");
          return false;
        }
      }

      // 2 & 3
      {
        // The slot details match the source of the request but since we need to check
        // for availability of the target person we simply put their id instead while
        // retaining all the other fields.
        ScheduleInfo temp = new ScheduleInfo();
        temp.mechanic_id = target.mechanic_id;
        temp.service_center_id = source.service_center_id;
        temp.week_id = source.week_id;
        temp.day_id = source.day_id;
        temp.slot_start = source.slot_start;
        temp.slot_end = source.slot_end;
        if (!isMechanicFreeForSlot(temp)) {
          System.out.println("The mechanic is not free in the given slot");
          return false;
        }
      }

      // 4.
      {
        if (getAvailableTotalTimeForEmployee(target.service_center_id, target.mechanic_id, source.week_id)
            + (source.slot_end - source.slot_start + 1) > MAX_WEEKLY_WORKING_HOURS) {
          System.out.println("The requested mechanic will cross maximum hours");
          return false;
        }
      }

      return true;

    } catch (SQLException e) {
      Utils.printSeperator();
      System.out.println("ERROR while checking if mechanic is available for swap, details: " + e.getMessage());
    }

    return false;
  }

  public static Integer getAvailableTotalTimeForEmployee(String service_center_id, String mechanic_id,
      Integer week_id) {
    try {
      Connection connection = ConnectionManager.getDatabaseConnection();
      String get_total_working_hours_query = "select sum(temp.duration) from (select service_center_id, emp_id, week, (slot_end - slot_start + 1) as duration from SCHEDULE) temp where temp.service_center_id=? and temp.emp_id=? and temp.week=?";
      PreparedStatement statement = connection.prepareStatement(get_total_working_hours_query);
      statement.setString(1, service_center_id);
      statement.setString(2, mechanic_id);
      statement.setInt(3, week_id);
      ResultSet results = statement.executeQuery();
      results.next();
      Integer current_working_hours = results.getInt(1);
      return MAX_WEEKLY_WORKING_HOURS - current_working_hours;
    } catch (SQLException e) {
      Utils.printSeperator();
      System.out.println("ERROR while checking available hours for mechanic, details : " + e.getMessage());
    }
    return 0;
  }

  public static boolean doesMechanicHaveFreeTimeInWeek(String service_center_id, String mechanic_id,
      Integer week_id) {
    return getAvailableTotalTimeForEmployee(service_center_id, mechanic_id, week_id) > 0;
  }

  public static ArrayList<ScheduleInfo> getSlotsForMechanic(String mechanic_id) {
    ArrayList<ScheduleInfo> assigned_hours = new ArrayList<ScheduleInfo>();

    try {
      String service_center_id = getServiceCenterForEmployee(mechanic_id);

      Connection connection = ConnectionManager.getDatabaseConnection();

      int[] weeks = IntStream.rangeClosed(1, 4).toArray();
      // TODO: Check if store works on saturday before adding 6.
      int[] days = IntStream.rangeClosed(1, 6).toArray();

      for (int week : weeks) {
        for (int day : days) {
          String get_assigned_slots_for_mechanic_query = "select slot_start, slot_end from SCHEDULE where service_center_id=? and emp_id=? and week=? and day=?";
          PreparedStatement statement = connection.prepareStatement(get_assigned_slots_for_mechanic_query);
          statement.setString(1, service_center_id);
          statement.setString(2, mechanic_id);
          statement.setInt(3, week);
          statement.setInt(4, day);
          ResultSet results = statement.executeQuery();
          while (results.next()) {
            ScheduleInfo info = new ScheduleInfo();
            info.service_center_id = service_center_id;
            info.mechanic_id = mechanic_id;
            info.week_id = week;
            info.day_id = day;
            info.slot_start = results.getInt(1);
            info.slot_end = results.getInt(2);
            assigned_hours.add(info);
          }

        }
      }

    } catch (SQLException e) {
      Utils.printSeperator();
      System.out.println("ERROR while getting assigned slots for mechanic, details: " + e.getMessage());
    }

    return assigned_hours;
  }

  public static String getServiceCenterForEmployee(String mechanic_id) throws SQLException {
    try {
      Connection connection = ConnectionManager.getDatabaseConnection();
      String get_service_center_for_mechanic_query = "select service_center_id from EMPLOYEES where employee_id=?";
      PreparedStatement statement = connection.prepareStatement(get_service_center_for_mechanic_query);
      statement.setString(1, mechanic_id);
      ResultSet results = statement.executeQuery();
      results.next();
      return results.getString("service_center_id");
    } catch (SQLException e) {
      Utils.printSeperator();
      System.out.println("ERROR while looking up service center for employee. details: " + e.getMessage());
      throw e;
    }
  }

  public static boolean requestSwap(ScheduleInfo source, ScheduleInfo target) {
    boolean status = false;
    try {
      System.out.println("source id : " + source.mechanic_id);
      System.out.println("target id : " + target.mechanic_id);

      Connection connection = ConnectionManager.getDatabaseConnection();
      String insert_swap_query = "insert into swaps(source_emp_id, source_slot_week, source_slot_day, source_slot_start, source_slot_end, target_emp_id, target_slot_week, target_slot_day, target_slot_start, target_slot_end, status) values(?,?,?,?,?,?,?,?,?,?,?)";
      PreparedStatement statement = connection.prepareStatement(insert_swap_query);
      statement.setString(1, source.mechanic_id);
      statement.setInt(2, source.week_id);
      statement.setInt(3, source.day_id);
      statement.setInt(4, source.slot_start);
      statement.setInt(5, source.slot_end);
      statement.setString(6, target.mechanic_id);
      statement.setInt(7, target.week_id);
      statement.setInt(8, target.day_id);
      statement.setInt(9, target.slot_start);
      statement.setInt(10, target.slot_end);
      statement.setInt(11, 0);
      int modified_row_count = statement.executeUpdate();
      System.out.println("Succesfully recorded " + modified_row_count + " swap!");
      status = true;
    } catch (SQLException e) {
      Utils.printSeperator();
      System.out.println("ERROR while trying to add swap to table, details : " + e.getMessage());
    }
    return status;
  }

  public static ArrayList<SwapInfo> getSwapRequests(String mechanic_id) {
    ArrayList<SwapInfo> swaps = new ArrayList<>();
    try {
      Connection connection = ConnectionManager.getDatabaseConnection();
      String service_center_id = getServiceCenterForEmployee(mechanic_id);
      String get_slots_query = "select swap_id, source_emp_id, source_slot_week, source_slot_day, source_slot_start, source_slot_end, target_emp_id, target_slot_week, target_slot_day, target_slot_start, target_slot_end from SWAPS where target_emp_id=? and status=0";
      PreparedStatement statement = connection.prepareStatement(get_slots_query);
      statement.setString(1, mechanic_id);
      ResultSet results = statement.executeQuery();
      while (results.next()) {
        SwapInfo swap = new SwapInfo();
        swap.swap_id = results.getInt(1);
        swap.source_slot.service_center_id = service_center_id;
        swap.source_slot.mechanic_id = results.getString(2);
        swap.source_slot.week_id = results.getInt(3);
        swap.source_slot.day_id = results.getInt(4);
        swap.source_slot.slot_start = results.getInt(5);
        swap.source_slot.slot_end = results.getInt(6);
        swap.target_slot.service_center_id = service_center_id;
        swap.target_slot.mechanic_id = results.getString(7);
        swap.target_slot.week_id = results.getInt(8);
        swap.target_slot.day_id = results.getInt(9);
        swap.target_slot.slot_start = results.getInt(10);
        swap.target_slot.slot_end = results.getInt(11);
        swaps.add(swap);
      }
    } catch (SQLException e) {
      Utils.printSeperator();
      System.out.println("ERROR while displaying swaps for user, details : " + e.getMessage());
    }
    return swaps;
  }

  public static boolean assignSlotToMechanic(ScheduleInfo slot_info, String invoice_id) {
    boolean status = false;
    try {
      System.out.println("Trying to assing a slot to mechanic");
      Connection connection = ConnectionManager.getDatabaseConnection();
      String assign_slot_query = "insert into SCHEDULE(service_center_id, week, day, slot_start, slot_end, emp_id, invoice_id) values(?,?,?,?,?,?,?)";
      PreparedStatement statement = connection.prepareStatement(assign_slot_query);
      statement.setString(1, slot_info.service_center_id);
      statement.setInt(2, slot_info.week_id);
      statement.setInt(3, slot_info.day_id);
      statement.setInt(4, slot_info.slot_start);
      statement.setInt(5, slot_info.slot_end);
      statement.setString(6, slot_info.mechanic_id);
      statement.setString(7, invoice_id);
      int row_count = statement.executeUpdate();
      System.out.println("Row count: " + row_count);
      return row_count == 1;
    } catch (SQLException e) {
      Utils.printSeperator();
      System.out.println("ERROR while trying to schedule slot for employee, details : " + e.getMessage());
    }

    return status;
  }

  public static boolean shiftSlotToMechanic(ScheduleInfo slot, String target_mechanic) {
    boolean status = false;
    try {
      Connection connection = ConnectionManager.getDatabaseConnection();
      String shift_slot_query = "Update SCHEDULE set emp_id=? where service_center_id=? and week=? and day=? and slot_start=? and slot_end=?";
      PreparedStatement statement = connection.prepareStatement(shift_slot_query);
      statement.setString(1, target_mechanic);
      statement.setString(2, slot.service_center_id);
      statement.setInt(3, slot.week_id);
      statement.setInt(4, slot.day_id);
      statement.setInt(5, slot.slot_start);
      statement.setInt(6, slot.slot_end);
      int row_count = statement.executeUpdate();
      return row_count == 1;
    } catch (SQLException e) {
      Utils.printSeperator();
      System.out.println("ERROR while trying to shift slot to mechanic, details : " + e.getMessage());
    }

    return status;
  }

  public static boolean acceptSwap(SwapInfo swap) {
    boolean status = false;
    try {
      Connection connection = ConnectionManager.getDatabaseConnection();
      {
        // Update the schedules table with the updated info.
        status = shiftSlotToMechanic(swap.source_slot, swap.target_slot.mechanic_id);
        if (status) {
          status = shiftSlotToMechanic(swap.target_slot, swap.source_slot.mechanic_id);
          if (status == false) {
            System.out.println("Failed to shift second slot to first mechanic");
          }
        } else {
          System.out.println("Failed to shift first slot to second mechanic");
        }
      }
      {
        // Update the swaps table with the accepted status.
        String accept_swap_query = "update SWAPS s set status=1 where swap_id=?";
        PreparedStatement statement = connection.prepareStatement(accept_swap_query);
        statement.setInt(1, swap.swap_id);
        int row_count = statement.executeUpdate();
        status = (row_count != 0);
      }
    } catch (SQLException e) {
      System.out.println("ERROR while trying to swap schedules, details : " + e.getMessage());
    }

    return status;
  }

  // Checks for 1 slot at a time i.e., count of mechanics available at slot 1,
  // slot 2 etc.
  public static Integer getCountOfMechanicsAvailableAtSlot(String service_center_id, Integer week, Integer day,
      Integer slot_start) {
    Integer count = 0;
    try {
      Connection connection = ConnectionManager.getDatabaseConnection();
      String get_count_of_available_employees_query = "select count(distinct emp_id) from SCHEDULE where service_center_id=? and week=? and day=? and (? between slot_start and slot_end)";
      PreparedStatement statement = connection.prepareStatement(get_count_of_available_employees_query);
      statement.setString(1, service_center_id);
      statement.setInt(2, week);
      statement.setInt(3, day);
      statement.setInt(4, slot_start);
      ResultSet results = statement.executeQuery();
      if (results.next())
        count = results.getInt(1);
      int total = getMechanicsForServiceCenter(service_center_id).size();
      count = total - count;
    } catch (SQLException e) {
      Utils.printSeperator();
      System.out
          .println("ERROR while getting count of employees available during given slot, details : " + e.getMessage());
    }
    return count;
  }

  public static boolean isTimeOffAllowed(ScheduleInfo time_off_info) {
    if (isMechanicFreeForSlot(time_off_info) == false) {
      System.out.println("Time off not allowed because mechanic is not free");
      return false;
    }
    for (int i = time_off_info.slot_start; i <= time_off_info.slot_end; i++) {
      Integer available_employees = getCountOfMechanicsAvailableAtSlot(time_off_info.service_center_id,
          time_off_info.week_id, time_off_info.day_id, i);
      if (available_employees < 3) {
        System.out.println("EMP ERROR");
        return false;
      }
    }
    return true;
  }

  public static boolean recordTimeOffForEmployee(ScheduleInfo time_off_info) {
    // TODO: This invoice ID is incorrect but we should have a dummy invoice to
    // indicate time-off as per the current approach.
    return assignSlotToMechanic(time_off_info, TIME_OFF_INVOICE_ID);
  }
};
