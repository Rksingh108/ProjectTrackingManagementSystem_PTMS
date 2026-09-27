package com.ptms.app.controller;

import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Client;
import com.ptms.app.model.User;
import com.ptms.app.service.ClientService;
import com.ptms.app.service.IClientService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ClientController {

    private final ClientService clientService;
    private final Scanner scanner;

    public ClientController() {
        this.clientService = new IClientService();
        this.scanner = new Scanner(System.in);
    }

    public ClientController(ClientService clientService, Scanner scanner) {
        this.clientService = clientService;
        this.scanner = scanner;
    }

    public void showMenu(User loggedInUser) {
        boolean running = true;

        while (running) {
            try {
                showDashboard(loggedInUser);

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1" -> viewAllClients(loggedInUser);
                    case "2" -> searchClients(loggedInUser);
                    case "3" -> viewClientDetails(loggedInUser);
                    case "4" -> addClient(loggedInUser);
                    case "5" -> updateClient(loggedInUser);
                    case "6" -> deleteClient(loggedInUser);
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (UnauthorizedException |
                     ResourceNotFoundException |
                     ValidationException e) {
                System.out.println("Operation failed: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void showDashboard(User user) throws SQLException {
        int totalClients = clientService.getTotalClients(user);
        int totalCompanies = clientService.getTotalCompanies(user);

        System.out.println();
        System.out.println("===============================================");
        System.out.println("                CLIENT DASHBOARD               ");
        System.out.println("===============================================");
        System.out.printf("  Logged User     : %-26s %n", user.getUsername());
        System.out.printf("  Role            : %-26s %n", user.getRole());
        System.out.println("===============================================");
        System.out.printf("  Total Clients   : %-26d %n", totalClients);
        System.out.printf("  Companies       : %-26d %n", totalCompanies);
        System.out.println("===============================================");
        System.out.println("  1. View All Clients                         ");
        System.out.println("  2. Search Clients                           ");
        System.out.println("  3. View Client Details                      ");

        if (canModify(user)) {
            System.out.println("  4. Add Client                               ");
            System.out.println("  5. Update Client                            ");
            System.out.println("  6. Delete Client                            ");
        }

        System.out.println("  0. Back                                     ");
        System.out.println("===============================================");
        System.out.print("Choose an option: ");
    }

    private void viewAllClients(User user) throws SQLException {
        List<Client> clients = clientService.getAllClients(user);

        if (clients.isEmpty()) {
            System.out.println("No clients found.");
            return;
        }

        printClientTable(clients);
    }

    private void searchClients(User user) throws SQLException {
        System.out.print("Enter name, email, phone or company: ");

        String keyword = scanner.nextLine().trim();
        List<Client> clients = clientService.searchClients(keyword, user);

        if (clients.isEmpty()) {
            System.out.println("No clients found.");
            return;
        }

        printClientTable(clients);
    }

    private void viewClientDetails(User user) throws SQLException {
        System.out.print("Enter client ID: ");

        int id = Integer.parseInt(scanner.nextLine().trim());
        Client client = clientService.getClientById(id, user);

        printClientDetails(client);
    }

    private void addClient(User user) throws SQLException {
        if (!canModify(user)) {
            throw new UnauthorizedException(
                    "You do not have permission to add clients."
            );
        }

        System.out.println();
        System.out.println("========== ADD CLIENT ==========");

        String name = readRequired("Client Name: ");
        String email = readRequired("Email: ");
        String phone = readRequired("Phone: ");
        String company = readRequired("Company Name: ");

        Client client = new Client(name, email, phone, company);
        Client created = clientService.addClient(client, user);

        System.out.println("Client created successfully.");
        System.out.println("Client ID: " + created.getId());
    }

    private void updateClient(User user) throws SQLException {
        if (!canModify(user)) {
            throw new UnauthorizedException(
                    "You do not have permission to update clients."
            );
        }

        System.out.print("Enter client ID: ");

        int id = Integer.parseInt(scanner.nextLine().trim());
        Client client = clientService.getClientById(id, user);

        System.out.println();
        System.out.println("Leave a field empty to keep the current value.");

        System.out.print("Name [" + client.getName() + "]: ");
        String name = scanner.nextLine().trim();

        if (!name.isEmpty()) {
            client.setName(name);
        }

        System.out.print("Email [" + client.getEmail() + "]: ");
        String email = scanner.nextLine().trim();

        if (!email.isEmpty()) {
            client.setEmail(email);
        }

        System.out.print("Phone [" + client.getPhone() + "]: ");
        String phone = scanner.nextLine().trim();

        if (!phone.isEmpty()) {
            client.setPhone(phone);
        }

        System.out.print("Company [" + client.getCompanyName() + "]: ");
        String company = scanner.nextLine().trim();

        if (!company.isEmpty()) {
            client.setCompanyName(company);
        }

        clientService.updateClient(client, user);

        System.out.println("Client updated successfully.");
    }

    private void deleteClient(User user) throws SQLException {
        if (!canModify(user)) {
            throw new UnauthorizedException(
                    "You do not have permission to delete clients."
            );
        }

        System.out.print("Enter client ID: ");

        int id = Integer.parseInt(scanner.nextLine().trim());
        Client client = clientService.getClientById(id, user);

        printClientDetails(client);

        System.out.print("Confirm deletion (Y/N): ");
        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Delete operation cancelled.");
            return;
        }

        clientService.deleteClient(id, user);

        System.out.println("Client deleted successfully.");
    }

    private void printClientTable(List<Client> clients) {
        System.out.println();
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.printf(
                "%-6s %-22s %-30s %-15s %-25s%n",
                "ID",
                "NAME",
                "EMAIL",
                "PHONE",
                "COMPANY"
        );
        System.out.println("------------------------------------------------------------------------------------------------");

        for (Client client : clients) {
            System.out.printf(
                    "%-6d %-22s %-30s %-15s %-25s%n",
                    client.getId(),
                    client.getName(),
                    client.getEmail(),
                    client.getPhone(),
                    client.getCompanyName()
            );
        }

        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.println("Total results: " + clients.size());
    }

    private void printClientDetails(Client client) {
        System.out.println();
        System.out.println("===============================================");
        System.out.println("                CLIENT DETAILS                 ");
        System.out.println("===============================================");
        System.out.printf(" ID             : %-27d %n", client.getId());
        System.out.printf(" Name           : %-27s %n", client.getName());
        System.out.printf(" Email          : %-27s %n", client.getEmail());
        System.out.printf(" Phone          : %-27s %n", client.getPhone());
        System.out.printf(" Company        : %-27s %n", client.getCompanyName());
        System.out.println("===============================================");
    }

    private boolean canModify(User user) {
        return user != null &&
                (user.getRole() == User.Role.ADMIN ||
                        user.getRole() == User.Role.PROJECT_MANAGER);
    }

    private String readRequired(String message) {
        while (true) {
            System.out.print(message);

            String value = scanner.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("This field is required.");
        }
    }
}