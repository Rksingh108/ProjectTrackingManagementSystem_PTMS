package com.ptms.app;

import com.ptms.app.controller.ClientController;
import com.ptms.app.controller.ProjectController;
import com.ptms.app.controller.ProjectMemberController;
import com.ptms.app.controller.UserController;
import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * The one entry point that actually runs the app.
 * Everything else (model/dao/service/controller) is just classes sitting
 * there until something like this calls them.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserController userController = new UserController();

        User loggedInUser = null;

        while (loggedInUser == null) {
            System.out.println("\n=== PTMS ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> loggedInUser = userController.login();
                    case "2" -> loggedInUser = userController.registerUser();
                    case "0" -> {
                        System.out.println("Goodbye.");
                        return;
                    }
                    default -> System.out.println("Invalid option, try again.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                System.out.println("Check DBConnection / db.properties and that MySQL is running.");
            }
        }

        // Once logged in, hand off to a main dashboard offering each area.
        showDashboard(loggedInUser, userController, scanner);
    }

    private static void showDashboard(User loggedInUser, UserController userController, Scanner scanner) {
        ClientController clientController = new ClientController();
        ProjectController projectController = new ProjectController();
        ProjectMemberController projectMemberController = new ProjectMemberController();
        boolean running = true;

        while (running) {
            System.out.println("\n=== Dashboard (" + loggedInUser.getUsername() + " / " + loggedInUser.getRole() + ") ===");
            System.out.println("1. User Management");
            System.out.println("2. Client Management");
            System.out.println("3. Project Management");
            System.out.println("4. Project Members");
            System.out.println("0. Logout");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> userController.showMenu(loggedInUser);
                case "2" -> clientController.showMenu(loggedInUser);
                case "3" -> projectController.showMenu(loggedInUser);
                case "4" -> projectMemberController.showMenu(loggedInUser);
                case "0" -> {
                    System.out.println("Logged out.");
                    running = false;
                }
                default -> System.out.println("Invalid option, try again.");
            }
        }
    }
}