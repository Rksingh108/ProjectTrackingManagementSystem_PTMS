package com.ptms.app.controller;

import com.ptms.app.model.Ticket;
import com.ptms.app.model.User;
import com.ptms.app.service.TicketService;
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
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private Scanner scanner;

    private TicketController controller;
    private User user;

    @BeforeEach
    void setUp() {
        controller = new TicketController(
                ticketService,
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
        verifyNoInteractions(ticketService);
    }

    @Test
    void createTicket() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("1")
                .thenReturn("10")
                .thenReturn("Fix login issue")
                .thenReturn("Login is not working")
                .thenReturn("HIGH")
                .thenReturn("2026-10-10")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(ticketService)
                .createTicket(any(Ticket.class), eq(user));
    }

    @Test
    void viewTicketsForProject() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("2")
                .thenReturn("10")
                .thenReturn("0");

        when(ticketService.getTicketsForProject(10))
                .thenReturn(List.of(new Ticket()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(ticketService)
                .getTicketsForProject(10);
    }

    @Test
    void viewMyTickets() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("3")
                .thenReturn("0");

        when(ticketService.getTicketsForUser(1))
                .thenReturn(List.of(new Ticket()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(ticketService)
                .getTicketsForUser(1);
    }

    @Test
    void assignTicket() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("4")
                .thenReturn("100")
                .thenReturn("2")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(ticketService)
                .assignTicket(100, 2, user);
    }

    @Test
    void advanceStatus() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("5")
                .thenReturn("100")
                .thenReturn("IN_PROGRESS")
                .thenReturn("50")
                .thenReturn("Development completed")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(ticketService).advanceStatus(
                100,
                "IN_PROGRESS",
                50,
                "Development completed",
                user
        );
    }

    @Test
    void deleteTicket() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("6")
                .thenReturn("100")
                .thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(ticketService)
                .deleteTicket(100, user);
    }
}