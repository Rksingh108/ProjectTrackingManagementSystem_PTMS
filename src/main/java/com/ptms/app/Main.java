package com.ptms.app;

import com.ptms.app.controller.ClientController;
import com.ptms.app.controller.ProjectController;
import com.ptms.app.controller.ProjectMemberController;
import com.ptms.app.controller.TicketController;
import com.ptms.app.controller.TicketTrackingController;
import com.ptms.app.controller.UserController;
import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserController userController = new UserController();
        boolean applicationRunning = true;

        while (applicationRunning) {
            User loggedInUser = null;

            while (loggedInUser == null) {
                System.out.println();
                System.out.println("======================================");
                System.out.println("       PROJECT TRACKING SYSTEM");
                System.out.println("======================================");
                System.out.println("0. Close Application");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("======================================");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();

                try {
                    switch (choice) {
                        case "1" -> loggedInUser = userController.login();

                        case "2" -> loggedInUser = userController.registerUser();

                        case "0" -> {
                            System.out.println("Thank you. Visit again.");
                            applicationRunning = false;
                            loggedInUser = null;
                        }

                        default -> System.out.println("Invalid option.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                    System.out.println("Check DBConnection, db.properties and MySQL.");
                }
            }

            if (applicationRunning && loggedInUser != null) {
                showDashboard(loggedInUser, userController, scanner);
            }
        }

        scanner.close();
    }

    private static void showDashboard(User loggedInUser, UserController userController, Scanner scanner) {
        ClientController clientController = new ClientController();
        ProjectController projectController = new ProjectController();
        ProjectMemberController projectMemberController = new ProjectMemberController();
        TicketController ticketController = new TicketController();
        TicketTrackingController ticketTrackingController = new TicketTrackingController();
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("======================================");
            System.out.println("           PTMS DASHBOARD");
            System.out.println("======================================");
            System.out.println("Logged in as : " + loggedInUser.getUsername());
            System.out.println("Role         : " + loggedInUser.getRole());
            System.out.println("--------------------------------------");
            System.out.println("0. Logout");
            System.out.println("1. User Management");
            System.out.println("2. Client Management");
            System.out.println("3. Project Management");
            System.out.println("4. Project Members");
            System.out.println("5. Ticket Management");
            System.out.println("6. Ticket Tracking");
            System.out.println("======================================");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> userController.showMenu(loggedInUser);

                case "2" -> clientController.showMenu(loggedInUser);

                case "3" -> projectController.showMenu(loggedInUser);

                case "4" -> projectMemberController.showMenu(loggedInUser);

                case "5" -> ticketController.showMenu(loggedInUser);

                case "6" -> ticketTrackingController.showMenu(loggedInUser);

                case "0" -> {
                    System.out.println("Logged out successfully.");
                    running = false;
                }

                default -> System.out.println("Invalid option.");
            }
        }
    }
}