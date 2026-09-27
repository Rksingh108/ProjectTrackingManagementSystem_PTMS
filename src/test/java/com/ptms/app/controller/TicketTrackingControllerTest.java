package com.ptms.app.controller;

import com.ptms.app.model.TicketTracking;
import com.ptms.app.model.User;
import com.ptms.app.service.TicketTrackingService;
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
class TicketTrackingControllerTest {

    @Mock
    private TicketTrackingService ticketTrackingService;

    @Mock
    private Scanner scanner;

    private TicketTrackingController controller;
    private User user;

    @BeforeEach
    void setUp() {
        controller = new TicketTrackingController(
                ticketTrackingService,
                scanner
        );

        user = new User();
        user.setId(1);
        user.setRole(User.Role.TEAM_MEMBER);
    }

    @Test
    void showMenuExit() {

        // Arrange
        when(scanner.nextLine()).thenReturn("0");

        // Act
        controller.showMenu(user);

        // Assert
        verify(scanner).nextLine();
        verifyNoInteractions(ticketTrackingService);
    }

    @Test
    void viewTrackingForTicket() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("1")
                .thenReturn("100")
                .thenReturn("0");

        when(ticketTrackingService.getTrackingForTicket(100))
                .thenReturn(new TicketTracking());

        // Act
        controller.showMenu(user);

        // Assert
        verify(ticketTrackingService)
                .getTrackingForTicket(100);
    }

    @Test
    void viewMyUpdates() throws SQLException {

        // Arrange
        when(scanner.nextLine())
                .thenReturn("2")
                .thenReturn("0");

        when(ticketTrackingService.getUpdatesByUser(1))
                .thenReturn(List.of(new TicketTracking()));

        // Act
        controller.showMenu(user);

        // Assert
        verify(ticketTrackingService)
                .getUpdatesByUser(1);
    }
}