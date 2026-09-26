package com.ptms.app;
import com.ptms.app.controller.ClientsController;
import com.ptms.app.controller.UserController;

import java.util.Scanner;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        logger.info("PTMS application starting.");

        try (Scanner scanner = new Scanner(System.in)) {
            UserController userController = new UserController();
            ClientsController clientsController = new ClientsController();

            boolean running = true;
            while (running) {
                System.out.println("""
                        ======= PTMS Main Menu =======
                        1. User Management
                        2. Client Management
                        0. Exit
                        """);
                System.out.print("Choose an option: ");
                String input = scanner.nextLine().trim();

                switch (input) {
                    case "1" -> userController.handleMenu(scanner);
                    case "2" -> clientsController.handleMenu(scanner);
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option, please try again.");
                }
            }
        } catch (Exception e) {
            logger.severe(() -> "Fatal error: " + e.getMessage());
        } finally {
            logger.info("PTMS application exited.");
        }
    }
}