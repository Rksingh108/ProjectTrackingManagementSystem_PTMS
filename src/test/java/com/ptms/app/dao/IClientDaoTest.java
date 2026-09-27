package com.ptms.app.dao;
import com.ptms.app.model.Client;
import com.ptms.app.util.DBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IClientDaoTest {

    private IClientDao clientDao;

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        clientDao = new IClientDao();

        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
    }

    @Test
    void insertClient_shouldInsertClientSuccessfully() throws Exception {

        // Arrange
        Client client = new Client();
        client.setName("John");
        client.setEmail("john@gmail.com");
        client.setPhone("9876543210");
        client.setCompanyName("ABC Technologies");

        when(connection.prepareStatement(
                anyString(),
                eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(101);

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            int result = clientDao.insertClient(client);

            // Assert
            assertEquals(1, result);
            assertEquals(101, client.getId());

            verify(preparedStatement).setString(1, "John");
            verify(preparedStatement).setString(2, "john@gmail.com");
            verify(preparedStatement).setString(3, "9876543210");
            verify(preparedStatement).setString(4, "ABC Technologies");

            verify(preparedStatement).executeUpdate();
        }
    }

    @Test
    void insertClient_shouldReturnZeroWhenInsertFails() throws Exception {

        // Arrange
        Client client = new Client();
        client.setName("John");
        client.setEmail("john@gmail.com");
        client.setPhone("9876543210");
        client.setCompanyName("ABC Technologies");

        when(connection.prepareStatement(
                anyString(),
                eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate()).thenReturn(0);

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            int result = clientDao.insertClient(client);

            // Assert
            assertEquals(0, result);

            verify(preparedStatement).executeUpdate();
            verify(preparedStatement, never()).getGeneratedKeys();
        }
    }

    @Test
    void insertClient_shouldThrowSQLException() throws Exception {

        // Arrange
        Client client = new Client();
        client.setName("John");

        when(connection.prepareStatement(
                anyString(),
                eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate())
                .thenThrow(new SQLException("Insert failed"));

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act & Assert
            assertThrows(
                    SQLException.class,
                    () -> clientDao.insertClient(client)
            );
        }
    }

    @Test
    void findByClientId_shouldReturnClientWhenFound() throws Exception {

        // Arrange
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);

        when(resultSet.getInt("id")).thenReturn(101);
        when(resultSet.getString("name")).thenReturn("John");
        when(resultSet.getString("email")).thenReturn("john@gmail.com");
        when(resultSet.getString("phone")).thenReturn("9876543210");
        when(resultSet.getString("company_name"))
                .thenReturn("ABC Technologies");

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            Client result = clientDao.findByClientId(101);

            // Assert
            assertNotNull(result);
            assertEquals(101, result.getId());
            assertEquals("John", result.getName());
            assertEquals("john@gmail.com", result.getEmail());
            assertEquals("9876543210", result.getPhone());
            assertEquals("ABC Technologies", result.getCompanyName());

            verify(preparedStatement).setInt(1, 101);
            verify(preparedStatement).executeQuery();
        }
    }

    @Test
    void findByClientId_shouldReturnNullWhenClientNotFound()
            throws Exception {

        // Arrange
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next()).thenReturn(false);

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            Client result = clientDao.findByClientId(999);

            // Assert
            assertNull(result);

            verify(preparedStatement).setInt(1, 999);
        }
    }

    @Test
    void findByClientId_shouldThrowSQLException() throws Exception {

        // Arrange
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeQuery())
                .thenThrow(new SQLException("Database error"));

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act & Assert
            assertThrows(
                    SQLException.class,
                    () -> clientDao.findByClientId(101)
            );
        }
    }

    @Test
    void findAll_shouldReturnAllClients() throws Exception {

        // Arrange
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(resultSet.getInt("id"))
                .thenReturn(101)
                .thenReturn(102);

        when(resultSet.getString("name"))
                .thenReturn("John")
                .thenReturn("David");

        when(resultSet.getString("email"))
                .thenReturn("john@gmail.com")
                .thenReturn("david@gmail.com");

        when(resultSet.getString("phone"))
                .thenReturn("9876543210")
                .thenReturn("9123456780");

        when(resultSet.getString("company_name"))
                .thenReturn("ABC Technologies")
                .thenReturn("XYZ Solutions");

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            List<Client> clients = clientDao.findAll();

            // Assert
            assertNotNull(clients);
            assertEquals(2, clients.size());

            assertEquals(101, clients.get(0).getId());
            assertEquals("John", clients.get(0).getName());

            assertEquals(102, clients.get(1).getId());
            assertEquals("David", clients.get(1).getName());

            verify(preparedStatement).executeQuery();
        }
    }

    @Test
    void findAll_shouldReturnEmptyListWhenNoClientsExist()
            throws Exception {

        // Arrange
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next()).thenReturn(false);

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            List<Client> clients = clientDao.findAll();

            // Assert
            assertNotNull(clients);
            assertTrue(clients.isEmpty());
        }
    }

    @Test
    void searchByClientName_shouldReturnMatchingClients()
            throws Exception {

        // Arrange
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(false);

        when(resultSet.getInt("id")).thenReturn(101);
        when(resultSet.getString("name")).thenReturn("John");
        when(resultSet.getString("email"))
                .thenReturn("john@gmail.com");
        when(resultSet.getString("phone"))
                .thenReturn("9876543210");
        when(resultSet.getString("company_name"))
                .thenReturn("ABC Technologies");

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            List<Client> clients =
                    clientDao.searchByClientName("John");

            // Assert
            assertNotNull(clients);
            assertEquals(1, clients.size());

            assertEquals("John", clients.get(0).getName());

            verify(preparedStatement)
                    .setString(1, "%John%");

            verify(preparedStatement)
                    .setString(2, "%John%");

            verify(preparedStatement).executeQuery();
        }
    }

    @Test
    void searchByClientName_shouldReturnEmptyListWhenNoMatch()
            throws Exception {

        // Arrange
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeQuery())
                .thenReturn(resultSet);

        when(resultSet.next()).thenReturn(false);

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            List<Client> clients =
                    clientDao.searchByClientName("Unknown");

            // Assert
            assertNotNull(clients);
            assertTrue(clients.isEmpty());

            verify(preparedStatement)
                    .setString(1, "%Unknown%");

            verify(preparedStatement)
                    .setString(2, "%Unknown%");
        }
    }

    @Test
    void updateClient_shouldUpdateClientSuccessfully()
            throws Exception {

        // Arrange
        Client client = new Client();
        client.setId(101);
        client.setName("John Updated");
        client.setEmail("john.updated@gmail.com");
        client.setPhone("9999999999");
        client.setCompanyName("Updated Technologies");

        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            int result = clientDao.updateClient(client);

            // Assert
            assertEquals(1, result);

            verify(preparedStatement)
                    .setString(1, "John Updated");

            verify(preparedStatement)
                    .setString(2, "john.updated@gmail.com");

            verify(preparedStatement)
                    .setString(3, "9999999999");

            verify(preparedStatement)
                    .setString(4, "Updated Technologies");

            verify(preparedStatement)
                    .setInt(5, 101);

            verify(preparedStatement).executeUpdate();
        }
    }

    @Test
    void updateClient_shouldReturnZeroWhenClientNotFound()
            throws Exception {

        // Arrange
        Client client = new Client();
        client.setId(999);
        client.setName("Unknown");

        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate()).thenReturn(0);

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            int result = clientDao.updateClient(client);

            // Assert
            assertEquals(0, result);
        }
    }

    @Test
    void updateClient_shouldThrowSQLException() throws Exception {

        // Arrange
        Client client = new Client();
        client.setId(101);
        client.setName("John");

        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate())
                .thenThrow(new SQLException("Update failed"));

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act & Assert
            assertThrows(
                    SQLException.class,
                    () -> clientDao.updateClient(client)
            );
        }
    }

    @Test
    void deleteClient_shouldDeleteClientSuccessfully()
            throws Exception {

        // Arrange
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            int result = clientDao.deleteClient(101);

            // Assert
            assertEquals(1, result);

            verify(preparedStatement).setInt(1, 101);
            verify(preparedStatement).executeUpdate();
        }
    }

    @Test
    void deleteClient_shouldReturnZeroWhenClientNotFound()
            throws Exception {

        // Arrange
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate()).thenReturn(0);

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act
            int result = clientDao.deleteClient(999);

            // Assert
            assertEquals(0, result);

            verify(preparedStatement).setInt(1, 999);
        }
    }

    @Test
    void deleteClient_shouldThrowSQLException() throws Exception {

        // Arrange
        when(connection.prepareStatement(anyString()))
                .thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate())
                .thenThrow(new SQLException("Delete failed"));

        try (MockedStatic<DBConnection> mockedDB =
                     mockStatic(DBConnection.class)) {

            mockedDB.when(DBConnection::getConnection)
                    .thenReturn(connection);

            // Act & Assert
            assertThrows(
                    SQLException.class,
                    () -> clientDao.deleteClient(101)
            );
        }
    }
}