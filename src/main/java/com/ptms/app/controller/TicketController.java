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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class TicketController {

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
            showDashboard();

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        createTicket(loggedInUser);
                        break;
                    case "2":
                        viewTicketsForProject();
                        break;
                    case "3":
                        viewMyTickets(loggedInUser);
                        break;
                    case "4":
                        assignTicket(loggedInUser);
                        break;
                    case "5":
                        advanceStatus(loggedInUser);
                        break;
                    case "6":
                        deleteTicket(loggedInUser);
                        break;
                    case "7":
                        viewTicket();
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (UnauthorizedException |
                     ValidationException |
                     ResourceNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date. Use YYYY-MM-DD.");
            }
        }
    }

    private void showDashboard() {
        System.out.println();
        System.out.println("==============================");
        System.out.println("       TICKET MANAGEMENT");
        System.out.println("==============================");
        System.out.println("0. Back");
        System.out.println("1. Create Ticket");
        System.out.println("2. View Project Tickets");
        System.out.println("3. View My Tickets");
        System.out.println("4. Assign Ticket");
        System.out.println("5. Update Ticket Status");
        System.out.println("6. Delete Ticket");
        System.out.println("7. View Ticket By ID");
        System.out.println("==============================");
        System.out.print("Choose an option: ");
    }

    private void createTicket(User requestingUser) throws SQLException {
        int projectId = readInt("Project ID: ");

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Description: ");
        String description = scanner.nextLine().trim();

        System.out.print("Priority (LOW, MEDIUM, HIGH): ");
        String priority = scanner.nextLine().trim().toUpperCase();

        System.out.print("Deadline (YYYY-MM-DD, blank to skip): ");
        String deadlineInput = scanner.nextLine().trim();

        LocalDate deadline = null;

        if (!deadlineInput.isEmpty()) {
            deadline = LocalDate.parse(deadlineInput);
        }

        Ticket ticket = new Ticket(
                projectId,
                title,
                description,
                priority
        );

        ticket.setDeadline(deadline);

        ticketService.createTicket(ticket, requestingUser);

        System.out.println(
                "Ticket created successfully. ID: " + ticket.getId()
        );
    }

    private void viewTicketsForProject() throws SQLException {
        int projectId = readInt("Project ID: ");

        List<Ticket> tickets =
                ticketService.getTicketsForProject(projectId);

        if (tickets.isEmpty()) {
            System.out.println("No tickets found.");
            return;
        }

        System.out.println();
        System.out.println("========== PROJECT TICKETS ==========");

        tickets.forEach(this::printTicket);
    }

    private void viewMyTickets(User requestingUser) throws SQLException {
        List<Ticket> tickets =
                ticketService.getTicketsForUser(requestingUser.getId());

        if (tickets.isEmpty()) {
            System.out.println("No tickets assigned to you.");
            return;
        }

        System.out.println();
        System.out.println("========== MY TICKETS ==========");

        tickets.forEach(this::printTicket);
    }

    private void viewTicket() throws SQLException {
        int ticketId = readInt("Ticket ID: ");

        Ticket ticket = ticketService.getTicketById(ticketId);

        printTicket(ticket);
    }

    private void assignTicket(User requestingUser) throws SQLException {
        int ticketId = readInt("Ticket ID: ");
        int userId = readInt("User ID to assign: ");

        ticketService.assignTicket(
                ticketId,
                userId,
                requestingUser
        );

        System.out.println("Ticket assigned successfully.");
    }

    private void advanceStatus(User requestingUser) throws SQLException {
        int ticketId = readInt("Ticket ID: ");

        System.out.println();
        System.out.println("New Status:");
        System.out.println("1. IN_PROGRESS");
        System.out.println("2. IMPLEMENTED");
        System.out.println("3. COMPLETED");
        System.out.print("Choose status: ");

        String statusChoice = scanner.nextLine().trim();

        String newStatus;

        switch (statusChoice) {
            case "1":
                newStatus = "IN_PROGRESS";
                break;
            case "2":
                newStatus = "IMPLEMENTED";
                break;
            case "3":
                newStatus = "COMPLETED";
                break;
            default:
                throw new ValidationException("Invalid status.");
        }

        int progress = readInt("Progress (0-100): ");

        System.out.print("Comment: ");
        String comment = scanner.nextLine().trim();

        ticketService.advanceStatus(
                ticketId,
                newStatus,
                progress,
                comment,
                requestingUser
        );

        System.out.println("Ticket status updated successfully.");
    }

    private void deleteTicket(User requestingUser) throws SQLException {
        int ticketId = readInt("Ticket ID to delete: ");

        ticketService.deleteTicket(
                ticketId,
                requestingUser
        );

        System.out.println("Ticket deleted successfully.");
    }

    private int readInt(String message) {
        System.out.print(message);
        String input = scanner.nextLine().trim();

        return Integer.parseInt(input);
    }

    private void printTicket(Ticket ticket) {
        System.out.println();
        System.out.println("----------------------------------------");
        System.out.println("Ticket ID     : " + ticket.getId());
        System.out.println("Project ID    : " + ticket.getProjectId());
        System.out.println("Title         : " + ticket.getTitle());
        System.out.println("Description   : " + ticket.getDescription());
        System.out.println("Priority      : " + ticket.getPriority());
        System.out.println("Status        : " + ticket.getStatus());
        System.out.println("Assigned To   : " + ticket.getAssignedTo());
        System.out.println("Deadline      : " + ticket.getDeadline());
        System.out.println("Created At    : " + ticket.getCreatedAt());
        System.out.println("----------------------------------------");
    }
}