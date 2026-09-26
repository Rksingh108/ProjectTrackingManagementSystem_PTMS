package com.ptms.app.controller;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.User;
import com.ptms.app.service.IUserService;
import com.ptms.app.service.UserService;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
/**
 * Console entry point for user-management actions.
 * Takes the currently logged-in user so it can pass it to the service for
 * the role checks — the controller itself doesn't decide who's allowed to
 * do what, it just relays the outcome (success message or error) to the console.
 */
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
    /** Call this after login, passing whoever is currently signed in. */
    public void showMenu(User loggedInUser) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- User Management ---");
            System.out.println("1. View all users");
            System.out.println("2. Search users by name");
            System.out.println("3. View users by role");
            System.out.println("4. Update my profile");
            System.out.println("5. Change a user's role (Admin only)");
            System.out.println("6. Delete a user (Admin only)");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> viewAllUsers();
                    case "2" -> searchUsers();
                    case "3" -> viewUsersByRole();
                    case "4" -> updateProfile(loggedInUser);
                    case "5" -> changeRole(loggedInUser);
                    case "6" -> deleteUser(loggedInUser);
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option, try again.");
                }
            } catch (UnauthorizedException | ValidationException | ResourceNotFoundException e) {
                // Expected, user-facing failures — show the message, don't crash the menu.
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }
    /** Registration happens before login, so it takes no logged-in user. */
    public User registerUser() throws SQLException {
        System.out.println("\n--- Register New User ---");
        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        User.Role role = promptForRole();
        User newUser = new User(firstName, lastName, username, email, password, role);
        try {
            User saved = userService.registerUser(newUser);
            System.out.println("Registered successfully. Welcome, " + saved.getFirstName() + "!");
            return saved;
        } catch (ValidationException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return null;
        }
    }
    public User login() throws SQLException {
        System.out.println("\n--- Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        try {
            User user = userService.login(username, password);
            System.out.println("Login successful. Welcome back, " + user.getFirstName() + "!");
            return user;
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    private void viewAllUsers() throws SQLException {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        users.forEach(this::printUserSummary);
    }
    private void searchUsers() throws SQLException {
        System.out.print("Search keyword: ");
        String keyword = scanner.nextLine().trim();
        List<User> results = userService.searchUsers(keyword);
        if (results.isEmpty()) {
            System.out.println("No matching users.");
            return;
        }
        results.forEach(this::printUserSummary);
    }

    private void viewUsersByRole() throws SQLException {
        User.Role role = promptForRole();
        List<User> users = userService.getUsersByRole(role);
        if (users.isEmpty()) {
            System.out.println("No users with role " + role + ".");
            return;
        }
        users.forEach(this::printUserSummary);
    }

    private void updateProfile(User loggedInUser) throws SQLException {
        System.out.println("Leave a field blank to keep its current value.");

        System.out.print("First name [" + loggedInUser.getFirstName() + "]: ");
        String firstName = scanner.nextLine().trim();
        if (!firstName.isEmpty()) {
            loggedInUser.setFirstName(firstName);
        }

        System.out.print("Last name [" + loggedInUser.getLastName() + "]: ");
        String lastName = scanner.nextLine().trim();
        if (!lastName.isEmpty()) {
            loggedInUser.setLastName(lastName);
        }

        System.out.print("Email [" + loggedInUser.getEmail() + "]: ");
        String email = scanner.nextLine().trim();
        if (!email.isEmpty()) {
            loggedInUser.setEmail(email);
        }

        userService.updateUser(loggedInUser);
        System.out.println("Profile updated.");
    }

    private void changeRole(User requestingUser) throws SQLException {
        System.out.print("User id whose role you want to change: ");
        int userId = Integer.parseInt(scanner.nextLine().trim());
        User.Role newRole = promptForRole();

        userService.changeRole(userId, newRole, requestingUser);
        System.out.println("Role updated.");
    }

    private void deleteUser(User requestingUser) throws SQLException {
        System.out.print("User id to delete: ");
        int userId = Integer.parseInt(scanner.nextLine().trim());

        userService.deleteUser(userId, requestingUser);
        System.out.println("User deleted.");
    }

    private User.Role promptForRole() {
        while (true) {
            System.out.print("Role (ADMIN, PROJECT_MANAGER, TEAM_LEAD, TEAM_MEMBER): ");
            String input = scanner.nextLine().trim().toUpperCase();
            try {
                return User.Role.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Not a valid role, try again.");
            }
        }
    }

    private void printUserSummary(User user) {
        System.out.printf("id=%d | %s %s | username=%s | email=%s | role=%s%n",
                user.getId(), user.getFirstName(), user.getLastName(),
                user.getUsername(), user.getEmail(), user.getRole());
    }
}
