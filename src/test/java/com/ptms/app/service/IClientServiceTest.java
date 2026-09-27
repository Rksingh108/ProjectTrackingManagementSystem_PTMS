package com.ptms.app.service;

import com.ptms.app.dao.ClientDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IClientServiceTest {

    @Mock
    private ClientDao clientDao;

    private IClientService clientService;

    private User admin;
    private User projectManager;
    private User employee;
    private Client client;

    @BeforeEach
    void setUp() {
        clientService = new IClientService(clientDao);

        admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        projectManager = new User();
        projectManager.setId(2);
        projectManager.setRole(User.Role.PROJECT_MANAGER);

        employee = new User();
        employee.setId(3);
        employee.setRole(User.Role.TEAM_MEMBER);

        client = new Client();
        client.setId(1);
        client.setName("ABC Technologies");
    }

    @Test
    void addClient_ShouldAddClientForAdmin() throws SQLException {

        // Arrange
        when(clientDao.insertClient(client)).thenReturn(1);

        // Act
        Client result = clientService.addClient(client, admin);

        // Assert
        assertNotNull(result);
        assertEquals(client, result);
        verify(clientDao).insertClient(client);
    }

    @Test
    void addClient_ShouldAllowProjectManager() throws SQLException {

        // Arrange
        when(clientDao.insertClient(client)).thenReturn(1);

        // Act
        Client result = clientService.addClient(client, projectManager);

        // Assert
        assertNotNull(result);
        assertEquals(client, result);
        verify(clientDao).insertClient(client);
    }

    @Test
    void addClient_ShouldThrowUnauthorizedExceptionForEmployee()
            throws SQLException {

        // Arrange

        // Act
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> clientService.addClient(client, employee)
        );

        // Assert
        assertEquals(
                "Only an Admin or Project Manager can add a client.",
                exception.getMessage()
        );

        verify(clientDao, never()).insertClient(any(Client.class));
    }

    @Test
    void getClientById_ShouldReturnClient_WhenClientExists()
            throws SQLException {

        // Arrange
        when(clientDao.findByClientId(1)).thenReturn(client);

        // Act
        Client result = clientService.getClientById(1);

        // Assert
        assertNotNull(result);
        assertEquals(client, result);
        verify(clientDao).findByClientId(1);
    }

    @Test
    void getClientById_ShouldThrowResourceNotFoundException_WhenClientDoesNotExist()
            throws SQLException {

        // Arrange
        when(clientDao.findByClientId(1)).thenReturn(null);

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clientService.getClientById(1)
        );

        // Assert
        assertEquals(
                "No client found with id 1",
                exception.getMessage()
        );

        verify(clientDao).findByClientId(1);
    }

    @Test
    void getAllClients_ShouldReturnAllClients()
            throws SQLException {

        // Arrange
        List<Client> clients = List.of(client);

        when(clientDao.findAll()).thenReturn(clients);

        // Act
        List<Client> result = clientService.getAllClients();

        // Assert
        assertNotNull(result);
        assertEquals(clients, result);
        verify(clientDao).findAll();
    }

    @Test
    void searchClients_ShouldReturnMatchingClients()
            throws SQLException {

        // Arrange
        String keyword = "ABC";
        List<Client> clients = List.of(client);

        when(clientDao.searchByClientName(keyword)).thenReturn(clients);

        // Act
        List<Client> result = clientService.searchClients(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(client, result.get(0));

        verify(clientDao).searchByClientName(keyword);
    }

    @Test
    void updateClient_ShouldUpdateClientForAdmin()
            throws SQLException {

        // Arrange
        when(clientDao.updateClient(client)).thenReturn(1);

        // Act
        clientService.updateClient(client, admin);

        // Assert
        verify(clientDao).updateClient(client);
    }

    @Test
    void updateClient_ShouldAllowProjectManager()
            throws SQLException {

        // Arrange
        when(clientDao.updateClient(client)).thenReturn(1);

        // Act
        clientService.updateClient(client, projectManager);

        // Assert
        verify(clientDao).updateClient(client);
    }

    @Test
    void updateClient_ShouldThrowResourceNotFoundException_WhenClientDoesNotExist()
            throws SQLException {

        // Arrange
        when(clientDao.updateClient(client)).thenReturn(0);

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clientService.updateClient(client, admin)
        );

        // Assert
        assertEquals(
                "No client found with id 1 to update.",
                exception.getMessage()
        );

        verify(clientDao).updateClient(client);
    }

    @Test
    void updateClient_ShouldThrowUnauthorizedExceptionForEmployee()
            throws SQLException {

        // Arrange

        // Act
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> clientService.updateClient(client, employee)
        );

        // Assert
        assertEquals(
                "Only an Admin or Project Manager can update a client.",
                exception.getMessage()
        );

        verify(clientDao, never()).updateClient(any(Client.class));
    }

    @Test
    void deleteClient_ShouldDeleteClientForAdmin()
            throws SQLException {

        // Arrange
        when(clientDao.deleteClient(1)).thenReturn(1);

        // Act
        clientService.deleteClient(1, admin);

        // Assert
        verify(clientDao).deleteClient(1);
    }

    @Test
    void deleteClient_ShouldAllowProjectManager()
            throws SQLException {

        // Arrange
        when(clientDao.deleteClient(1)).thenReturn(1);

        // Act
        clientService.deleteClient(1, projectManager);

        // Assert
        verify(clientDao).deleteClient(1);
    }

    @Test
    void deleteClient_ShouldThrowResourceNotFoundException_WhenClientDoesNotExist()
            throws SQLException {

        // Arrange
        when(clientDao.deleteClient(1)).thenReturn(0);

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clientService.deleteClient(1, admin)
        );

        // Assert
        assertEquals(
                "No client found with id 1 to delete.",
                exception.getMessage()
        );

        verify(clientDao).deleteClient(1);
    }

    @Test
    void deleteClient_ShouldThrowUnauthorizedExceptionForEmployee()
            throws SQLException {

        // Arrange

        // Act
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> clientService.deleteClient(1, employee)
        );

        // Assert
        assertEquals(
                "Only an Admin or Project Manager can delete a client.",
                exception.getMessage()
        );

        verify(clientDao, never()).deleteClient(anyInt());
    }
}
