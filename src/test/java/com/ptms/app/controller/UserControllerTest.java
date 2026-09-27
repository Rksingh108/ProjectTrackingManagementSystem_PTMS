package com.ptms.app.controller;

import com.ptms.app.model.User;
import com.ptms.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Scanner scanner;

    private UserController controller;
    private User user;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService, scanner);

        user = new User();
        user.setId(1);
        user.setFirstName("Karan");
        user.setLastName("Singh");
        user.setUsername("karan");
        user.setEmail("karan@gmail.com");
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
        verifyNoInteractions(userService);
    }

    @Test
    void viewAllUsers() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("1")
                .thenReturn("0");

        when(userService.getAllUsers())
                .thenReturn(List.of(new User()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(userService).getAllUsers();
    }

    @Test
    void searchUsers() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("2")
                .thenReturn("karan")
                .thenReturn("0");

        when(userService.searchUsers("karan"))
                .thenReturn(List.of(new User()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(userService).searchUsers("karan");
    }

    @Test
    void viewUsersByRole() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("3")
                .thenReturn("TEAM_MEMBER")
                .thenReturn("0");

        when(userService.getUsersByRole(User.Role.TEAM_MEMBER))
                .thenReturn(List.of(new User()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(userService)
                .getUsersByRole(User.Role.TEAM_MEMBER);
    }

    @Test
    void updateProfile() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("4")
                .thenReturn("NewName")
                .thenReturn("")
                .thenReturn("")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        assertEquals("NewName", user.getFirstName());
        verify(userService).updateUser(user);
    }

    @Test
    void changeRole() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("5")
                .thenReturn("2")
                .thenReturn("TEAM_LEAD")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(userService)
                .changeRole(2, User.Role.TEAM_LEAD, user);
    }

    @Test
    void deleteUser() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("6")
                .thenReturn("2")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(userService)
                .deleteUser(2, user);
    }

    @Test
    void registerUser() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("Karan")
                .thenReturn("Singh")
                .thenReturn("karan")
                .thenReturn("karan@gmail.com")
                .thenReturn("1234")
                .thenReturn("TEAM_MEMBER");

        User savedUser = new User();
        savedUser.setUsername("karan");

        when(userService.registerUser(any(User.class)))
                .thenReturn(savedUser);

        // Act
        User result = controller.registerUser();

        // Assert
        assertEquals(savedUser, result);
        verify(userService).registerUser(any(User.class));
    }

    @Test
    void login() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("karan")
                .thenReturn("1234");

        when(userService.login("karan", "1234"))
                .thenReturn(user);

        // Act
        User result = controller.login();

        // Assert
        assertEquals(user, result);
        verify(userService).login("karan", "1234");
    }
}