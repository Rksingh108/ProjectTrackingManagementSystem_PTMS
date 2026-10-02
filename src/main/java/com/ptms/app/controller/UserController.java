package com.ptms.app.controller;

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
        while (true) {
            System.out.println("\n===== USER MANAGEMENT =====");
            System.out.println("1. View All Users");
            System.out.println("2. Search Users");
            System.out.println("3. View Users By Role");
            System.out.println("4. Update Profile");
            System.out.println("5. Change User Role");
            System.out.println("6. Delete User");
            System.out.println("0. Back");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
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
                    case "0":
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (SQLException | RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public User registerUser() {
        System.out.println("\n===== USER REGISTRATION =====");

        try {
            System.out.print("First Name: ");
            String firstName = scanner.nextLine();

            System.out.print("Last Name: ");
            String lastName = scanner.nextLine();

            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            System.out.println("\nSelect Role:");
            System.out.println("1. ADMIN");
            System.out.println("2. PROJECT_MANAGER");
            System.out.println("3. TEAM_LEAD");
            System.out.println("4. TEAM_MEMBER");
            System.out.print("Choose role: ");

            String roleChoice = scanner.nextLine();

            User.Role role = promptForRegistrationRole(roleChoice);

            User user = new User();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);
            user.setRole(role);

            User registeredUser = userService.registerUser(user);

            System.out.println("\nUser registered successfully.");
            System.out.println("User ID: " + registeredUser.getId());
            System.out.println("Username: " + registeredUser.getUsername());
            System.out.println("Role: " + registeredUser.getRole());

            return registeredUser;

        } catch (SQLException | RuntimeException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return null;
        }
    }

    public User login() {
        System.out.println("\n===== LOGIN =====");

        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            User user = userService.login(username, password);

            System.out.println("\nLogin successful.");
            System.out.println("Welcome, " + user.getFirstName());
            System.out.println("Role: " + user.getRole());

            return user;

        } catch (SQLException | RuntimeException e) {
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

        System.out.println("\n===== ALL USERS =====");

        for (User user : users) {
            printUser(user);
        }
    }

    private void searchUsers(User loggedInUser) throws SQLException {
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine();

        List<User> users = userService.searchUsers(keyword, loggedInUser);

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        System.out.println("\n===== SEARCH RESULTS =====");

        for (User user : users) {
            printUser(user);
        }
    }

    private void viewUsersByRole(User loggedInUser) throws SQLException {
        User.Role role = promptForRole();

        List<User> users = userService.getUsersByRole(role, loggedInUser);

        if (users.isEmpty()) {
            System.out.println("No users found with role " + role);
            return;
        }

        System.out.println("\n===== USERS WITH ROLE " + role + " =====");

        for (User user : users) {
            printUser(user);
        }
    }

    private void updateProfile(User loggedInUser) throws SQLException {
        System.out.println("\n===== UPDATE PROFILE =====");

        User user = new User();
        user.setId(loggedInUser.getId());

        System.out.print("First Name: ");
        user.setFirstName(scanner.nextLine());

        System.out.print("Last Name: ");
        user.setLastName(scanner.nextLine());

        System.out.print("Email: ");
        user.setEmail(scanner.nextLine());

        System.out.print("Date of Birth (YYYY-MM-DD): ");
        String dob = scanner.nextLine();

        if (!dob.trim().isEmpty()) {
            user.setDateOfBirth(java.time.LocalDate.parse(dob));
        }

        System.out.print("Mobile Number: ");
        user.setMobileNumber(scanner.nextLine());

        System.out.print("Gender: ");
        user.setGender(scanner.nextLine());

        userService.updateProfile(user, loggedInUser);

        System.out.println("Profile updated successfully.");
    }

    private void changeRole(User loggedInUser) throws SQLException {
        System.out.println("\n===== CHANGE USER ROLE =====");

        System.out.print("Enter User ID: ");
        int userId = Integer.parseInt(scanner.nextLine());

        User.Role role = promptForRole();

        userService.changeRole(userId, role, loggedInUser);

        System.out.println("User role updated successfully.");
    }

    private void deleteUser(User loggedInUser) throws SQLException {
        System.out.println("\n===== DELETE USER =====");

        System.out.print("Enter User ID: ");
        int userId = Integer.parseInt(scanner.nextLine());

        System.out.print("Are you sure? (yes/no): ");
        String confirmation = scanner.nextLine();

        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("Delete operation cancelled.");
            return;
        }

        userService.deleteUser(userId, loggedInUser);

        System.out.println("User deleted successfully.");
    }

    private User.Role promptForRegistrationRole(String choice) {
        switch (choice) {
            case "1":
                return User.Role.ADMIN;
            case "2":
                return User.Role.PROJECT_MANAGER;
            case "3":
                return User.Role.TEAM_LEAD;
            case "4":
                return User.Role.TEAM_MEMBER;
            default:
                throw new IllegalArgumentException("Invalid role choice.");
        }
    }

    private User.Role promptForRole() {
        System.out.println("\nSelect Role:");
        System.out.println("1. ADMIN");
        System.out.println("2. PROJECT_MANAGER");
        System.out.println("3. TEAM_LEAD");
        System.out.println("4. TEAM_MEMBER");
        System.out.print("Choose role: ");

        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                return User.Role.ADMIN;
            case "2":
                return User.Role.PROJECT_MANAGER;
            case "3":
                return User.Role.TEAM_LEAD;
            case "4":
                return User.Role.TEAM_MEMBER;
            default:
                throw new IllegalArgumentException("Invalid role choice.");
        }
    }

    private void printUser(User user) {
        System.out.println("--------------------------------");
        System.out.println("ID       : " + user.getId());
        System.out.println("Name     : " +
                user.getFirstName() + " " + user.getLastName());
        System.out.println("Username : " + user.getUsername());
        System.out.println("Email    : " + user.getEmail());
        System.out.println("Role     : " + user.getRole());
    }
}