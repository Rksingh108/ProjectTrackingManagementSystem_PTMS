package com.ptms.app.controller;

import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Client;
import com.ptms.app.model.User;
import com.ptms.app.service.ClientService;
import com.ptms.app.service.IClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ClientController {
    private static final Logger logger=LoggerFactory.getLogger(ClientController.class);
    private final ClientService clientService;
    private final Scanner scanner;

    public ClientController(){
        this.clientService=new IClientService();
        this.scanner=new Scanner(System.in);
    }

    public ClientController(ClientService clientService,Scanner scanner){
        this.clientService=clientService;
        this.scanner=scanner;
    }

    public void showMenu(User loggedInUser){
        if(loggedInUser==null||loggedInUser.getRole()!=User.Role.ADMIN){
            logger.warn("Unauthorized Client Management access attempt.");
            System.out.println("Only ADMIN can access Client Management.");
            return;
        }

        boolean running=true;
        logger.info("Client module started for user: {}",loggedInUser.getUsername());

        while(running){
            try{
                showDashboard(loggedInUser);
                String choice=scanner.nextLine().trim();

                switch(choice){
                    case "1"->viewAllClients(loggedInUser);
                    case "2"->searchClients(loggedInUser);
                    case "3"->viewClientDetails(loggedInUser);
                    case "4"->addClient(loggedInUser);
                    case "5"->updateClient(loggedInUser);
                    case "6"->deleteClient(loggedInUser);
                    case "0"->{
                        logger.info("User {} exited Client module",loggedInUser.getUsername());
                        running=false;
                    }
                    default->{
                        logger.warn("Invalid Client Management option selected by user {}",loggedInUser.getUsername());
                        System.out.println("Invalid option. Please try again.");
                    }
                }
            }catch(UnauthorizedException|ResourceNotFoundException|ValidationException e){
                logger.warn("Client operation failed for user {}: {}",loggedInUser.getUsername(),e.getMessage());
                System.out.println("Operation failed: "+e.getMessage());
            }catch(SQLException e){
                logger.error("Database error during Client operation for user {}",loggedInUser.getUsername(),e);
                System.out.println("Database error: "+e.getMessage());
            }catch(NumberFormatException e){
                logger.warn("Invalid numeric input provided by user {}",loggedInUser.getUsername());
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void showDashboard(User user)throws SQLException{
        int totalClients=clientService.getTotalClients(user);
        int totalCompanies=clientService.getTotalCompanies(user);

        System.out.println();
        System.out.println("======================================");
        System.out.println("          CLIENT MANAGEMENT");
        System.out.println("======================================");
        System.out.println("Logged in as : "+user.getUsername());
        System.out.println("Role         : "+user.getRole());
        System.out.println("--------------------------------------");
        System.out.println("Total Clients   : "+totalClients);
        System.out.println("Total Companies : "+totalCompanies);
        System.out.println("--------------------------------------");
        System.out.println("1. View All Clients");
        System.out.println("2. Search Clients");
        System.out.println("3. View Client Details");
        System.out.println("4. Add Client");
        System.out.println("5. Update Client");
        System.out.println("6. Delete Client");
        System.out.println("0. Back");
        System.out.println("======================================");
        System.out.print("Choose an option: ");
    }

    private void viewAllClients(User user)throws SQLException{
        List<Client> clients=clientService.getAllClients(user);
        logger.info("User {} viewed all clients. Total results: {}",user.getUsername(),clients.size());

        if(clients.isEmpty()){
            System.out.println("No clients found.");
            return;
        }
        printClientTable(clients);
    }

    private void searchClients(User user)throws SQLException{
        System.out.print("Enter name, email, phone or company: ");
        String keyword=scanner.nextLine().trim();
        List<Client> clients=clientService.searchClients(keyword,user);

        logger.info("User {} searched clients using keyword '{}'. Results: {}",user.getUsername(),keyword,clients.size());

        if(clients.isEmpty()){
            System.out.println("No clients found.");
            return;
        }
        printClientTable(clients);
    }

    private void viewClientDetails(User user)throws SQLException{
        System.out.print("Enter client ID: ");
        int id=Integer.parseInt(scanner.nextLine().trim());
        Client client=clientService.getClientById(id,user);

        logger.info("User {} viewed details of client ID {}",user.getUsername(),id);
        printClientDetails(client);
    }

    private void addClient(User user)throws SQLException{
        System.out.println();
        System.out.println("========== ADD CLIENT ==========");

        String name=readRequired("Client Name: ");
        String email=readRequired("Email: ");
        String phone=readRequired("Phone: ");
        String company=readRequired("Company Name: ");

        Client client=new Client(name,email,phone,company);
        Client created=clientService.addClient(client,user);

        logger.info("User {} added client successfully. Client ID: {}",user.getUsername(),created.getId());

        System.out.println("Client created successfully.");
        System.out.println("Client ID: "+created.getId());
    }

    private void updateClient(User user)throws SQLException{
        System.out.print("Enter client ID: ");
        int id=Integer.parseInt(scanner.nextLine().trim());
        Client client=clientService.getClientById(id,user);

        System.out.println("Leave a field empty to keep the current value.");

        System.out.print("Name ["+client.getName()+"]: ");
        String name=scanner.nextLine().trim();
        if(!name.isEmpty())client.setName(name);

        System.out.print("Email ["+client.getEmail()+"]: ");
        String email=scanner.nextLine().trim();
        if(!email.isEmpty())client.setEmail(email);

        System.out.print("Phone ["+client.getPhone()+"]: ");
        String phone=scanner.nextLine().trim();
        if(!phone.isEmpty())client.setPhone(phone);

        System.out.print("Company ["+client.getCompanyName()+"]: ");
        String company=scanner.nextLine().trim();
        if(!company.isEmpty())client.setCompanyName(company);

        clientService.updateClient(client,user);

        logger.info("User {} updated client successfully. Client ID: {}",user.getUsername(),id);
        System.out.println("Client updated successfully.");
    }

    private void deleteClient(User user)throws SQLException{
        System.out.print("Enter client ID: ");
        int id=Integer.parseInt(scanner.nextLine().trim());
        Client client=clientService.getClientById(id,user);

        printClientDetails(client);
        System.out.print("Confirm deletion (Y/N): ");
        String confirmation=scanner.nextLine().trim();

        if(!confirmation.equalsIgnoreCase("Y")){
            logger.info("User {} cancelled deletion of client ID {}",user.getUsername(),id);
            System.out.println("Delete operation cancelled.");
            return;
        }

        clientService.deleteClient(id,user);
        logger.info("User {} deleted client successfully. Client ID: {}",user.getUsername(),id);
        System.out.println("Client deleted successfully.");
    }

    private void printClientTable(List<Client> clients){
        System.out.println();
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-6s %-22s %-30s %-15s %-25s%n","ID","NAME","EMAIL","PHONE","COMPANY");
        System.out.println("--------------------------------------------------------------------------------");

        for(Client client:clients){
            System.out.printf("%-6d %-22s %-30s %-15s %-25s%n",client.getId(),client.getName(),client.getEmail(),client.getPhone(),client.getCompanyName());
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("Total results: "+clients.size());
    }

    private void printClientDetails(Client client){
        System.out.println();
        System.out.println("======================================");
        System.out.println("           CLIENT DETAILS");
        System.out.println("======================================");
        System.out.println("ID      : "+client.getId());
        System.out.println("Name    : "+client.getName());
        System.out.println("Email   : "+client.getEmail());
        System.out.println("Phone   : "+client.getPhone());
        System.out.println("Company : "+client.getCompanyName());
        System.out.println("======================================");
    }

    private String readRequired(String message){
        while(true){
            System.out.print(message);
            String value=scanner.nextLine().trim();
            if(!value.isEmpty())return value;
            System.out.println("This field is required.");
        }
    }
}