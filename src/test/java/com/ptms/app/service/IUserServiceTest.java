package com.ptms.app.service;

import com.ptms.app.dao.UserDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IUserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private IUserService userService;

    private User admin;
    private User teamMember;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1);
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setUsername("admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword("admin123");
        admin.setRole(User.Role.ADMIN);

        teamMember = new User();
        teamMember.setId(2);
        teamMember.setFirstName("John");
        teamMember.setLastName("Smith");
        teamMember.setUsername("john");
        teamMember.setEmail("john@gmail.com");
        teamMember.setPassword("john123");
        teamMember.setRole(User.Role.TEAM_MEMBER);
    }

    @Test
    void registerUser_shouldRegisterSuccessfully() throws SQLException {
        // Arrange
        when(userDao.findByUsername("john")).thenReturn(null);
        when(userDao.findByEmail("john@gmail.com")).thenReturn(null);
        when(userDao.insertUser(teamMember)).thenReturn(1);

        // Act
        User result = userService.registerUser(teamMember);

        // Assert
        assertNotNull(result);
        assertEquals("john", result.getUsername());
        assertEquals(User.Role.TEAM_MEMBER, result.getRole());

        verify(userDao).findByUsername("john");
        verify(userDao).findByEmail("john@gmail.com");
        verify(userDao).insertUser(teamMember);
    }

    @Test
    void registerUser_shouldRejectDuplicateUsername() throws SQLException {
        // Arrange
        when(userDao.findByUsername("john")).thenReturn(teamMember);

        // Act
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.registerUser(teamMember)
        );

        // Assert
        assertEquals("Username already exists.", exception.getMessage());

        verify(userDao).findByUsername("john");
        verify(userDao, never()).insertUser(any(User.class));
    }

    @Test
    void login_shouldReturnUserForValidCredentials() throws SQLException {
        // Arrange
        when(userDao.findByUsername("john")).thenReturn(teamMember);

        // Act
        User result = userService.login("john", "john123");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals("john", result.getUsername());

        verify(userDao).findByUsername("john");
    }

    @Test
    void login_shouldRejectInvalidPassword() throws SQLException {
        // Arrange
        when(userDao.findByUsername("john")).thenReturn(teamMember);

        // Act
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userService.login("john", "wrong")
        );

        // Assert
        assertEquals(
                "Invalid username or password.",
                exception.getMessage()
        );

        verify(userDao).findByUsername("john");
    }

    @Test
    void getUserById_shouldReturnUser() throws SQLException {
        // Arrange
        when(userDao.findByUserId(2)).thenReturn(teamMember);

        // Act
        User result = userService.getUserById(2);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals("john", result.getUsername());

        verify(userDao).findByUserId(2);
    }

    @Test
    void getUserById_shouldThrowWhenUserNotFound() throws SQLException {
        // Arrange
        when(userDao.findByUserId(99)).thenReturn(null);

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(99)
        );

        // Assert
        assertEquals(
                "User not found with id 99",
                exception.getMessage()
        );

        verify(userDao).findByUserId(99);
    }

    @Test
    void getAllUsers_shouldReturnUsersForAdmin() throws SQLException {
        // Arrange
        when(userDao.findAll()).thenReturn(List.of(admin, teamMember));

        // Act
        List<User> result = userService.getAllUsers(admin);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        verify(userDao).findAll();
    }

    @Test
    void getAllUsers_shouldRejectNonAdmin() {
        // Arrange
        User requestingUser = teamMember;

        // Act
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.getAllUsers(requestingUser)
        );

        // Assert
        assertEquals(
                "Only ADMIN can perform this operation.",
                exception.getMessage()
        );

        verifyNoInteractions(userDao);
    }

    @Test
    void updateProfile_shouldUpdateSuccessfully() throws SQLException {
        // Arrange
        when(userDao.updateProfile(teamMember)).thenReturn(1);

        // Act
        userService.updateProfile(teamMember, teamMember);

        // Assert
        verify(userDao).updateProfile(teamMember);
    }

    @Test
    void changeRole_shouldChangeRoleForAdmin() throws SQLException {
        // Arrange
        when(userDao.findByUserId(2)).thenReturn(teamMember);
        when(userDao.updateRole(2, User.Role.TEAM_LEAD)).thenReturn(1);

        // Act
        userService.changeRole(
                2,
                User.Role.TEAM_LEAD,
                admin
        );

        // Assert
        verify(userDao).findByUserId(2);
        verify(userDao).updateRole(2, User.Role.TEAM_LEAD);
    }

    @Test
    void deleteUser_shouldDeleteSuccessfully() throws SQLException {
        // Arrange
        when(userDao.findByUserId(2)).thenReturn(teamMember);
        when(userDao.deleteUser(2)).thenReturn(1);

        // Act
        userService.deleteUser(2, admin);

        // Assert
        verify(userDao).findByUserId(2);
        verify(userDao).deleteUser(2);
    }

    @Test
    void deleteUser_shouldRejectNonAdmin() {
        // Arrange
        User requestingUser = teamMember;

        // Act
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.deleteUser(2, requestingUser)
        );

        // Assert
        assertEquals(
                "Only ADMIN can perform this operation.",
                exception.getMessage()
        );

        verifyNoInteractions(userDao);
    }
}