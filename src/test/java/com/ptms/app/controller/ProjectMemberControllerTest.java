package com.ptms.app.controller;

import com.ptms.app.model.ProjectMember;
import com.ptms.app.model.User;
import com.ptms.app.service.ProjectMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberControllerTest {

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private Scanner scanner;

    private ProjectMemberController controller;
    private User user;

    @BeforeEach
    void setUp() {
        controller = new ProjectMemberController(
                projectMemberService,
                scanner
        );

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
        verifyNoInteractions(projectMemberService);
    }

    @Test
    void addMember() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("1")
                .thenReturn("10")
                .thenReturn("2")
                .thenReturn("TEAM_MEMBER")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(projectMemberService)
                .addMember(10, 2, "TEAM_MEMBER", user);
    }

    @Test
    void removeMember() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("2")
                .thenReturn("10")
                .thenReturn("2")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(projectMemberService)
                .removeMember(10, 2, user);
    }

    @Test
    void viewMembersOfProject() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("3")
                .thenReturn("10")
                .thenReturn("0");

        when(projectMemberService.getMembersOfProject(10))
                .thenReturn(List.of(new ProjectMember()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(projectMemberService)
                .getMembersOfProject(10);
    }

    @Test
    void viewMyMemberships() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("4")
                .thenReturn("0");

        when(projectMemberService.getProjectsForMember(1))
                .thenReturn(List.of(new ProjectMember()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(projectMemberService)
                .getProjectsForMember(1);
    }
}