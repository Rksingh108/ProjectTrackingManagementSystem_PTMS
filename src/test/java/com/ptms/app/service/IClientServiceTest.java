package com.ptms.app.service;

import com.ptms.app.dao.ClientDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Client;
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
class IClientServiceTest {

    @Mock
    ClientDao clientDao;

    IClientService service;
    User admin;
    User member;
    Client client;

    @BeforeEach
    void setUp() {
        service = new IClientService(clientDao);

        admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        member = new User();
        member.setId(2);
        member.setRole(User.Role.TEAM_MEMBER);

        client = new Client();
        client.setId(10);
        client.setName("John Doe");
        client.setEmail("john@gmail.com");
        client.setPhone("9876543210");
        client.setCompanyName("ABC Company");
    }

    @Test
    void addClient_success() throws SQLException {
        // Arrange
        when(clientDao.insertClient(client)).thenReturn(1);

        // Act
        Client result = service.addClient(client, admin);

        // Assert
        assertEquals(client, result);
        verify(clientDao).insertClient(client);
    }

    @Test
    void addClient_unauthorized() {
        // Arrange
        User user = member;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.addClient(client, user)
        );

        // Assert
        assertEquals("Only ADMIN can add a client.", ex.getMessage());
        verifyNoInteractions(clientDao);
    }

    @Test
    void addClient_invalidClient() {
        // Arrange
        client.setEmail("invalid");

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.addClient(client, admin)
        );

        // Assert
        assertEquals(
                "Please enter a valid email address.",
                ex.getMessage()
        );
        verifyNoInteractions(clientDao);
    }

    @Test
    void getClientById_success() throws SQLException {
        // Arrange
        when(clientDao.findByClientId(10)).thenReturn(client);

        // Act
        Client result = service.getClientById(10, admin);

        // Assert
        assertEquals(client, result);
        verify(clientDao).findByClientId(10);
    }

    @Test
    void getClientById_notFound() throws SQLException {
        // Arrange
        when(clientDao.findByClientId(10)).thenReturn(null);

        // Act
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getClientById(10, admin)
        );

        // Assert
        assertEquals("No client found with id 10", ex.getMessage());
    }

    @Test
    void getAllClients_success() throws SQLException {
        // Arrange
        when(clientDao.findAll()).thenReturn(List.of(client));

        // Act
        List<Client> result = service.getAllClients(admin);

        // Assert
        assertEquals(1, result.size());
        assertEquals(client, result.get(0));
        verify(clientDao).findAll();
    }

    @Test
    void searchClients_success() throws SQLException {
        // Arrange
        when(clientDao.searchClients("John")).thenReturn(List.of(client));

        // Act
        List<Client> result =
                service.searchClients(" John ", admin);

        // Assert
        assertEquals(1, result.size());
        verify(clientDao).searchClients("John");
    }

    @Test
    void searchClients_emptyKeyword() {
        // Arrange
        String keyword = " ";

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.searchClients(keyword, admin)
        );

        // Assert
        assertEquals(
                "Search keyword cannot be empty.",
                ex.getMessage()
        );
    }

    @Test
    void updateClient_success() throws SQLException {
        // Arrange
        when(clientDao.findByClientId(10)).thenReturn(client);
        when(clientDao.updateClient(client)).thenReturn(1);

        // Act
        service.updateClient(client, admin);

        // Assert
        verify(clientDao).findByClientId(10);
        verify(clientDao).updateClient(client);
    }

    @Test
    void updateClient_notFound() throws SQLException {
        // Arrange
        when(clientDao.findByClientId(10)).thenReturn(null);

        // Act
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateClient(client, admin)
        );

        // Assert
        verify(clientDao).findByClientId(10);
        verify(clientDao, never()).updateClient(any());
    }

    @Test
    void deleteClient_success() throws SQLException {
        // Arrange
        when(clientDao.findByClientId(10)).thenReturn(client);
        when(clientDao.deleteClient(10)).thenReturn(1);

        // Act
        service.deleteClient(10, admin);

        // Assert
        verify(clientDao).findByClientId(10);
        verify(clientDao).deleteClient(10);
    }

    @Test
    void deleteClient_notFound() throws SQLException {
        // Arrange
        when(clientDao.findByClientId(10)).thenReturn(null);

        // Act
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.deleteClient(10, admin)
        );

        // Assert
        verify(clientDao).findByClientId(10);
    }

    @Test
    void getTotalClients_success() throws SQLException {
        // Arrange
        when(clientDao.countClients()).thenReturn(5);

        // Act
        int result = service.getTotalClients(admin);

        // Assert
        assertEquals(5, result);
        verify(clientDao).countClients();
    }

    @Test
    void getTotalCompanies_success() throws SQLException {
        // Arrange
        when(clientDao.countCompanies()).thenReturn(3);

        // Act
        int result = service.getTotalCompanies(admin);

        // Assert
        assertEquals(3, result);
        verify(clientDao).countCompanies();
    }

    @Test
    void getTotalClients_unauthorized() {
        // Arrange
        User user = member;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.getTotalClients(user)
        );

        // Assert
        assertEquals(
                "Only ADMIN can view client statistics.",
                ex.getMessage()
        );
    }

    @Test
    void daoException_isPropagated() throws SQLException {
        // Arrange
        when(clientDao.countClients())
                .thenThrow(new SQLException("Database error"));

        // Act
        SQLException ex = assertThrows(
                SQLException.class,
                () -> service.getTotalClients(admin)
        );

        // Assert
        assertEquals("Database error", ex.getMessage());
    }
}
