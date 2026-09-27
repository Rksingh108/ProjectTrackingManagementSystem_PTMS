package com.ptms.app.controller;

import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.ProjectMember;
import com.ptms.app.model.User;
import com.ptms.app.service.IProjectMemberService;
import com.ptms.app.service.ProjectMemberService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class ProjectMemberController {

    private static final Logger logger =
            Logger.getLogger(ProjectMemberController.class.getName());

    private final ProjectMemberService projectMemberService;
    private final Scanner scanner;

    public ProjectMemberController() {
        this.projectMemberService = new IProjectMemberService();
        this.scanner = new Scanner(System.in);
    }

    public ProjectMemberController(
            ProjectMemberService projectMemberService,
            Scanner scanner) {

        this.projectMemberService = projectMemberService;
        this.scanner = scanner;
    }

    public void showMenu(User loggedInUser) {
        boolean running = true;

        while (running) {
            logger.info("""
                    
                    --- Project Members ---
                    1. Add member to project
                    2. Remove member from project
                    3. View members of a project
                    4. View my project memberships
                    0. Back
                    Choose an option:
                    """);

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addMember(loggedInUser);
                    case "2" -> removeMember(loggedInUser);
                    case "3" -> viewMembersOfProject();
                    case "4" -> viewMyMemberships(loggedInUser);
                    case "0" -> {
                        running = false;
                        logger.info("Returning to previous menu.");
                    }
                    default -> logger.warning(
                            "Invalid option, try again."
                    );
                }
            } catch (UnauthorizedException |
                     ValidationException |
                     ResourceNotFoundException e) {

                logger.warning(
                        "Error: " + e.getMessage()
                );

            } catch (SQLException e) {

                logger.severe(
                        "Database error: " + e.getMessage()
                );
            }
        }
    }

    private void addMember(User requestingUser)
            throws SQLException {

        logger.info("Project id:");
        int projectId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        logger.info("User id to add:");
        int userId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        logger.info(
                "Role in project (e.g. TEAM_MEMBER):"
        );
        String role = scanner.nextLine().trim();

        projectMemberService.addMember(
                projectId,
                userId,
                role,
                requestingUser
        );

        logger.info("Member added successfully.");
    }

    private void removeMember(User requestingUser)
            throws SQLException {

        logger.info("Project id:");
        int projectId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        logger.info("User id to remove:");
        int userId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        projectMemberService.removeMember(
                projectId,
                userId,
                requestingUser
        );

        logger.info("Member removed successfully.");
    }

    private void viewMembersOfProject()
            throws SQLException {

        logger.info("Project id:");

        int projectId = Integer.parseInt(
                scanner.nextLine().trim()
        );

        List<ProjectMember> members =
                projectMemberService.getMembersOfProject(
                        projectId
                );

        if (members.isEmpty()) {
            logger.info(
                    "No members found for this project."
            );
            return;
        }

        members.forEach(this::printMemberSummary);
    }

    private void viewMyMemberships(User requestingUser)
            throws SQLException {

        List<ProjectMember> memberships =
                projectMemberService.getProjectsForMember(
                        requestingUser.getId()
                );

        if (memberships.isEmpty()) {
            logger.info(
                    "You are not a member of any project."
            );
            return;
        }

        memberships.forEach(this::printMemberSummary);
    }

    private void printMemberSummary(ProjectMember member) {

        logger.info(
                String.format(
                        "projectId=%d | userId=%d | role=%s | joinedAt=%s",
                        member.getProjectId(),
                        member.getUserId(),
                        member.getRoleInProject(),
                        member.getJoinedAt()
                )
        );
    }
}