package com.ptms.app.controller;

import com.ptms.app.model.Project;
import com.ptms.app.model.User;
import com.ptms.app.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private Scanner scanner;

    private ProjectController controller;
    private User user;

    @BeforeEach
    void setUp() {
        controller = new ProjectController(projectService, scanner);

        user = new User();
        user.setId(1);
        user.setRole(User.Role.ADMIN);
    }

    @Test
    void showMenuExit() {

        // Arrange
        when(scanner.nextLine()).thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(scanner).nextLine();
        verifyNoInteractions(projectService);
    }

    @Test
    void createProject() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("1")
                .thenReturn("PTMS")
                .thenReturn("Project tracking system")
                .thenReturn("IT")
                .thenReturn("50000")
                .thenReturn("2026-09-27")
                .thenReturn("2026-12-31")
                .thenReturn("HIGH")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(projectService)
                .createProject(any(Project.class), eq(user));
    }

    @Test
    void viewMyProjects() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("2")
                .thenReturn("0");

        when(projectService.getProjectsForUser(user))
                .thenReturn(List.of(new Project()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(projectService).getProjectsForUser(user);
    }

    @Test
    void viewAllProjects() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("3")
                .thenReturn("0");

        when(projectService.getAllProjects())
                .thenReturn(List.of(new Project()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(projectService).getAllProjects();
    }

    @Test
    void viewProjectDetails() throws SQLException {

        // Arrange
        Project project = new Project();
        project.setId(1);
        project.setName("PTMS");

        when(scanner.nextLine())
                .thenReturn("4")
                .thenReturn("1")
                .thenReturn("0");

        when(projectService.getProjectById(1))
                .thenReturn(project);

        // Act
        controller.showMenu(user);

        // Assert
        verify(projectService).getProjectById(1);
    }

    @Test
    void assignTeamLead() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("5")
                .thenReturn("1")
                .thenReturn("2")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(projectService)
                .assignTeamLead(1, 2, user);
    }

    @Test
    void updateProject() throws SQLException {

        // Arrange
        Project project = new Project();
        project.setId(1);
        project.setName("Old Project");
        project.setStatus("IN_PROGRESS");
        project.setPriority("MEDIUM");

        when(scanner.nextLine())
                .thenReturn("6")
                .thenReturn("1")
                .thenReturn("New Project")
                .thenReturn("COMPLETED")
                .thenReturn("HIGH")
                .thenReturn("0");

        when(projectService.getProjectById(1))
                .thenReturn(project);

        // Act
        controller.showMenu(user);

        // Assert
        verify(projectService).getProjectById(1);
        verify(projectService).updateProject(project, user);
    }

    @Test
    void deleteProject() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("7")
                .thenReturn("1")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(projectService).deleteProject(1, user);
    }
}