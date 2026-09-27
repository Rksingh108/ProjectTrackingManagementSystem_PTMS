package com.ptms.app.controller;

import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.User;
import com.ptms.app.service.IUserService;
import com.ptms.app.service.UserService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class UserController {

    private final UserService userService;
    private final Scanner scanner;

    public UserController() {
        this.userService = new IUserService();
        this.scanner = new Scanner(System.in);
    }

    public UserController(UserService userService, Scanner scanner) {
        this.userService = userService;
        this.scanner = scanner;
    }

    public void showMenu(User loggedInUser) {
        boolean running = true;

        while (running) {
            printMenu(loggedInUser);

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "0":
                        running = false;
                        break;
                    case "1":
                        viewAllUsers(loggedInUser);
                        break;
                    case "2":
                        searchUsers(loggedInUser);
                        break;
                    case "3":
                        viewUsersByRole(loggedInUser);
                        break;
                    case "4":
                        updateProfile(loggedInUser);
                        break;
                    case "5":
                        changeRole(loggedInUser);
                        break;
                    case "6":
                        deleteUser(loggedInUser);
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
            }
        }
    }

    private void printMenu(User user) {
        System.out.println();
        System.out.println("========== USER MANAGEMENT ==========");
        System.out.println("0. Back");
        System.out.println("4. Update my profile");

        if (user.getRole() == User.Role.ADMIN) {
            System.out.println("1. View all users");
            System.out.println("2. Search users");
            System.out.println("3. View users by role");
            System.out.println("5. Change user role");
            System.out.println("6. Delete user");
        }

        System.out.println("=====================================");
        System.out.print("Choose an option: ");
    }

    public User registerUser() throws SQLException {
        System.out.println();
        System.out.println("========== REGISTER ==========");

        User user = new User();

        System.out.print("First name: ");
        user.setFirstName(scanner.nextLine().trim());

        System.out.print("Last name: ");
        user.setLastName(scanner.nextLine().trim());

        System.out.print("Username: ");
        user.setUsername(scanner.nextLine().trim());

        System.out.print("Email: ");
        user.setEmail(scanner.nextLine().trim());

        System.out.print("Password: ");
        user.setPassword(scanner.nextLine());

        try {
            User saved = userService.registerUser(user);

            System.out.println(
                    "Registration successful. Welcome, "
                            + saved.getFirstName()
            );

            return saved;
        } catch (ValidationException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return null;
        }
    }

    public User login() throws SQLException {
        System.out.println();
        System.out.println("========== LOGIN ==========");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            User user = userService.login(username, password);

            System.out.println(
                    "Login successful. Welcome, "
                            + user.getFirstName()
            );

            return user;
        } catch (ValidationException e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }

    private void viewAllUsers(User loggedInUser) throws SQLException {
        List<User> users = userService.getAllUsers(loggedInUser);

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.println();
        System.out.println("========== ALL USERS ==========");

        users.forEach(this::printUser);
    }

    private void searchUsers(User loggedInUser) throws SQLException {
        System.out.print("Search keyword: ");

        String keyword = scanner.nextLine().trim();

        List<User> users =
                userService.searchUsers(keyword, loggedInUser);

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.println();
        System.out.println("========== SEARCH RESULTS ==========");

        users.forEach(this::printUser);
    }

    private void viewUsersByRole(User loggedInUser) throws SQLException {
        User.Role role = promptForRole();

        List<User> users =
                userService.getUsersByRole(role, loggedInUser);

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.println();
        System.out.println("========== USERS BY ROLE ==========");

        users.forEach(this::printUser);
    }

    private void updateProfile(User loggedInUser) throws SQLException {
        System.out.println("Leave blank to keep current value.");

        System.out.print(
                "First name [" + loggedInUser.getFirstName() + "]: "
        );

        String firstName = scanner.nextLine().trim();

        if (!firstName.isEmpty()) {
            loggedInUser.setFirstName(firstName);
        }

        System.out.print(
                "Last name [" + loggedInUser.getLastName() + "]: "
        );

        String lastName = scanner.nextLine().trim();

        if (!lastName.isEmpty()) {
            loggedInUser.setLastName(lastName);
        }

        System.out.print(
                "Email [" + loggedInUser.getEmail() + "]: "
        );

        String email = scanner.nextLine().trim();

        if (!email.isEmpty()) {
            loggedInUser.setEmail(email);
        }

        userService.updateProfile(loggedInUser);

        System.out.println("Profile updated successfully.");
    }

    private void changeRole(User loggedInUser) throws SQLException {
        if (loggedInUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException(
                    "Only ADMIN can change roles."
            );
        }

        int userId = readPositiveInt();
        User.Role role = promptForRole();

        userService.changeRole(
                userId,
                role,
                loggedInUser
        );

        System.out.println("Role updated successfully.");
    }

    private void deleteUser(User loggedInUser) throws SQLException {
        if (loggedInUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException(
                    "Only ADMIN can delete users."
            );
        }

        int userId = readPositiveInt();

        userService.deleteUser(
                userId,
                loggedInUser
        );

        System.out.println("User deleted successfully.");
    }

    private int readPositiveInt() {
        System.out.print("User ID: ");

        try {
            int value = Integer.parseInt(
                    scanner.nextLine().trim()
            );

            if (value <= 0) {
                throw new NumberFormatException();
            }

            return value;
        } catch (NumberFormatException e) {
            throw new ValidationException(
                    "Enter a valid positive number."
            );
        }
    }

    private User.Role promptForRole() {
        while (true) {
            System.out.print(
                    "Role (ADMIN, PROJECT_MANAGER, TEAM_LEAD, TEAM_MEMBER): "
            );

            String input = scanner.nextLine()
                    .trim()
                    .toUpperCase();

            try {
                return User.Role.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid role. Try again.");
            }
        }
    }

    private void printUser(User user) {
        System.out.println("----------------------------------------");
        System.out.println("ID       : " + user.getId());
        System.out.println("Name     : " + user.getFirstName() + " " + user.getLastName());
        System.out.println("Username : " + user.getUsername());
        System.out.println("Email    : " + user.getEmail());
        System.out.println("Role     : " + user.getRole());
        System.out.println("----------------------------------------");
    }
}