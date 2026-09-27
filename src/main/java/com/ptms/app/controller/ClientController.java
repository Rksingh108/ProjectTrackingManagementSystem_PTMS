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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientController {

    private static final Logger logger = Logger.getLogger(ClientController.class.getName());

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
            logger.info("""
                    
                    --- Client Management ---
                    0. Back
                    1. Add client
                    2. View all clients
                    3. Search clients by name
                    4. Update client
                    5. Delete client
                   
                    """);

            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addClient(loggedInUser);
                    case "2" -> viewAllClients();
                    case "3" -> searchClients();
                    case "4" -> updateClient(loggedInUser);
                    case "5" -> deleteClient(loggedInUser);
                    case "0" -> {
                        running = false;
                        logger.info("Exited client management.");
                    }
                    default -> logger.warning("Invalid menu option selected: " + choice);
                }
            } catch (UnauthorizedException | ResourceNotFoundException e) {
                logger.warning("Operation failed: " + e.getMessage());
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Database error while processing client operation.", e);
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

        logger.info("Client added successfully. Client ID: " + client.getId());
    }

    private void viewAllClients() throws SQLException {
        List<Client> clients = clientService.getAllClients();

        if (clients.isEmpty()) {
            logger.info("No clients found.");
            return;
        }

        logger.info("Retrieved " + clients.size() + " client(s).");
        clients.forEach(this::printClientSummary);
    }

    private void searchClients() throws SQLException {
        System.out.print("Search keyword: ");
        String keyword = scanner.nextLine().trim();

        List<Client> results = clientService.searchClients(keyword);

        if (results.isEmpty()) {
            logger.info("No clients found for keyword: " + keyword);
            return;
        }

        logger.info("Found " + results.size() + " client(s) for keyword: " + keyword);
        results.forEach(this::printClientSummary);
    }

    private void updateClient(User requestingUser) throws SQLException {
        System.out.print("Client id to update: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        Client client = clientService.getClientById(id);

        logger.info("Updating client with ID: " + id);
        logger.info("Leave a field blank to keep its current value.");

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

        logger.info("Client updated successfully. Client ID: " + id);
    }

    private void deleteClient(User requestingUser) throws SQLException {
        System.out.print("Client id to delete: ");
        int id = Integer.parseInt(scanner.nextLine().trim());

        clientService.deleteClient(id, requestingUser);

        logger.info("Client deleted successfully. Client ID: " + id);
    }

    private void printClientSummary(Client client) {
        logger.info(String.format(
                "Client: id=%d | name=%s | email=%s | phone=%s | company=%s",
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getCompanyName()
        ));
    }
}