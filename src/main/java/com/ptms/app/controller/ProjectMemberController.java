package com.ptms.app.controller;

import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.model.Project;
import com.ptms.app.model.ProjectMember;
import com.ptms.app.model.User;
import com.ptms.app.service.IProjectMemberService;
import com.ptms.app.service.ProjectMemberService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ProjectMemberController {

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
            try {
                showDashboard(loggedInUser);

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        viewMyProjects(loggedInUser);
                        break;
                    case "2":
                        viewProjectDetails(loggedInUser);
                        break;
                    case "3":
                        viewMyTasksMessage();
                        break;
                    case "4":
                        viewProjectTeam(loggedInUser);
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (UnauthorizedException | ResourceNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid project ID.");
            }
        }
    }

    private void showDashboard(User loggedInUser) throws SQLException {
        List<ProjectMember> projects =
                projectMemberService.getMyProjects(loggedInUser);

        System.out.println();
        System.out.println("================================================");
        System.out.println("           PROJECT MEMBER DASHBOARD");
        System.out.println("================================================");
        System.out.println("Logged User : " + loggedInUser.getUsername());
        System.out.println("Role        : " + loggedInUser.getRole());
        System.out.println("My Projects : " + projects.size());
        System.out.println("------------------------------------------------");
        System.out.println("1. View My Projects");
        System.out.println("2. View Project Details");
        System.out.println("3. View My Tasks");
        System.out.println("4. View Project Team");
        System.out.println("0. Back");
        System.out.println("================================================");
        System.out.print("Choose an option: ");
    }

    private void viewMyProjects(User loggedInUser) throws SQLException {
        List<ProjectMember> projects =
                projectMemberService.getMyProjects(loggedInUser);

        System.out.println();
        System.out.println("========== MY PROJECTS ==========");

        if (projects.isEmpty()) {
            System.out.println("You are not assigned to any project.");
            return;
        }

        for (ProjectMember member : projects) {
            System.out.println("----------------------------------------");
            System.out.println("Project ID : " + member.getProjectId());
            System.out.println("My Role    : " + member.getRoleInProject());
            System.out.println("Joined At  : " + member.getJoinedAt());
        }

        System.out.println("----------------------------------------");
    }

    private void viewProjectDetails(User loggedInUser) throws SQLException {
        System.out.print("Enter project ID: ");

        int projectId = Integer.parseInt(scanner.nextLine().trim());

        Project project =
                projectMemberService.getProjectDetails(
                        projectId,
                        loggedInUser
                );

        ProjectMember membership =
                projectMemberService.getMyMembership(
                        projectId,
                        loggedInUser
                );

        System.out.println();
        System.out.println("========== PROJECT DETAILS ==========");
        System.out.println("Project ID   : " + membership.getProjectId());
        System.out.println("My Role      : " + membership.getRoleInProject());
        System.out.println("Joined At    : " + membership.getJoinedAt());
        System.out.println("Manager ID   : " + project.getManagerId());
        System.out.println("Team Lead ID : " + project.getTeamLeadId());
        System.out.println("=====================================");
    }

    private void viewProjectTeam(User loggedInUser) throws SQLException {
        System.out.print("Enter project ID: ");

        int projectId = Integer.parseInt(scanner.nextLine().trim());

        List<ProjectMember> members =
                projectMemberService.getProjectTeam(
                        projectId,
                        loggedInUser
                );

        System.out.println();
        System.out.println("========== PROJECT TEAM ==========");

        if (members.isEmpty()) {
            System.out.println("No team members found.");
            return;
        }

        for (ProjectMember member : members) {
            System.out.println(
                    "User ID: " + member.getUserId()
                            + " | Role: " + member.getRoleInProject()
                            + " | Joined: " + member.getJoinedAt()
            );
        }

        System.out.println("==================================");
    }

    private void viewMyTasksMessage() {
        System.out.println();
        System.out.println("========== MY TASKS ==========");
        System.out.println("Task management will be handled by the Task module.");
    }
}