package com.ptms.app.controller;

import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.ProjectMember;
import com.ptms.app.model.User;
import com.ptms.app.service.IProjectMemberService;
import com.ptms.app.service.ProjectMemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ProjectMemberController {

    private static final Logger logger =
            LoggerFactory.getLogger(ProjectMemberController.class);

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

    public void addTeamMember(User loggedInUser) {
        try {
            requireMemberManagementRole(loggedInUser);

            System.out.println();
            System.out.println("========== ADD TEAM MEMBER ==========");

            System.out.print("Project ID: ");
            int projectId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Team Member User ID: ");
            int userId = Integer.parseInt(scanner.nextLine().trim());

            projectMemberService.addMember(
                    projectId,
                    userId,
                    "TEAM_MEMBER",
                    loggedInUser
            );

            logger.info(
                    "Team Member added. Project ID: {}, User ID: {}, Added by: {}",
                    projectId,
                    userId,
                    loggedInUser.getUsername()
            );

            System.out.println("Team Member added successfully.");

        } catch (
                UnauthorizedException
                | ValidationException
                | ResourceNotFoundException e) {

            logger.warn(
                    "Add team member failed: {}",
                    e.getMessage()
            );

            System.out.println("Error: " + e.getMessage());

        } catch (SQLException e) {
            logger.error(
                    "Database error while adding team member.",
                    e
            );

            System.out.println(
                    "Database error: " + e.getMessage()
            );

        } catch (NumberFormatException e) {
            System.out.println("Please enter valid numeric values.");
        }
    }

    public void removeTeamMember(User loggedInUser) {
        try {
            requireMemberManagementRole(loggedInUser);

            System.out.println();
            System.out.println("========== REMOVE TEAM MEMBER ==========");

            System.out.print("Project ID: ");
            int projectId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Team Member User ID: ");
            int userId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Confirm removal? (yes/no): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (!"yes".equals(confirmation)) {
                System.out.println("Removal cancelled.");
                return;
            }

            projectMemberService.removeMember(
                    projectId,
                    userId,
                    loggedInUser
            );

            logger.info(
                    "Team Member removed. Project ID: {}, User ID: {}, Removed by: {}",
                    projectId,
                    userId,
                    loggedInUser.getUsername()
            );

            System.out.println("Team Member removed successfully.");

        } catch (
                UnauthorizedException
                | ValidationException
                | ResourceNotFoundException e) {

            logger.warn(
                    "Remove team member failed: {}",
                    e.getMessage()
            );

            System.out.println("Error: " + e.getMessage());

        } catch (SQLException e) {
            logger.error(
                    "Database error while removing team member.",
                    e
            );

            System.out.println(
                    "Database error: " + e.getMessage()
            );

        } catch (NumberFormatException e) {
            System.out.println("Please enter valid numeric values.");
        }
    }

    public void showTeam(User loggedInUser) {
        try {
            if (loggedInUser == null) {
                throw new UnauthorizedException(
                        "User must be logged in."
                );
            }

            System.out.println();
            System.out.println("========== PROJECT TEAM ==========");

            System.out.print("Project ID: ");
            int projectId = Integer.parseInt(scanner.nextLine().trim());

            List<ProjectMember> members =
                    projectMemberService.getProjectTeam(
                            projectId,
                            loggedInUser
                    );

            if (members.isEmpty()) {
                System.out.println(
                        "No members found in this project."
                );
                return;
            }

            System.out.println();
            System.out.println("Project ID: " + projectId);
            System.out.println("----------------------------------------");

            for (ProjectMember member : members) {
                System.out.println(
                        "User ID       : " + member.getUserId()
                );

                System.out.println(
                        "Project Role  : " + member.getRoleInProject()
                );

                System.out.println(
                        "Joined At     : " + member.getJoinedAt()
                );

                System.out.println("----------------------------------------");
            }

        } catch (
                UnauthorizedException
                | ValidationException
                | ResourceNotFoundException e) {

            logger.warn(
                    "Show project team failed: {}",
                    e.getMessage()
            );

            System.out.println("Error: " + e.getMessage());

        } catch (SQLException e) {
            logger.error(
                    "Database error while showing project team.",
                    e
            );

            System.out.println(
                    "Database error: " + e.getMessage()
            );

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid project ID.");
        }
    }

    private void requireMemberManagementRole(User loggedInUser) {
        if (loggedInUser == null) {
            throw new UnauthorizedException(
                    "User must be logged in."
            );
        }

        if (loggedInUser.getRole() != User.Role.ADMIN
                && loggedInUser.getRole() != User.Role.TEAM_LEAD) {

            throw new UnauthorizedException(
                    "Only ADMIN or TEAM_LEAD can manage project members."
            );
        }
    }
}