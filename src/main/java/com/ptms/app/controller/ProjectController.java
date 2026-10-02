package com.ptms.app.controller;

import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Project;
import com.ptms.app.model.ProjectMember;
import com.ptms.app.model.User;
import com.ptms.app.service.IProjectMemberService;
import com.ptms.app.service.IProjectService;
import com.ptms.app.service.ProjectMemberService;
import com.ptms.app.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;
    private final ProjectMemberService projectMemberService;
    private final Scanner scanner;

    public ProjectController() {
        this.projectService = new IProjectService();
        this.projectMemberService = new IProjectMemberService();
        this.scanner = new Scanner(System.in);
    }

    public ProjectController(ProjectService projectService, ProjectMemberService projectMemberService, Scanner scanner) {
        this.projectService = projectService;
        this.projectMemberService = projectMemberService;
        this.scanner = scanner;
    }

    public void showMenu(User loggedInUser) {
        if (loggedInUser == null) {
            System.out.println("User must be logged in.");
            return;
        }

        boolean running = true;

        while (running) {
            printMenu(loggedInUser);
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        if (loggedInUser.getRole() == User.Role.ADMIN
                                || loggedInUser.getRole() == User.Role.PROJECT_MANAGER) {
                            createProject(loggedInUser);
                        } else {
                            throw new UnauthorizedException("Only ADMIN or PROJECT_MANAGER can create projects.");
                        }
                        break;

                    case "2":
                        viewMyProjects(loggedInUser);
                        break;

                    case "3":
                        if (loggedInUser.getRole() == User.Role.ADMIN) {
                            viewAllProjects(loggedInUser);
                        } else {
                            throw new UnauthorizedException("Only ADMIN can view all projects.");
                        }
                        break;

                    case "4":
                        viewProjectDetails(loggedInUser);
                        break;

                    case "5":
                        if (loggedInUser.getRole() == User.Role.ADMIN) {
                            assignProjectManager(loggedInUser);
                        } else if (loggedInUser.getRole() == User.Role.PROJECT_MANAGER) {
                            assignTeamLead(loggedInUser);
                        } else {
                            throw new UnauthorizedException("You are not authorized for this option.");
                        }
                        break;

                    case "6":
                        if (loggedInUser.getRole() == User.Role.ADMIN
                                || loggedInUser.getRole() == User.Role.PROJECT_MANAGER) {
                            updateProject(loggedInUser);
                        } else {
                            throw new UnauthorizedException("Only ADMIN or PROJECT_MANAGER can update projects.");
                        }
                        break;

                    case "7":
                        if (loggedInUser.getRole() == User.Role.PROJECT_MANAGER) {
                            approveProjectCompletion(loggedInUser);
                        } else if (loggedInUser.getRole() == User.Role.TEAM_LEAD
                                || loggedInUser.getRole() == User.Role.ADMIN) {
                            addTeamMember(loggedInUser);
                        } else {
                            throw new UnauthorizedException("You are not authorized for this option.");
                        }
                        break;

                    case "8":
                        viewTeam(loggedInUser);
                        break;

                    case "9":
                        if (loggedInUser.getRole() == User.Role.TEAM_LEAD
                                || loggedInUser.getRole() == User.Role.ADMIN) {
                            removeTeamMember(loggedInUser);
                        } else if (loggedInUser.getRole() == User.Role.ADMIN) {
                            deleteProject(loggedInUser);
                        } else {
                            throw new UnauthorizedException("You are not authorized for this option.");
                        }
                        break;

                    case "10":
                        if (loggedInUser.getRole() == User.Role.ADMIN) {
                            deleteProject(loggedInUser);
                        } else {
                            throw new UnauthorizedException("Only ADMIN can delete projects.");
                        }
                        break;

                    case "0":
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid option.");
                }
            } catch (UnauthorizedException | ValidationException | ResourceNotFoundException e) {
                logger.warn(
                        "Project operation failed for user {}: {}",
                        getUsername(loggedInUser),
                        e.getMessage()
                );
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                logger.error(
                        "Database error during project operation for user {}",
                        getUsername(loggedInUser),
                        e
                );
                System.out.println("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private void printMenu(User user) {
        System.out.println();
        System.out.println("========== PROJECT MANAGEMENT ==========");
        System.out.println(
                "Logged in as: " + user.getUsername() + " | Role: " + user.getRole()
        );
        System.out.println();

        switch (user.getRole()) {
            case ADMIN:
                System.out.println("1. Create Project");
                System.out.println("2. View My Projects");
                System.out.println("3. View All Projects");
                System.out.println("4. View Project Details");
                System.out.println("5. Assign Project Manager");
                System.out.println("6. Update Project");
                System.out.println("7. Add Team Member");
                System.out.println("8. View Team");
                System.out.println("10. Delete Project");
                break;

            case PROJECT_MANAGER:
                System.out.println("1. Create Project");
                System.out.println("2. View My Projects");
                System.out.println("4. View Project Details");
                System.out.println("5. Assign Team Lead");
                System.out.println("6. Update Project");
                System.out.println("7. Approve Project Completion");
                System.out.println("8. View Team");
                break;

            case TEAM_LEAD:
                System.out.println("2. View My Projects");
                System.out.println("4. View Project Details");
                System.out.println("7. Add Team Member");
                System.out.println("8. View Team");
                System.out.println("9. Remove Team Member");
                break;

            case TEAM_MEMBER:
                System.out.println("2. View My Projects");
                System.out.println("4. View Project Details");
                System.out.println("8. View Team");
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

        Project project = new Project(name, requirements, null, priority);
        project.setDomain(domain);
        project.setCost(cost);
        project.setStartDate(startDate);
        project.setDeadline(deadline);

        projectService.createProject(project, requestingUser);

        logger.info(
                "Project created by user {}. Project ID: {}",
                requestingUser.getUsername(),
                project.getId()
        );

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

    private void assignProjectManager(User requestingUser) throws SQLException {
        int projectId = readProjectId();

        System.out.print("Project Manager User ID: ");
        int managerId = Integer.parseInt(scanner.nextLine().trim());

        projectService.assignProjectManager(projectId, managerId, requestingUser);

        logger.info(
                "Project Manager {} assigned to project {}",
                managerId,
                projectId
        );

        System.out.println("Project Manager assigned successfully.");
    }

    private void assignTeamLead(User requestingUser) throws SQLException {
        int projectId = readProjectId();

        System.out.print("Team Lead User ID: ");
        int teamLeadId = Integer.parseInt(scanner.nextLine().trim());

        projectService.assignTeamLead(projectId, teamLeadId, requestingUser);

        logger.info(
                "Team Lead {} assigned to project {}",
                teamLeadId,
                projectId
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

        if (requestingUser.getRole() == User.Role.ADMIN) {
            System.out.print("Status [" + project.getStatus() + "]: ");
            String status = scanner.nextLine().trim();

            if (!status.isEmpty()) {
                project.setStatus(status);
            }
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

        logger.info(
                "Project updated by user {}. Project ID: {}",
                requestingUser.getUsername(),
                projectId
        );

        System.out.println("Project updated successfully.");
    }

    private void approveProjectCompletion(User requestingUser) throws SQLException {
        System.out.println();
        System.out.println("========== APPROVE PROJECT COMPLETION ==========");

        int projectId = readProjectId();

        System.out.print("Approve project completion? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!"yes".equals(confirmation)) {
            System.out.println("Project completion approval cancelled.");
            return;
        }

        projectService.approveProjectCompletion(projectId, requestingUser);

        logger.info(
                "Project completion approved by user {}. Project ID: {}",
                requestingUser.getUsername(),
                projectId
        );

        System.out.println("Project completion approved successfully.");
    }

    private void addTeamMember(User requestingUser) throws SQLException {
        System.out.println();
        System.out.println("========== ADD TEAM MEMBER ==========");

        int projectId = readProjectId();

        System.out.print("Team Member User ID: ");
        int userId = Integer.parseInt(scanner.nextLine().trim());

        projectMemberService.addMember(
                projectId,
                userId,
                "TEAM_MEMBER",
                requestingUser
        );

        logger.info(
                "Team Member {} added to project {} by {}",
                userId,
                projectId,
                requestingUser.getUsername()
        );

        System.out.println("Team Member added successfully.");
    }

    private void removeTeamMember(User requestingUser) throws SQLException {
        System.out.println();
        System.out.println("========== REMOVE TEAM MEMBER ==========");

        int projectId = readProjectId();

        System.out.print("Team Member User ID: ");
        int userId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Confirm removal? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!"yes".equals(confirmation)) {
            System.out.println("Member removal cancelled.");
            return;
        }

        projectMemberService.removeMember(projectId, userId, requestingUser);

        logger.info(
                "Team Member {} removed from project {} by {}",
                userId,
                projectId,
                requestingUser.getUsername()
        );

        System.out.println("Team Member removed successfully.");
    }

    private void viewTeam(User requestingUser) throws SQLException {
        System.out.println();
        System.out.println("========== PROJECT TEAM ==========");

        int projectId = readProjectId();

        List<ProjectMember> members = projectMemberService.getProjectTeam(
                projectId,
                requestingUser
        );

        if (members.isEmpty()) {
            System.out.println("No team members found.");
            return;
        }

        System.out.println();
        System.out.println("Project ID: " + projectId);
        System.out.println("----------------------------------------");

        for (ProjectMember member : members) {
            System.out.println("User ID       : " + member.getUserId());
            System.out.println("Project Role  : " + member.getRoleInProject());
            System.out.println("Joined At     : " + member.getJoinedAt());
            System.out.println("----------------------------------------");
        }
    }

    private void deleteProject(User requestingUser) throws SQLException {
        int projectId = readProjectId();

        System.out.print("Are you sure you want to delete this project? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (!"yes".equals(confirmation)) {
            System.out.println("Delete cancelled.");
            return;
        }

        projectService.deleteProject(projectId, requestingUser);

        logger.info(
                "Project {} deleted by {}",
                projectId,
                requestingUser.getUsername()
        );

        System.out.println("Project deleted successfully.");
    }

    private int readProjectId() {
        System.out.print("Project ID: ");
        String input = scanner.nextLine().trim();

        try {
            int projectId = Integer.parseInt(input);

            if (projectId <= 0) {
                throw new NumberFormatException();
            }

            return projectId;
        } catch (NumberFormatException e) {
            throw new ValidationException("Project ID must be a positive number.");
        }
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

    private String getUsername(User user) {
        return user != null ? user.getUsername() : "unknown";
    }
}