package com.ptms.app.service;

import com.ptms.app.dao.TicketTrackingDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.TicketTracking;
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
class ITicketTrackingServiceTest {

    @Mock
    private TicketTrackingDao ticketTrackingDao;

    private ITicketTrackingService service;

    private User admin;
    private User user;
    private User otherUser;

    private TicketTracking tracking;

    @BeforeEach
    void setUp() {

        // Arrange
        service = new ITicketTrackingService(ticketTrackingDao);

        admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        user = new User();
        user.setId(10);
        user.setRole(User.Role.TEAM_MEMBER);

        otherUser = new User();
        otherUser.setId(20);
        otherUser.setRole(User.Role.TEAM_MEMBER);

        tracking = new TicketTracking(
                100,
                "IN_PROGRESS",
                50,
                10
        );

        tracking.setComment("Work in progress");
    }

    @Test
    void getTrackingForTicket_success() throws SQLException {

        // Arrange
        when(ticketTrackingDao.findByTicketId(100))
                .thenReturn(tracking);

        // Act
        TicketTracking result =
                service.getTrackingForTicket(100, user);

        // Assert
        assertNotNull(result);
        assertEquals(tracking, result);

        verify(ticketTrackingDao).findByTicketId(100);
    }

    @Test
    void getTrackingForTicket_notFound() throws SQLException {

        // Arrange
        when(ticketTrackingDao.findByTicketId(100))
                .thenReturn(null);

        // Act
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getTrackingForTicket(100, user)
        );

        // Assert
        assertEquals(
                "Tracking not found for ticket 100",
                ex.getMessage()
        );

        verify(ticketTrackingDao).findByTicketId(100);
    }

    @Test
    void getTrackingForTicket_invalidTicketId() {

        // Arrange
        int ticketId = 0;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.getTrackingForTicket(ticketId, user)
        );

        // Assert
        assertEquals(
                "Ticket ID must be greater than 0.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketTrackingDao);
    }

    @Test
    void getTrackingForTicket_negativeTicketId() {

        // Arrange
        int ticketId = -1;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.getTrackingForTicket(ticketId, user)
        );

        // Assert
        assertEquals(
                "Ticket ID must be greater than 0.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketTrackingDao);
    }

    @Test
    void getTrackingForTicket_nullUser() {

        // Arrange
        User requestingUser = null;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.getTrackingForTicket(100, requestingUser)
        );

        // Assert
        assertEquals(
                "Invalid requesting user.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketTrackingDao);
    }

    @Test
    void getTrackingForTicket_userWithoutRole() {

        // Arrange
        User requestingUser = new User();
        requestingUser.setId(10);
        requestingUser.setRole(null);

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.getTrackingForTicket(100, requestingUser)
        );

        // Assert
        assertEquals(
                "Invalid requesting user.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketTrackingDao);
    }

    @Test
    void getUpdatesByUser_success() throws SQLException {

        // Arrange
        List<TicketTracking> updates = List.of(tracking);

        when(ticketTrackingDao.findByUserId(10))
                .thenReturn(updates);

        // Act
        List<TicketTracking> result =
                service.getUpdatesByUser(10, user);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tracking, result.get(0));

        verify(ticketTrackingDao).findByUserId(10);
    }

    @Test
    void getUpdatesByUser_adminCanViewAnyUser() throws SQLException {

        // Arrange
        List<TicketTracking> updates = List.of(tracking);

        when(ticketTrackingDao.findByUserId(20))
                .thenReturn(updates);

        // Act
        List<TicketTracking> result =
                service.getUpdatesByUser(20, admin);

        // Assert
        assertEquals(1, result.size());
        assertEquals(tracking, result.get(0));

        verify(ticketTrackingDao).findByUserId(20);
    }

    @Test
    void getUpdatesByUser_unauthorizedUser() {

        // Arrange
        int requestedUserId = 20;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.getUpdatesByUser(
                        requestedUserId,
                        user
                )
        );

        // Assert
        assertEquals(
                "You can only view your own tracking updates.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketTrackingDao);
    }

    @Test
    void getUpdatesByUser_invalidUserId() {

        // Arrange
        int userId = 0;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.getUpdatesByUser(userId, user)
        );

        // Assert
        assertEquals(
                "User ID must be greater than 0.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketTrackingDao);
    }

    @Test
    void getUpdatesByUser_negativeUserId() {

        // Arrange
        int userId = -1;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.getUpdatesByUser(userId, user)
        );

        // Assert
        assertEquals(
                "User ID must be greater than 0.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketTrackingDao);
    }

    @Test
    void getUpdatesByUser_nullUser() {

        // Arrange
        User requestingUser = null;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.getUpdatesByUser(10, requestingUser)
        );

        // Assert
        assertEquals(
                "Invalid requesting user.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketTrackingDao);
    }

    @Test
    void getUpdatesByUser_userWithoutRole() {

        // Arrange
        User requestingUser = new User();
        requestingUser.setId(10);
        requestingUser.setRole(null);

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.getUpdatesByUser(10, requestingUser)
        );

        // Assert
        assertEquals(
                "Invalid requesting user.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketTrackingDao);
    }

    @Test
    void getUpdatesByUser_emptyList() throws SQLException {

        // Arrange
        when(ticketTrackingDao.findByUserId(10))
                .thenReturn(List.of());

        // Act
        List<TicketTracking> result =
                service.getUpdatesByUser(10, user);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(ticketTrackingDao).findByUserId(10);
    }
}