package autor;

import autor.User;
import java.util.*;
import java.sql.*;

public class Mechanic extends User {

	private String __mechanic_id = null;

	/**
	 * Default constructor for Mechanic
	 * 
	 * @param user_type the Mechanic's type, calls User super constructor
	 */
	public Mechanic(UserType user_type, String mechanic_id) {
		super(user_type);
		__mechanic_id = mechanic_id;
	}

	@Override
	public void displayMenuForUser() {
		Map<Integer, String> options = new HashMap<Integer, String>();
		options.put(1, "View Schedule");
		options.put(2, "Request Time-off");
		options.put(3, "Request Swap");
		options.put(4, "Accept/ Reject Swap");
		Integer selection = Utils.getInputFromMenu(options);
		switch (selection) {
			case 1: {
				viewSchedule();
				break;
			}
			case 2: {
				requestTimeOff();
				break;
			}
			case 3: {
				requestSwap();
				break;
			}
			case 4: {
				acceptOrRejectSwap();
				break;
			}
			default: {
				Utils.printInvalidSelectionMessage();
				return;
			}
		}
		displayMenuForUser();
		return;
	}

	/**
	 * Displays this Mechanic's schedule
	 */
	public void viewSchedule() {
		ArrayList<ScheduleInfo> assigned_hours = ScheduleUtil.getSlotsForMechanic(__mechanic_id);
		ScheduleInfo.printSlots(assigned_hours);
		Utils.printSeperator();
	}

	public void requestSwap() {
		Utils.printSeperator();

		String service_center_id = null;
		try {
			service_center_id = ScheduleUtil.getServiceCenterForEmployee(__mechanic_id);
		} catch (Exception e) {
			System.out.println("Failed to find service center id for employee, please try again, details : " + e.getMessage());
		}

		ScheduleInfo source_slot = new ScheduleInfo();
		source_slot.service_center_id = service_center_id;
		source_slot.mechanic_id = __mechanic_id;
		source_slot.week_id = Utils.getIntegerInput("Please enter the week ID of the swap you are offering : ");
		source_slot.day_id = Utils.getIntegerInput("Please enter the day ID of the swap you are offering : ");
		source_slot.slot_start = Utils
				.getIntegerInput("Please enter the start time ID of the swap you are offering : ");
		source_slot.slot_end = Utils.getIntegerInput("Please enter the end time ID of the swap you are offering : ");

		ScheduleInfo target_slot = new ScheduleInfo();
		target_slot.service_center_id = service_center_id;
		target_slot.mechanic_id = Utils
				.getStringInput("Please enter the ID of the mechanic you want to offer the swap to : ");
		target_slot.week_id = Utils.getIntegerInput("Please enter the week which you want to request : ");
		target_slot.day_id = Utils.getIntegerInput("Please enter the day ID which you want to request : ");
		target_slot.slot_start = Utils
				.getIntegerInput("Please enter the start time ID of the slot which you want to take : ");
		target_slot.slot_end = Utils
				.getIntegerInput("Please enter the end time ID of the slot which you want to take : ");

		System.out.println(source_slot.mechanic_id + " "  + target_slot.mechanic_id);

		if (ScheduleUtil.isMechanicAvailableForSwap(source_slot, target_slot)) {
		System.out.println(source_slot.mechanic_id + " "  + target_slot.mechanic_id);
			boolean status = ScheduleUtil.requestSwap(source_slot, target_slot);
			if (status == false) {
				System.out.println("Failed to register swap! Please try again.");
			}
		} else {
			Utils.printSeperator();
			System.out.println("The requested mechanic is unavailable for swap. Please try with other parameters.");
			Utils.printSeperator();
		}
	}

	public void requestTimeOff() {
		Utils.printSeperator();
		Integer week = Utils.getIntegerInput("Enter the week ID intended for time-off : ");
		Integer day = Utils.getIntegerInput("Enter the day ID intended for time-off : ");
		Integer start_slot = Utils.getIntegerInput("Enter the starting time slot for the time-off");
		Integer end_slot = Utils.getIntegerInput("Enter the ending time slot for the time-off");
		System.out.println("Looking for slot...");
		boolean result = requestTimeOffImpl(week, day, start_slot, end_slot);
		if (result) {
			System.out.println("Time off scheduled succesfully! Have a great holiday!");
		} else {
			System.out.println("Failed to schedule time-off, work your shift!");
		}
	}

	/**
	 * Requests time off at the time slot indicated by week, day, starting time
	 * slot,
	 * and ending time slot. Automatically approved if Mechanic is not assigned job
	 * during the given period.
	 * 
	 * @param week      Week of the time to request off
	 * @param day       Day of the time to request off
	 * @param startSlot Start time slot to request off
	 * @param endSlot   End of time slot to request off
	 * @return True if approved
	 */
	public Boolean requestTimeOffImpl(Integer week, Integer day, Integer startSlot, Integer endSlot) {
		ScheduleInfo time_off_info = new ScheduleInfo();
		time_off_info.mechanic_id = __mechanic_id;
		time_off_info.service_center_id = getServiceCenterID();
		time_off_info.week_id = week;
		time_off_info.day_id = day;
		time_off_info.slot_start = startSlot;
		time_off_info.slot_end = endSlot;

		if (ScheduleUtil.isTimeOffAllowed(time_off_info) == false) {
			return false;
		}

		boolean result = ScheduleUtil.recordTimeOffForEmployee(time_off_info);
		if (result == false) {
			Utils.printSeperator();
			System.out.println("Failed to schedule time-off, please try again later!");
		}
		return result;
	}

	private String getServiceCenterID() {
		try {
			return ScheduleUtil.getServiceCenterForEmployee(__mechanic_id);
		} catch (SQLException e) {
			System.out
					.println("ERROR while trying to fetch service center ID for mechanic. Details : " + e.getMessage());
		}
		return "-1";
	}

	/**
	 * Displays a list of pending swaps
	 */
	public void acceptOrRejectSwap() {
		ArrayList<SwapInfo> swaps = ScheduleUtil.getSwapRequests(__mechanic_id);
		SwapInfo.printSwaps(swaps);

		Utils.printSeperator();
		Integer swap_id = Utils.getIntegerInput("Enter the swap ID that you would like to accept.");

		for (SwapInfo swap : swaps) {
			if (swap.swap_id == swap_id) {
				boolean result = ScheduleUtil.acceptSwap(swap);
				if (result) {
					System.out.println("Swap succesfully accpeted, schedule updated!");
				} else {
					System.out.println("Failed to accept swap, please try again!");
				}
			}
		}
		return;
	}
};
