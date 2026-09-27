package com.ptms.app.service;

import com.ptms.app.dao.UserDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private IUserService service;

    @BeforeEach
    void setUp() {
        service = new IUserService(userDao);
    }

    @Test
    void registerUser() throws SQLException {

        // Arrange
        User user = new User();
        user.setUsername("karan");

        when(userDao.findByUsername("karan"))
                .thenReturn(null);
        when(userDao.insertUser(user))
                .thenReturn(1);

        // Act
        User result = service.registerUser(user);

        // Assert
        assertEquals(user, result);
        verify(userDao).insertUser(user);
    }

    @Test
    void registerUserWhenUsernameExists() throws SQLException {

        // Arrange
        User user = new User();
        user.setUsername("karan");

        when(userDao.findByUsername("karan"))
                .thenReturn(user);

        // Act & Assert
        assertThrows(
                ValidationException.class,
                () -> service.registerUser(user)
        );

        verify(userDao, never()).insertUser(any());
    }

    @Test
    void login() throws SQLException {

        // Arrange
        User user = new User();
        user.setUsername("karan");
        user.setPassword("1234");

        when(userDao.findByUsername("karan"))
                .thenReturn(user);

        // Act
        User result = service.login("karan", "1234");

        // Assert
        assertEquals(user, result);
        verify(userDao).findByUsername("karan");
    }

    @Test
    void loginWithInvalidPassword() throws SQLException {

        // Arrange
        User user = new User();
        user.setUsername("karan");
        user.setPassword("1234");

        when(userDao.findByUsername("karan"))
                .thenReturn(user);

        // Act & Assert
        assertThrows(
                ValidationException.class,
                () -> service.login("karan", "wrong")
        );
    }

    @Test
    void getUserById() throws SQLException {

        // Arrange
        User user = new User();
        user.setId(1);

        when(userDao.findByUserId(1))
                .thenReturn(user);

        // Act
        User result = service.getUserById(1);

        // Assert
        assertEquals(1, result.getId());
        verify(userDao).findByUserId(1);
    }

    @Test
    void getAllUsers() throws SQLException {

        // Arrange
        List<User> users = List.of(
                new User(),
                new User()
        );

        when(userDao.findAll())
                .thenReturn(users);

        // Act
        List<User> result = service.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        verify(userDao).findAll();
    }

    @Test
    void searchUsers() throws SQLException {

        // Arrange
        List<User> users = List.of(new User());

        when(userDao.searchByUserName("karan"))
                .thenReturn(users);

        // Act
        List<User> result = service.searchUsers("karan");

        // Assert
        assertEquals(1, result.size());
        verify(userDao).searchByUserName("karan");
    }

    @Test
    void getUsersByRole() throws SQLException {

        // Arrange
        List<User> users = List.of(new User());

        when(userDao.findByUserRole(User.Role.ADMIN))
                .thenReturn(users);

        // Act
        List<User> result =
                service.getUsersByRole(User.Role.ADMIN);

        // Assert
        assertEquals(1, result.size());
        verify(userDao).findByUserRole(User.Role.ADMIN);
    }

    @Test
    void updateUser() throws SQLException {

        // Arrange
        User user = new User();
        user.setId(1);

        when(userDao.updateUser(user))
                .thenReturn(1);

        // Act
        service.updateUser(user);

        // Assert
        verify(userDao).updateUser(user);
    }

    @Test
    void changeRole() throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        User target = new User();
        target.setId(2);
        target.setRole(User.Role.TEAM_MEMBER);

        when(userDao.findByUserId(2))
                .thenReturn(target);

        // Act
        service.changeRole(
                2,
                User.Role.TEAM_LEAD,
                admin
        );

        // Assert
        assertEquals(User.Role.TEAM_LEAD, target.getRole());
        verify(userDao).updateUser(target);
    }

    @Test
    void changeRoleUnauthorized() throws SQLException {

        // Arrange
        User employee = new User();
        employee.setRole(User.Role.TEAM_MEMBER);

        // Act & Assert
        assertThrows(
                UnauthorizedException.class,
                () -> service.changeRole(
                        2,
                        User.Role.ADMIN,
                        employee
                )
        );

        verifyNoInteractions(userDao);
    }

    @Test
    void deleteUser() throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        when(userDao.deleteUser(2))
                .thenReturn(1);

        // Act
        service.deleteUser(2, admin);

        // Assert
        verify(userDao).deleteUser(2);
    }

    @Test
    void deleteUserUnauthorized() throws SQLException {

        // Arrange
        User employee = new User();
        employee.setRole(User.Role.TEAM_MEMBER);

        // Act & Assert
        assertThrows(
                UnauthorizedException.class,
                () -> service.deleteUser(2, employee)
        );

        verifyNoInteractions(userDao);
    }
}