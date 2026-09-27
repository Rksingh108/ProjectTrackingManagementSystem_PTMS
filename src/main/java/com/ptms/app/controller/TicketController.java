package com.ptms.app.controller;

import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Ticket;
import com.ptms.app.model.User;
import com.ptms.app.service.ITicketService;
import com.ptms.app.service.TicketService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class TicketController {

    private static final Logger logger =
            Logger.getLogger(TicketController.class.getName());

    private final TicketService ticketService;
    private final Scanner scanner;

    public TicketController() {
        this.ticketService = new ITicketService();
        this.scanner = new Scanner(System.in);
    }

    public TicketController(
            TicketService ticketService,
            Scanner scanner) {
        this.ticketService = ticketService;
        this.scanner = scanner;
    }

    public void showMenu(User loggedInUser) {
        boolean running = true;

        while (running) {
            logger.info("""
                    
                    --- Ticket Management ---
                    1. Create ticket
                    2. View tickets for a project
                    3. View my assigned tickets
                    4. Assign ticket
                    5. Advance ticket status
                    6. Delete ticket
                    0. Back
                    Choose an option:
                    """);

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> createTicket(loggedInUser);
                    case "2" -> viewTicketsForProject();
                    case "3" -> viewMyTickets(loggedInUser);
                    case "4" -> assignTicket(loggedInUser);
                    case "5" -> advanceStatus(loggedInUser);
                    case "6" -> deleteTicket(loggedInUser);
                    case "0" -> {
                        running = false;
                        logger.info("Returning to previous menu.");
                    }
                    default -> logger.warning(
                            "Invalid option, try again."
                    );
                }
            } catch (UnauthorizedException |
                     ValidationException |
                     ResourceNotFoundException e) {

                logger.warning("Error: " + e.getMessage());

            } catch (SQLException e) {

                logger.severe(
                        "Database error: " + e.getMessage()
                );
            }
        }
    }

    private void createTicket(User requestingUser)
            throws SQLException {

        logger.info("Project id:");
        int projectId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        logger.info("Title:");
        String title = scanner.nextLine().trim();

        logger.info("Description:");
        String description = scanner.nextLine().trim();

        logger.info(
                "Priority (LOW, MEDIUM, HIGH):"
        );
        String priority =
                scanner.nextLine().trim().toUpperCase();

        logger.info(
                "Deadline (YYYY-MM-DD, blank to skip):"
        );

        String deadlineInput =
                scanner.nextLine().trim();

        LocalDate deadline =
                deadlineInput.isEmpty()
                        ? null
                        : LocalDate.parse(deadlineInput);

        Ticket ticket = new Ticket(
                projectId,
                title,
                description,
                priority
        );

        ticket.setDeadline(deadline);

        ticketService.createTicket(
                ticket,
                requestingUser
        );

        logger.info(
                "Ticket created successfully. ID: "
                        + ticket.getId()
        );
    }

    private void viewTicketsForProject()
            throws SQLException {

        logger.info("Project id:");

        int projectId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        List<Ticket> tickets =
                ticketService.getTicketsForProject(projectId);

        if (tickets.isEmpty()) {
            logger.info(
                    "No tickets found for this project."
            );
            return;
        }

        tickets.forEach(this::printTicketSummary);
    }

    private void viewMyTickets(User requestingUser)
            throws SQLException {

        List<Ticket> tickets =
                ticketService.getTicketsForUser(
                        requestingUser.getId()
                );

        if (tickets.isEmpty()) {
            logger.info(
                    "No tickets assigned to you."
            );
            return;
        }

        tickets.forEach(this::printTicketSummary);
    }

    private void assignTicket(User requestingUser)
            throws SQLException {

        logger.info("Ticket id:");

        int ticketId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        logger.info("User id to assign:");

        int userId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        ticketService.assignTicket(
                ticketId,
                userId,
                requestingUser
        );

        logger.info("Ticket assigned successfully.");
    }

    private void advanceStatus(User requestingUser)
            throws SQLException {

        logger.info("Ticket id:");

        int ticketId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        logger.info(
                "New status (IN_PROGRESS, IMPLEMENTED, COMPLETED):"
        );

        String newStatus =
                scanner.nextLine().trim().toUpperCase();

        logger.info("Progress (0-100):");

        int progress = Integer.parseInt(
                scanner.nextLine().trim()
        );

        logger.info("Comment:");

        String comment = scanner.nextLine().trim();

        ticketService.advanceStatus(
                ticketId,
                newStatus,
                progress,
                comment,
                requestingUser
        );

        logger.info(
                "Ticket status updated to " + newStatus + "."
        );
    }

    private void deleteTicket(User requestingUser)
            throws SQLException {

        logger.info("Ticket id to delete:");

        int ticketId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        ticketService.deleteTicket(
                ticketId,
                requestingUser
        );

        logger.info("Ticket deleted successfully.");
    }

    private void printTicketSummary(Ticket ticket) {

        logger.info(
                String.format(
                        "id=%d | %s | project=%d | priority=%s | status=%s | assignedTo=%s",
                        ticket.getId(),
                        ticket.getTitle(),
                        ticket.getProjectId(),
                        ticket.getPriority(),
                        ticket.getStatus(),
                        ticket.getAssignedTo()
                )
        );
    }
}