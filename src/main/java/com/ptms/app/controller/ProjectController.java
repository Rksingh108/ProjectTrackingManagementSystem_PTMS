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
import java.util.logging.Logger;

public class ProjectController {

    private static final Logger logger =
            Logger.getLogger(ProjectController.class.getName());

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
            logger.info("""
                    
                    --- Project Management ---
                    1. Create project
                    2. View my projects
                    3. View all projects
                    4. View project details
                    5. Assign team lead
                    6. Update project
                    7. Delete project
                    0. Back
                    """);

            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> createProject(loggedInUser);
                    case "2" -> viewMyProjects(loggedInUser);
                    case "3" -> viewAllProjects();
                    case "4" -> viewProjectDetails();
                    case "5" -> assignTeamLead(loggedInUser);
                    case "6" -> updateProject(loggedInUser);
                    case "7" -> deleteProject(loggedInUser);
                    case "0" -> running = false;
                    default -> logger.warning(
                            "Invalid option, try again."
                    );
                }
            } catch (UnauthorizedException |
                     ValidationException |
                     ResourceNotFoundException e) {

                logger.warning("Error: " + e.getMessage());

            } catch (SQLException e) {

                logger.severe(
                        "Database error: " + e.getMessage()
                );
            }
        }
    }

    private void createProject(User requestingUser)
            throws SQLException {

        System.out.print("Project name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Requirements: ");
        String requirements = scanner.nextLine().trim();

        System.out.print("Domain: ");
        String domain = scanner.nextLine().trim();

        System.out.print("Cost: ");
        BigDecimal cost =
                parseBigDecimalOrNull(scanner.nextLine().trim());

        System.out.print(
                "Start date (YYYY-MM-DD, blank to skip): "
        );
        LocalDate startDate =
                parseDateOrNull(scanner.nextLine().trim());

        System.out.print(
                "Deadline (YYYY-MM-DD, blank to skip): "
        );
        LocalDate deadline =
                parseDateOrNull(scanner.nextLine().trim());

        System.out.print(
                "Priority (LOW, MEDIUM, HIGH): "
        );
        String priority =
                scanner.nextLine().trim().toUpperCase();

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

        projectService.createProject(
                project,
                requestingUser
        );

        logger.info(
                "Project created, id=" + project.getId()
        );
    }

    private void viewMyProjects(User requestingUser)
            throws SQLException {

        List<Project> projects =
                projectService.getProjectsForUser(requestingUser);

        if (projects.isEmpty()) {
            logger.info("No projects found for you.");
            return;
        }

        projects.forEach(this::printProjectSummary);
    }

    private void viewAllProjects()
            throws SQLException {

        List<Project> projects =
                projectService.getAllProjects();

        if (projects.isEmpty()) {
            logger.info("No projects found.");
            return;
        }

        projects.forEach(this::printProjectSummary);
    }

    private void viewProjectDetails()
            throws SQLException {

        System.out.print("Project id: ");

        int id = Integer.parseInt(
                scanner.nextLine().trim()
        );

        Project project =
                projectService.getProjectById(id);

        logger.info(project.toString());
    }

    private void assignTeamLead(User requestingUser)
            throws SQLException {

        System.out.print("Project id: ");

        int projectId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        System.out.print("Team lead user id: ");

        int teamLeadId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        projectService.assignTeamLead(
                projectId,
                teamLeadId,
                requestingUser
        );

        logger.info("Team lead assigned.");
    }

    private void updateProject(User requestingUser)
            throws SQLException {

        System.out.print("Project id to update: ");

        int id = Integer.parseInt(
                scanner.nextLine().trim()
        );

        Project project =
                projectService.getProjectById(id);

        logger.info(
                "Leave a field blank to keep its current value."
        );

        System.out.print(
                "Name [" + project.getName() + "]: "
        );

        String name = scanner.nextLine().trim();

        if (!name.isEmpty()) {
            project.setName(name);
        }

        System.out.print(
                "Status [" + project.getStatus() + "]: "
        );

        String status = scanner.nextLine().trim();

        if (!status.isEmpty()) {
            project.setStatus(status);
        }

        System.out.print(
                "Priority [" + project.getPriority() + "]: "
        );

        String priority = scanner.nextLine().trim();

        if (!priority.isEmpty()) {
            project.setPriority(
                    priority.toUpperCase()
            );
        }

        projectService.updateProject(
                project,
                requestingUser
        );

        logger.info("Project updated.");
    }

    private void deleteProject(User requestingUser)
            throws SQLException {

        System.out.print("Project id to delete: ");

        int id = Integer.parseInt(
                scanner.nextLine().trim()
        );

        projectService.deleteProject(
                id,
                requestingUser
        );

        logger.info("Project deleted.");
    }

    private BigDecimal parseBigDecimalOrNull(String input) {

        if (input.isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(input);

        } catch (NumberFormatException e) {

            logger.warning(
                    "Not a valid number, leaving cost blank."
            );

            return null;
        }
    }

    private LocalDate parseDateOrNull(String input) {

        if (input.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(input);

        } catch (Exception e) {

            logger.warning(
                    "Not a valid date. Expected YYYY-MM-DD, leaving blank."
            );

            return null;
        }
    }

    private void printProjectSummary(Project project) {

        logger.info(
                String.format(
                        "id=%d | %s | manager=%d | teamLead=%s | status=%s | priority=%s",
                        project.getId(),
                        project.getName(),
                        project.getManagerId(),
                        project.getTeamLeadId(),
                        project.getStatus(),
                        project.getPriority()
                )
        );
    }
}