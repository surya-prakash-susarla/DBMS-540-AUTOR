package autor;

import java.io.*;
import java.util.*;

public class DataReader {

  // WE ARE CURRENTLY NOT READING THE SERVICE CATEGORY ATTRIBUTE.
  public static String[] kServiceInfoFields = { "service_name", "service_id", "price_tier", "time" };

  public static String[] kServiceBundleFields = { "Schedules", "Services", "Service_ID", "Time", "PriceTier" };

  public static String[] kStoreInfoFields = { "service_center_id", "address", "phone", "saturday_status", "manager_id",
      "min_wage", "max_wage", "hourly_wage" };

  // DEBUG ONLY -> ONLY USE WHILE DEBUGGING, PRINTS A LOT OF STUFF OTHERWISE.
  private static void __debug_printCSVContents(ArrayList<Map<String, String>> contents) {
    System.out.println("Printing contents of the map");
    for (Map<String, String> i : contents) {
      for (Map.Entry<String, String> j : i.entrySet()) {
        System.out.println(String.format("Key : %20s <---> Value : %20s", j.getKey(), j.getValue()));
      }
    }
    return;
  }

  // NOTE: First line is the header and will be skipped from insertion
  private static ArrayList<Map<String, String>> readFieldsFromFile(Scanner file_contents, String[] fields) {
    ArrayList<Map<String, String>> values = new ArrayList<Map<String, String>>();
    try {
      Integer field_count = fields.length;
      System.out.println("Number of fields : " + field_count);

      if (field_count == 0) {
        System.out.println("No rows given!");
      } else {

        boolean is_header = true;
        while (file_contents.hasNext()) {
          Map<String, String> contents = new HashMap<String, String>();
          for (int i = 0; i < field_count; i++) {
            contents.put(fields[i], file_contents.next());
          }
          System.out.println();
          if (is_header) {
            System.out.println("Skipping the header line!");
            is_header = false;
          } else {
            values.add(contents);
          }
        }
      }
    } catch (Exception e) {
      Utils.printSeperator();
      System.out.println("ERROR while reading rows from service CSV, details : " + e.getMessage());
      __debug_printCSVContents(values);
    }
    return values;
  }

  private static Scanner getScannerForFile(String path) {
    Scanner scanner = null;
    try {
      System.out.println("Searching for the file in location -> " + System.getProperty("user.dir") + "/" + path);
      File file = new File(path);
      if (file.exists()) {
        System.out.println("File found!");
        scanner = new Scanner(file);
        scanner.useDelimiter(",|\r\n|\n");
      } else {
        System.out.println("ERROR -> FILE NOT FOUND");
      }
    } catch (Exception e) {
      System.out.println("ERROR while trying to get a handle for csv file, details : " + e.getMessage());
    }
    return scanner;
  }

  public static ArrayList<Map<String, String>> getListOfRowsFromFile(String file_path, String[] field_names) {
    ArrayList<Map<String, String>> contents = null;
    try {
      Scanner file_scanner = getScannerForFile(file_path);
      if (file_scanner != null) {
        contents = readFieldsFromFile(file_scanner, field_names);
        file_scanner.close();
      }
    } catch (Exception e) {
      Utils.printSeperator();
      System.out.println("ERROR while getting rows from file, details : " + e.getMessage());
    }
    return contents;
  }

  public static ArrayList<Map<String, String>> readServiceInfoFromFile() {
    return getListOfRowsFromFile("data/services.csv", kServiceInfoFields);
  }

  public static ArrayList<Map<String, String>> readServiceBundleFromFile() {
    return getListOfRowsFromFile("data/Bundles.csv", kServiceBundleFields);
  }

  public static ArrayList<Map<String, String>> readeStoreInfoFromFile() {
    return getListOfRowsFromFile("data/service_centers.csv", kStoreInfoFields);
  }

};
