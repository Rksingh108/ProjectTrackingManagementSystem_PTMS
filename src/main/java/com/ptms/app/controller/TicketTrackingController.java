package com.ptms.app.controller;

import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.TicketTracking;
import com.ptms.app.model.User;
import com.ptms.app.service.ITicketTrackingService;
import com.ptms.app.service.TicketTrackingService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class TicketTrackingController {
    private static final Logger logger = Logger.getLogger(TicketTrackingController.class.getName());

    private final TicketTrackingService trackingService;
    private final Scanner scanner;

    public TicketTrackingController() {
        this.trackingService = new ITicketTrackingService();
        this.scanner = new Scanner(System.in);
    }

    public TicketTrackingController(
            TicketTrackingService trackingService,
            Scanner scanner) {
        this.trackingService = trackingService;
        this.scanner = scanner;
    }

    public void showMenu(User user) {
        while (true) {
            System.out.println("\n===== TICKET TRACKING =====");
            System.out.println("1. View Ticket Tracking");
            System.out.println("2. View My Updates");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> viewTicketTracking(user);
                    case "2" -> viewMyUpdates(user);
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (UnauthorizedException |
                     ValidationException |
                     ResourceNotFoundException e) {

                System.out.println("Error: " + e.getMessage());
                logger.warning(e.getMessage());

            } catch (SQLException e) {

                System.out.println("Database error: " + e.getMessage());
                logger.severe(e.getMessage());
            }
        }
    }

    private void viewTicketTracking(User user) throws SQLException {
        System.out.print("Enter Ticket ID: ");
        int ticketId = Integer.parseInt(scanner.nextLine());

        TicketTracking tracking = trackingService.getTrackingForTicket(ticketId, user);

        printTracking(tracking);
    }

    private void viewMyUpdates(User user) throws SQLException {
        List<TicketTracking> updates = trackingService.getUpdatesByUser(user.getId(), user);

        if (updates.isEmpty()) {
            System.out.println("No tracking updates found.");
            return;
        }

        updates.forEach(this::printTracking);
    }

    private void printTracking(TicketTracking tracking) {
        System.out.println("\n-----------------------------");
        System.out.println("Tracking ID : " + tracking.getId());
        System.out.println("Ticket ID   : " + tracking.getTicketId());
        System.out.println("Status      : " + tracking.getStatus());
        System.out.println("Progress    : " + tracking.getProgress() + "%");
        System.out.println("Comment     : " + tracking.getComment());
        System.out.println("Updated By  : " + tracking.getUpdatedBy());
        System.out.println("Updated At  : " + tracking.getUpdatedAt());
        System.out.println("-----------------------------");
    }
}