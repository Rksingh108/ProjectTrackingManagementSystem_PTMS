package com.ptms.app;

import com.ptms.app.controller.ClientController;
import com.ptms.app.controller.ProjectController;
import com.ptms.app.controller.TicketController;
import com.ptms.app.controller.TicketTrackingController;
import com.ptms.app.controller.UserController;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.model.User;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        UserController userController = new UserController();
        ProjectController projectController = new ProjectController();
        ClientController clientController = new ClientController();
        TicketController ticketController = new TicketController();
        TicketTrackingController trackingController = new TicketTrackingController();

        System.out.println("==========================================");
        System.out.println("   PROJECT TRACKING MANAGEMENT SYSTEM");
        System.out.println("==========================================");

        while (true) {
            System.out.println("\n========== PTMS ==========");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.print("Choose option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> userController.registerUser();

                    case "2" -> {
                        User loggedInUser = userController.login();

                        if (loggedInUser != null) {
                            showDashboard(
                                    loggedInUser,
                                    userController,
                                    clientController,
                                    projectController,
                                    ticketController,
                                    trackingController
                            );
                        }
                    }

                    case "0" -> {
                        System.out.println("Thank you for using PTMS.");
                        scanner.close();
                        return;
                    }

                    default -> System.out.println("Invalid option.");
                }
            } catch (UnauthorizedException e) {
                System.out.println("Unauthorized: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void showDashboard(
            User user,
            UserController userController,
            ClientController clientController,
            ProjectController projectController,
            TicketController ticketController,
            TicketTrackingController trackingController) {

        boolean running = true;

        while (running) {
            System.out.println("\n==========================================");
            System.out.println("              PTMS DASHBOARD");
            System.out.println("==========================================");
            System.out.println("User : " + user.getUsername());
            System.out.println("Role : " + user.getRole());
            System.out.println("==========================================");

            switch (user.getRole()) {
                case ADMIN -> {
                    System.out.println("1. User Management");
                    System.out.println("2. Client Management");
                    System.out.println("3. Project Management");
                    System.out.println("4. Ticket Management");
                    System.out.println("5. Ticket Tracking");
                    System.out.println("0. Logout");

                    System.out.print("Choose option: ");

                    switch (getInput()) {
                        case "1" -> userController.showMenu(user);
                        case "2" -> clientController.showMenu(user);
                        case "3" -> projectController.showMenu(user);
                        case "4" -> ticketController.showMenu(user);
                        case "5" -> trackingController.showMenu(user);
                        case "0" -> running = false;
                        default -> System.out.println("Invalid option.");
                    }
                }

                case PROJECT_MANAGER -> {
                    System.out.println("1. Project Management");
                    System.out.println("2. Ticket Management");
                    System.out.println("3. Ticket Tracking");
                    System.out.println("0. Logout");

                    System.out.print("Choose option: ");

                    switch (getInput()) {
                        case "1" -> projectController.showMenu(user);
                        case "2" -> ticketController.showMenu(user);
                        case "3" -> trackingController.showMenu(user);
                        case "0" -> running = false;
                        default -> System.out.println("Invalid option.");
                    }
                }

                case TEAM_LEAD -> {
                    System.out.println("1. Project Management");
                    System.out.println("2. Ticket Management");
                    System.out.println("3. Ticket Tracking");
                    System.out.println("0. Logout");

                    System.out.print("Choose option: ");

                    switch (getInput()) {
                        case "1" -> projectController.showMenu(user);
                        case "2" -> ticketController.showMenu(user);
                        case "3" -> trackingController.showMenu(user);
                        case "0" -> running = false;
                        default -> System.out.println("Invalid option.");
                    }
                }

                case TEAM_MEMBER -> {
                    System.out.println("1. Project Management");
                    System.out.println("2. Ticket Management");
                    System.out.println("3. Ticket Tracking");
                    System.out.println("0. Logout");

                    System.out.print("Choose option: ");

                    switch (getInput()) {
                        case "1" -> projectController.showMenu(user);
                        case "2" -> ticketController.showMenu(user);
                        case "3" -> trackingController.showMenu(user);
                        case "0" -> running = false;
                        default -> System.out.println("Invalid option.");
                    }
                }
            }

            if (!running) {
                System.out.println("Logged out successfully.");
            }
        }
    }

    private static String getInput() {
        return scanner.nextLine().trim();
    }
}