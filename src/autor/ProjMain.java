package autor;

import java.util.*;
import autor.Utils;
import autor.ConnectionManager;

public class ProjMain {

    private static ProjMain __instance;

    private ConnectionManager __database_manager;

    public ProjMain() throws Exception {
        __database_manager = new ConnectionManager();
    }

    public ConnectionManager getConnectionManager() {
        return __database_manager;
    }

    public void displayMainMenu() {
        try {
            System.out.println(Utils.getSeperator());
            System.out.println("Welcome to AUTOR!\nSelect from menu:");

            Map<Integer, String> main_menu_options = new HashMap<Integer, String>();
            main_menu_options.put(1, "Login");
            main_menu_options.put(2, "Admin Options");
            main_menu_options.put(3, "Exam Queries");
            main_menu_options.put(4, "Exit");

            int selection = Utils.getInputFromMenu(main_menu_options);

            System.out.println("Selected option : " + selection);

            System.out.println(Utils.getSeperator());

            switch (selection) {
                case 1: {
                    User.performLogin();
                    // displaySignInMenu();
                    break;
                }
                case 2: {
                    Admin.handleAdminPageSelection();
                    break;
                }
                case 3: {
                    System.out.println("Running exam queries");
                    Utils.handleExamQueries();
                    displayMainMenu();
                    return;
                }
                case 4: {
                    System.out.println("Exiting from the program");
                    return;
                }
                default: {
                    System.out.println("Invalid option selected.");
                    return;
                }
            }
        } catch (Exception e) {
            Utils.printSeperator();
            System.out.println("ERROR exception during main menu display, details : " + e.getMessage());
            displayMainMenu();
        }
    }

    public static ProjMain getInstance() {
        return __instance;
    }

    public static void cleanUp() throws Exception {
        // Perform all cleanup operations before quitting the application.
        __instance.getConnectionManager().closeDatabaseConnection();
        Utils.closeScanner();
    }

    public static void main(String[] args) {
        try {
            __instance = new ProjMain();
            __instance.displayMainMenu();
        } catch (Exception e) {
            System.out.println("ERROR! Exception occured - " + e.getMessage());
        }
        try {
            cleanUp();
        } catch (Exception e) {
            System.out.println("Error occurred during cleanup");
        }
        return;
    }
};