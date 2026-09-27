package com.ptms.app.controller;

import com.ptms.app.model.Client;
import com.ptms.app.model.User;
import com.ptms.app.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @Mock
    private Scanner scanner;

    private ClientController controller;
    private User user;

    @BeforeEach
    void setUp() {
        controller = new ClientController(clientService, scanner);

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
        verifyNoInteractions(clientService);
    }

    @Test
    void addClient() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("1")
                .thenReturn("ABC Company")
                .thenReturn("abc@gmail.com")
                .thenReturn("9876543210")
                .thenReturn("ABC Pvt Ltd")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(clientService).addClient(any(Client.class), eq(user));
    }

    @Test
    void viewAllClients() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("2")
                .thenReturn("0");

        when(clientService.getAllClients())
                .thenReturn(List.of(new Client()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(clientService).getAllClients();
    }

    @Test
    void searchClients() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("3")
                .thenReturn("ABC")
                .thenReturn("0");

        when(clientService.searchClients("ABC"))
                .thenReturn(List.of(new Client()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(clientService).searchClients("ABC");
    }

    @Test
    void updateClient() throws SQLException {

        // Arrange
        Client client = new Client();
        client.setId(1);
        client.setName("Old Name");
        client.setEmail("old@gmail.com");
        client.setPhone("9999999999");
        client.setCompanyName("Old Company");

        when(scanner.nextLine())
                .thenReturn("4")
                .thenReturn("1")
                .thenReturn("New Name")
                .thenReturn("")
                .thenReturn("")
                .thenReturn("")
                .thenReturn("0");

        when(clientService.getClientById(1))
                .thenReturn(client);

        // Act
        controller.showMenu(user);

        // Assert
        verify(clientService).getClientById(1);
        verify(clientService).updateClient(client, user);
        assertEquals("New Name", client.getName());
    }

    @Test
    void deleteClient() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("5")
                .thenReturn("1")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(clientService).deleteClient(1, user);
    }
}