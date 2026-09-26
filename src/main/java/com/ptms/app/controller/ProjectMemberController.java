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

public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final Scanner scanner;

    public ProjectMemberController() {
        this.projectMemberService = new IProjectMemberService();
        this.scanner = new Scanner(System.in);
    }

    public ProjectMemberController(ProjectMemberService projectMemberService, Scanner scanner) {
        this.projectMemberService = projectMemberService;
        this.scanner = scanner;
    }

    public void showMenu(User loggedInUser) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Project Members ---");
            System.out.println("1. Add member to project");
            System.out.println("2. Remove member from project");
            System.out.println("3. View members of a project");
            System.out.println("4. View my project memberships");
            System.out.println("0. Back");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addMember(loggedInUser);
                    case "2" -> removeMember(loggedInUser);
                    case "3" -> viewMembersOfProject();
                    case "4" -> viewMyMemberships(loggedInUser);
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option, try again.");
                }
            } catch (UnauthorizedException | ValidationException | ResourceNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
    }

    private void addMember(User requestingUser) throws SQLException {
        System.out.print("Project id: ");
        int projectId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("User id to add: ");
        int userId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Role in project (e.g. TEAM_MEMBER): ");
        String role = scanner.nextLine().trim();

        projectMemberService.addMember(projectId, userId, role, requestingUser);
        System.out.println("Member added.");
    }

    private void removeMember(User requestingUser) throws SQLException {
        System.out.print("Project id: ");
        int projectId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("User id to remove: ");
        int userId = Integer.parseInt(scanner.nextLine().trim());

        projectMemberService.removeMember(projectId, userId, requestingUser);
        System.out.println("Member removed.");
    }

    private void viewMembersOfProject() throws SQLException {
        System.out.print("Project id: ");
        int projectId = Integer.parseInt(scanner.nextLine().trim());
        List<ProjectMember> members = projectMemberService.getMembersOfProject(projectId);
        if (members.isEmpty()) {
            System.out.println("No members found for this project.");
            return;
        }
        members.forEach(this::printMemberSummary);
    }

    private void viewMyMemberships(User requestingUser) throws SQLException {
        List<ProjectMember> memberships = projectMemberService.getProjectsForMember(requestingUser.getId());
        if (memberships.isEmpty()) {
            System.out.println("You are not a member of any project.");
            return;
        }
        memberships.forEach(this::printMemberSummary);
    }

    private void printMemberSummary(ProjectMember member) {
        System.out.printf("projectId=%d | userId=%d | role=%s | joinedAt=%s%n",
                member.getProjectId(), member.getUserId(), member.getRoleInProject(), member.getJoinedAt());
    }
}
