package autor;

import java.util.*;

public class ScheduleInfo {
  String service_center_id;
  String mechanic_id;
  Integer week_id;
  Integer day_id;
  Integer slot_start;
  Integer slot_end;

  public static void printSlots(ArrayList<ScheduleInfo> slots) {
    Utils.printSeperator();

    String sep = "-".repeat(10);
    String slot_format = "%d. +%10d+%10d+%10d+%10d+%20s+\n";
    String header = "     + " + sep + " + " + sep + " + " + sep + " + " + sep + " +\n";
    System.out.format(header);
    System.out.format("+ %10s + %10s + %10s + %10s + %10s+\n", "Week", "Day", "Start", "End", "Mechanic ID");
    int index = 1;
    for (ScheduleInfo slot : slots) {
      System.out.format(slot_format, index, slot.week_id, slot.day_id, slot.slot_start, slot.slot_end, slot.mechanic_id);
      index++;
    }

    System.out.format(header);

    Utils.printSeperator();
  }
};
