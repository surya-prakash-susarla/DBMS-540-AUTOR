package autor;

import java.util.*;

public class SwapInfo {
  public SwapInfo() {
    source_slot = new ScheduleInfo();
    target_slot = new ScheduleInfo();
  }

  public Integer swap_id;
  public ScheduleInfo source_slot;
  public ScheduleInfo target_slot;

  public static void printSwaps(ArrayList<SwapInfo> swaps) {
    Utils.printSeperator();
    System.out.println("Available swap requests are : ");
    Utils.printSeperator();
    for(SwapInfo swap : swaps ) {
      System.out.println("ID : " + swap.swap_id );
      System.out.println(String.format("From : %s\nWeek: %d, Day: %d, Start: %d, End: %s", swap.source_slot.mechanic_id, swap.source_slot.week_id, swap.source_slot.day_id, swap.source_slot.slot_start, swap.source_slot.slot_end));
      System.out.println(String.format("Requested for your slot on:\nWeek: %d, Day: %d, Start: %d, End: %d", swap.target_slot.week_id, swap.target_slot.day_id, swap.target_slot.slot_start, swap.target_slot.slot_end));
      Utils.printSeperator();
    }
  }
};
