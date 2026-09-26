package com.ptms.app.controller;

import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
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
            System.out.println("\n--- Client Management ---");
            System.out.println("1. Add client");
            System.out.println("2. View all clients");
            System.out.println("3. Search clients by name");
            System.out.println("4. Update client");
            System.out.println("5. Delete client");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addClient(loggedInUser);
                    case "2" -> viewAllClients();
                    case "3" -> searchClients();
                    case "4" -> updateClient(loggedInUser);
                    case "5" -> deleteClient(loggedInUser);
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option, try again.");
                }
            } catch (UnauthorizedException | ResourceNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void addClient(User requestingUser) throws SQLException {
        System.out.print("Client name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Company name: ");
        String companyName = scanner.nextLine().trim();

        Client client = new Client(name, email, phone, companyName);
        clientService.addClient(client, requestingUser);
        System.out.println("Client added, id=" + client.getId());
    }

    private void viewAllClients() throws SQLException {
        List<Client> clients = clientService.getAllClients();
        if (clients.isEmpty()) {
            System.out.println("No clients found.");
            return;
        }
        clients.forEach(this::printClientSummary);
    }

    private void searchClients() throws SQLException {
        System.out.print("Search keyword: ");
        String keyword = scanner.nextLine().trim();
        List<Client> results = clientService.searchClients(keyword);
        if (results.isEmpty()) {
            System.out.println("No matching clients.");
            return;
        }
        results.forEach(this::printClientSummary);
    }

    private void updateClient(User requestingUser) throws SQLException {
        System.out.print("Client id to update: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Client client = clientService.getClientById(id);

        System.out.println("Leave a field blank to keep its current value.");

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

        System.out.print("Company name [" + client.getCompanyName() + "]: ");
        String companyName = scanner.nextLine().trim();
        if (!companyName.isEmpty()) {
            client.setCompanyName(companyName);
        }

        clientService.updateClient(client, requestingUser);
        System.out.println("Client updated.");
    }

    private void deleteClient(User requestingUser) throws SQLException {
        System.out.print("Client id to delete: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        clientService.deleteClient(id, requestingUser);
        System.out.println("Client deleted.");
    }

    private void printClientSummary(Client client) {
        System.out.printf("id=%d | %s | email=%s | phone=%s | company=%s%n",
                client.getId(), client.getName(), client.getEmail(), client.getPhone(), client.getCompanyName());
    }
}