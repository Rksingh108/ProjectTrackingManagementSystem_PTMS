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

public class TicketTrackingController {

    private final TicketTrackingService ticketTrackingService;
    private final Scanner scanner;

    public TicketTrackingController() {
        this.ticketTrackingService = new ITicketTrackingService();
        this.scanner = new Scanner(System.in);
    }

    public TicketTrackingController(
            TicketTrackingService ticketTrackingService,
            Scanner scanner) {
        this.ticketTrackingService = ticketTrackingService;
        this.scanner = scanner;
    }

    public void showMenu(User loggedInUser) {
        if (loggedInUser == null) {
            throw new UnauthorizedException("User is not logged in.");
        }

        boolean running = true;

        while (running) {
            showDashboard();

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        viewTrackingForTicket();
                        break;
                    case "2":
                        viewMyUpdates(loggedInUser);
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (ResourceNotFoundException | ValidationException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void showDashboard() {
        System.out.println();
        System.out.println("==============================");
        System.out.println("       TICKET TRACKING");
        System.out.println("==============================");
        System.out.println("0. Back");
        System.out.println("1. View Ticket Tracking");
        System.out.println("2. View My Recent Updates");
        System.out.println("==============================");
        System.out.print("Choose an option: ");
    }

    private void viewTrackingForTicket() throws SQLException {
        int ticketId = readInt("Ticket ID: ");

        TicketTracking tracking =
                ticketTrackingService.getTrackingForTicket(ticketId);

        printTracking(tracking);
    }

    private void viewMyUpdates(User requestingUser) throws SQLException {
        List<TicketTracking> updates =
                ticketTrackingService.getUpdatesByUser(
                        requestingUser.getId()
                );

        if (updates.isEmpty()) {
            System.out.println("You have not updated any tickets.");
            return;
        }

        System.out.println();
        System.out.println("========== YOUR TICKET UPDATES ==========");

        updates.forEach(this::printTracking);
    }

    private int readInt(String message) {
        System.out.print(message);
        return Integer.parseInt(scanner.nextLine().trim());
    }

    private void printTracking(TicketTracking tracking) {
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("Ticket ID   : " + tracking.getTicketId());
        System.out.println("Status      : " + tracking.getStatus());
        System.out.println("Progress    : " + tracking.getProgress() + "%");
        System.out.println("Updated By  : " + tracking.getUpdatedBy());
        System.out.println("Updated At  : " + tracking.getUpdatedAt());
        System.out.println("Comment     : " + tracking.getComment());
        System.out.println("----------------------------------------");
    }
}