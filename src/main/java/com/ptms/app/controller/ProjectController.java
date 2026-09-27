package com.ptms.app.controller;

import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Project;
import com.ptms.app.model.User;
import com.ptms.app.service.IProjectService;
import com.ptms.app.service.ProjectService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ProjectController {

    private final ProjectService projectService;
    private final Scanner scanner;

    public ProjectController() {
        this.projectService = new IProjectService();
        this.scanner = new Scanner(System.in);
    }

    public ProjectController(ProjectService projectService, Scanner scanner) {
        this.projectService = projectService;
        this.scanner = scanner;
    }

    public void showMenu(User loggedInUser) {
        boolean running = true;

        while (running) {
            printMenu(loggedInUser);
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        createProject(loggedInUser);
                        break;
                    case "2":
                        viewMyProjects(loggedInUser);
                        break;
                    case "3":
                        viewAllProjects(loggedInUser);
                        break;
                    case "4":
                        viewProjectDetails(loggedInUser);
                        break;
                    case "5":
                        assignTeamLead(loggedInUser);
                        break;
                    case "6":
                        updateProject(loggedInUser);
                        break;
                    case "7":
                        deleteProject(loggedInUser);
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (UnauthorizedException | ValidationException | ResourceNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void printMenu(User user) {
        System.out.println();
        System.out.println("========== PROJECT MANAGEMENT ==========");
        System.out.println("Logged in as: " + user.getUsername() + " | Role: " + user.getRole());
        System.out.println();

        switch (user.getRole()) {
            case ADMIN:
                System.out.println("1. Create Project");
                System.out.println("2. View My Projects");
                System.out.println("3. View All Projects");
                System.out.println("4. View Project Details");
                System.out.println("5. Assign Team Lead");
                System.out.println("6. Update Project");
                System.out.println("7. Delete Project");
                break;

            case PROJECT_MANAGER:
                System.out.println("1. Create Project");
                System.out.println("2. View My Projects");
                System.out.println("4. View Project Details");
                System.out.println("5. Assign Team Lead");
                System.out.println("6. Update Project");
                break;

            case TEAM_LEAD:
                System.out.println("2. View My Projects");
                System.out.println("4. View Project Details");
                break;

            case TEAM_MEMBER:
                System.out.println("2. View My Projects");
                System.out.println("4. View Project Details");
                break;
        }

        System.out.println("0. Back");
        System.out.println();
        System.out.print("Choose an option: ");
    }

    private void createProject(User requestingUser) throws SQLException {
        System.out.println();
        System.out.println("========== CREATE PROJECT ==========");

        System.out.print("Project name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Requirements: ");
        String requirements = scanner.nextLine().trim();

        System.out.print("Domain: ");
        String domain = scanner.nextLine().trim();

        System.out.print("Cost: ");
        BigDecimal cost = parseBigDecimal(scanner.nextLine().trim());

        System.out.print("Start date (YYYY-MM-DD): ");
        LocalDate startDate = parseDate(scanner.nextLine().trim());

        System.out.print("Deadline (YYYY-MM-DD): ");
        LocalDate deadline = parseDate(scanner.nextLine().trim());

        System.out.print("Priority (LOW/MEDIUM/HIGH): ");
        String priority = scanner.nextLine().trim().toUpperCase();

        Project project = new Project(
                name,
                requirements,
                requestingUser.getId(),
                priority
        );

        project.setDomain(domain);
        project.setCost(cost);
        project.setStartDate(startDate);
        project.setDeadline(deadline);

        projectService.createProject(project, requestingUser);

        System.out.println("Project created successfully.");
        System.out.println("Project ID: " + project.getId());
    }

    private void viewMyProjects(User requestingUser) throws SQLException {
        List<Project> projects = projectService.getProjectsForUser(requestingUser);

        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }

        System.out.println();
        System.out.println("========== MY PROJECTS ==========");

        projects.forEach(this::printProject);
    }

    private void viewAllProjects(User requestingUser) throws SQLException {
        List<Project> projects = projectService.getAllProjects(requestingUser);

        if (projects.isEmpty()) {
            System.out.println("No projects found.");
            return;
        }

        System.out.println();
        System.out.println("========== ALL PROJECTS ==========");

        projects.forEach(this::printProject);
    }

    private void viewProjectDetails(User requestingUser) throws SQLException {
        int projectId = readProjectId();
        Project project = projectService.getProjectById(projectId, requestingUser);

        System.out.println();
        System.out.println("========== PROJECT DETAILS ==========");
        System.out.println("ID           : " + project.getId());
        System.out.println("Name         : " + project.getName());
        System.out.println("Requirements : " + project.getRequirements());
        System.out.println("Manager ID   : " + project.getManagerId());
        System.out.println("Team Lead ID : " + project.getTeamLeadId());
        System.out.println("Client ID    : " + project.getClientId());
        System.out.println("Domain       : " + project.getDomain());
        System.out.println("Cost         : " + project.getCost());
        System.out.println("Start Date   : " + project.getStartDate());
        System.out.println("Deadline     : " + project.getDeadline());
        System.out.println("Priority     : " + project.getPriority());
        System.out.println("Status       : " + project.getStatus());
    }

    private void assignTeamLead(User requestingUser) throws SQLException {
        int projectId = readProjectId();

        System.out.print("Team Lead User ID: ");
        int teamLeadId = Integer.parseInt(scanner.nextLine().trim());

        projectService.assignTeamLead(
                projectId,
                teamLeadId,
                requestingUser
        );

        System.out.println("Team Lead assigned successfully.");
    }

    private void updateProject(User requestingUser) throws SQLException {
        int projectId = readProjectId();
        Project project = projectService.getProjectById(projectId, requestingUser);

        System.out.println();
        System.out.println("Leave blank to keep current value.");

        System.out.print("Name [" + project.getName() + "]: ");
        String name = scanner.nextLine().trim();

        if (!name.isEmpty()) {
            project.setName(name);
        }

        System.out.print("Requirements [" + project.getRequirements() + "]: ");
        String requirements = scanner.nextLine().trim();

        if (!requirements.isEmpty()) {
            project.setRequirements(requirements);
        }

        System.out.print("Domain [" + project.getDomain() + "]: ");
        String domain = scanner.nextLine().trim();

        if (!domain.isEmpty()) {
            project.setDomain(domain);
        }

        System.out.print("Priority [" + project.getPriority() + "]: ");
        String priority = scanner.nextLine().trim();

        if (!priority.isEmpty()) {
            project.setPriority(priority.toUpperCase());
        }

        System.out.print("Status [" + project.getStatus() + "]: ");
        String status = scanner.nextLine().trim();

        if (!status.isEmpty()) {
            project.setStatus(status);
        }

        System.out.print("Cost [" + project.getCost() + "]: ");
        String costInput = scanner.nextLine().trim();

        if (!costInput.isEmpty()) {
            project.setCost(parseBigDecimal(costInput));
        }

        System.out.print("Start date [" + project.getStartDate() + "]: ");
        String startDateInput = scanner.nextLine().trim();

        if (!startDateInput.isEmpty()) {
            project.setStartDate(parseDate(startDateInput));
        }

        System.out.print("Deadline [" + project.getDeadline() + "]: ");
        String deadlineInput = scanner.nextLine().trim();

        if (!deadlineInput.isEmpty()) {
            project.setDeadline(parseDate(deadlineInput));
        }

        projectService.updateProject(project, requestingUser);

        System.out.println("Project updated successfully.");
    }

    private void deleteProject(User requestingUser) throws SQLException {
        int projectId = readProjectId();

        System.out.print("Are you sure? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!confirmation.equals("yes")) {
            System.out.println("Delete cancelled.");
            return;
        }

        projectService.deleteProject(projectId, requestingUser);

        System.out.println("Project deleted successfully.");
    }

    private int readProjectId() {
        System.out.print("Project ID: ");
        return Integer.parseInt(scanner.nextLine().trim());
    }

    private BigDecimal parseBigDecimal(String input) {
        if (input.isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            throw new ValidationException("Cost must be a valid number.");
        }
    }

    private LocalDate parseDate(String input) {
        if (input.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(input);
        } catch (Exception e) {
            throw new ValidationException("Date must use YYYY-MM-DD format.");
        }
    }

    private void printProject(Project project) {
        System.out.println("----------------------------------------");
        System.out.println("ID          : " + project.getId());
        System.out.println("Name        : " + project.getName());
        System.out.println("Manager ID  : " + project.getManagerId());
        System.out.println("Team Lead   : " + project.getTeamLeadId());
        System.out.println("Client ID   : " + project.getClientId());
        System.out.println("Status      : " + project.getStatus());
        System.out.println("Priority    : " + project.getPriority());
    }
}