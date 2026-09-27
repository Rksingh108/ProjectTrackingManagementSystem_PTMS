package com.ptms.app.service;

import com.ptms.app.dao.TicketDao;
import com.ptms.app.dao.TicketTrackingDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.model.Ticket;
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
class ITicketServiceTest {

    @Mock
    private TicketDao ticketDao;

    @Mock
    private TicketTrackingDao trackingDao;

    private ITicketService service;

    @BeforeEach
    void setUp() {
        service = new ITicketService(ticketDao, trackingDao);
    }

    @Test
    void shouldCreateTicket() throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        Ticket ticket = new Ticket(10, "Login Bug", "Fix login", "HIGH");

        when(ticketDao.insertTicket(ticket)).thenAnswer(i -> {
            ticket.setId(100);
            return 1;
        });

        // Act
        Ticket result = service.createTicket(ticket, admin);

        // Assert
        assertEquals(100, result.getId());
        assertEquals("IN_DEVELOPMENT", result.getStatus());

        verify(ticketDao).insertTicket(ticket);
        verify(trackingDao).insertTicket(any(TicketTracking.class));
    }

    @Test
    void shouldRejectUnauthorizedTicketCreation()
            throws SQLException {

        // Arrange
        User employee = new User();
        employee.setRole(User.Role.EMPLOYEE);

        Ticket ticket = new Ticket();

        // Act & Assert
        assertThrows(
                UnauthorizedException.class,
                () -> service.createTicket(ticket, employee)
        );

        verifyNoInteractions(ticketDao, trackingDao);
    }

    @Test
    void shouldGetTicketById() throws SQLException {

        // Arrange
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setTitle("Login Bug");

        when(ticketDao.findByTicketId(1))
                .thenReturn(ticket);

        // Act
        Ticket result = service.getTicketById(1);

        // Assert
        assertEquals(1, result.getId());
        assertEquals("Login Bug", result.getTitle());

        verify(ticketDao).findByTicketId(1);
    }

    @Test
    void shouldThrowExceptionWhenTicketNotFound()
            throws SQLException {

        // Arrange
        when(ticketDao.findByTicketId(99))
                .thenReturn(null);

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getTicketById(99)
        );
    }

    @Test
    void shouldGetTicketsForProject()
            throws SQLException {

        // Arrange
        List<Ticket> tickets = List.of(new Ticket(), new Ticket());

        when(ticketDao.findByProjectId(10))
                .thenReturn(tickets);

        // Act
        List<Ticket> result =
                service.getTicketsForProject(10);

        // Assert
        assertEquals(2, result.size());
        verify(ticketDao).findByProjectId(10);
    }

    @Test
    void shouldAssignTicket() throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        Ticket ticket = new Ticket();
        ticket.setId(10);

        when(ticketDao.findByTicketId(10))
                .thenReturn(ticket);

        // Act
        service.assignTicket(10, 20, admin);

        // Assert
        assertEquals(20, ticket.getAssignedTo());
        verify(ticketDao).updateTicket(ticket);
    }

    @Test
    void shouldAdvanceTicketStatus()
            throws SQLException {

        // Arrange
        User member = new User();
        member.setId(20);
        member.setRole(User.Role.TEAM_MEMBER);

        Ticket ticket = new Ticket();
        ticket.setId(10);
        ticket.setStatus("IN_DEVELOPMENT");
        ticket.setAssignedTo(20);

        TicketTracking tracking =
                new TicketTracking(10, "IN_DEVELOPMENT", 0, 20);

        when(ticketDao.findByTicketId(10))
                .thenReturn(ticket);

        when(trackingDao.findByTicketId(10))
                .thenReturn(tracking);

        // Act
        service.advanceStatus(
                10,
                "IN_PROGRESS",
                50,
                "Work started",
                member
        );

        // Assert
        assertEquals("IN_PROGRESS", ticket.getStatus());
        assertEquals("IN_PROGRESS", tracking.getStatus());
        assertEquals(50, tracking.getProgress());

        verify(ticketDao).updateTicket(ticket);
        verify(trackingDao).updateTicket(tracking);
    }

    @Test
    void shouldRejectInvalidStatusTransition()
            throws SQLException {

        // Arrange
        Ticket ticket = new Ticket();
        ticket.setId(10);
        ticket.setStatus("IN_DEVELOPMENT");

        when(ticketDao.findByTicketId(10))
                .thenReturn(ticket);

        User user = new User();
        user.setId(20);
        user.setRole(User.Role.TEAM_MEMBER);

        // Act & Assert
        assertThrows(
                com.ptms.app.exception.ValidationException.class,
                () -> service.advanceStatus(
                        10,
                        "COMPLETED",
                        100,
                        "Invalid",
                        user
                )
        );

        verify(ticketDao, never()).updateTicket(any());
    }

    @Test
    void shouldDeleteTicket() throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        when(ticketDao.deleteTicket(10))
                .thenReturn(1);

        // Act
        service.deleteTicket(10, admin);

        // Assert
        verify(ticketDao).deleteTicket(10);
    }

    @Test
    void shouldRejectUnauthorizedDelete()
            throws SQLException {

        // Arrange
        User member = new User();
        member.setRole(User.Role.TEAM_MEMBER);

        // Act & Assert
        assertThrows(
                UnauthorizedException.class,
                () -> service.deleteTicket(10, member)
        );

        verifyNoInteractions(ticketDao);
    }
}