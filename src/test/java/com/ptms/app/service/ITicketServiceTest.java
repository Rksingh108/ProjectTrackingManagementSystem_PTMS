package com.ptms.app.service;

import com.ptms.app.dao.TicketDao;
import com.ptms.app.dao.TicketTrackingDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Ticket;
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

    private User admin;
    private User manager;
    private User teamLead;
    private User teamMember;

    private Ticket ticket;

    @BeforeEach
    void setUp() {

        // Arrange
        service = new ITicketService(ticketDao, trackingDao);

        admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        manager = new User();
        manager.setId(10);
        manager.setRole(User.Role.PROJECT_MANAGER);

        teamLead = new User();
        teamLead.setId(20);
        teamLead.setRole(User.Role.TEAM_LEAD);

        teamMember = new User();
        teamMember.setId(30);
        teamMember.setRole(User.Role.TEAM_MEMBER);

        ticket = new Ticket();
        ticket.setId(100);
        ticket.setProjectId(1);
        ticket.setTitle("Login Bug");
        ticket.setDescription("Fix login validation");
        ticket.setPriority("HIGH");
        ticket.setStatus("IN_DEVELOPMENT");
        ticket.setAssignedTo(30);
    }

    @Test
    void getTicketById_success() throws SQLException {

        // Arrange
        when(ticketDao.findByTicketId(100)).thenReturn(ticket);

        // Act
        Ticket result = service.getTicketById(100);

        // Assert
        assertEquals(ticket, result);
        verify(ticketDao).findByTicketId(100);
    }

    @Test
    void getTicketById_notFound() throws SQLException {

        // Arrange
        when(ticketDao.findByTicketId(100)).thenReturn(null);

        // Act
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getTicketById(100)
        );

        // Assert
        assertEquals(
                "Ticket not found with id 100",
                ex.getMessage()
        );
    }

    @Test
    void getTicketById_invalidId() {

        // Arrange
        int ticketId = 0;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.getTicketById(ticketId)
        );

        // Assert
        assertEquals(
                "Ticket ID must be greater than zero.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void getTicketsForProject_success() throws SQLException {

        // Arrange
        when(ticketDao.findByProjectId(1))
                .thenReturn(List.of(ticket));

        // Act
        List<Ticket> result =
                service.getTicketsForProject(1);

        // Assert
        assertEquals(1, result.size());
        assertEquals(ticket, result.get(0));

        verify(ticketDao).findByProjectId(1);
    }

    @Test
    void getTicketsForUser_success() throws SQLException {

        // Arrange
        when(ticketDao.findByAssignedTo(30))
                .thenReturn(List.of(ticket));

        // Act
        List<Ticket> result =
                service.getTicketsForUser(30);

        // Assert
        assertEquals(1, result.size());
        assertEquals(ticket, result.get(0));

        verify(ticketDao).findByAssignedTo(30);
    }

    @Test
    void createTicket_nullTicket() {

        // Arrange
        Ticket invalidTicket = null;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createTicket(invalidTicket, teamLead)
        );

        // Assert
        assertEquals(
                "Ticket cannot be null.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void createTicket_invalidProjectId() {

        // Arrange
        ticket.setProjectId(0);

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createTicket(ticket, teamLead)
        );

        // Assert
        assertEquals(
                "Project ID is required.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void createTicket_invalidTitle() {

        // Arrange
        ticket.setTitle("");

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createTicket(ticket, teamLead)
        );

        // Assert
        assertEquals(
                "Ticket title is required.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void createTicket_invalidDescription() {

        // Arrange
        ticket.setDescription("");

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createTicket(ticket, teamLead)
        );

        // Assert
        assertEquals(
                "Ticket description is required.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void createTicket_invalidPriority() {

        // Arrange
        ticket.setPriority("URGENT");

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createTicket(ticket, teamLead)
        );

        // Assert
        assertEquals(
                "Priority must be LOW, MEDIUM or HIGH.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void createTicket_unauthorizedUser() {

        // Arrange
        User user = teamMember;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.createTicket(ticket, user)
        );

        // Assert
        assertEquals(
                "Only TEAM_LEAD or ADMIN can create tickets.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void createTicket_nullUser() {

        // Arrange
        User user = null;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.createTicket(ticket, user)
        );

        // Assert
        assertEquals(
                "Invalid requesting user.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void assignTicket_unauthorizedUser() {

        // Arrange
        User user = teamMember;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.assignTicket(100, 30, user)
        );

        // Assert
        assertEquals(
                "Only TEAM_LEAD or ADMIN can assign tickets.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void assignTicket_invalidTicketId() {

        // Arrange
        int ticketId = 0;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.assignTicket(ticketId, 30, teamLead)
        );

        // Assert
        assertEquals(
                "Ticket ID must be greater than zero.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void assignTicket_invalidUserId() {

        // Arrange
        int userId = 0;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.assignTicket(100, userId, teamLead)
        );

        // Assert
        assertEquals(
                "User ID must be greater than zero.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void advanceStatus_nullUser() {

        // Arrange
        User user = null;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.advanceStatus(
                        100,
                        "IN_PROGRESS",
                        25,
                        "Started work",
                        user
                )
        );

        // Assert
        assertEquals(
                "Invalid requesting user.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void advanceStatus_invalidStatus() {

        // Arrange
        String status = "INVALID";

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.advanceStatus(
                        100,
                        status,
                        25,
                        "Update",
                        teamMember
                )
        );

        // Assert
        assertEquals(
                "Invalid ticket status.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void advanceStatus_emptyStatus() {

        // Arrange
        String status = "";

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.advanceStatus(
                        100,
                        status,
                        25,
                        "Update",
                        teamMember
                )
        );

        // Assert
        assertEquals(
                "Status is required.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void advanceStatus_invalidProgress() {

        // Arrange
        int progress = 101;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.advanceStatus(
                        100,
                        "IN_PROGRESS",
                        progress,
                        "Update",
                        teamMember
                )
        );

        // Assert
        assertEquals(
                "Progress must be between 0 and 100.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void advanceStatus_negativeProgress() {

        // Arrange
        int progress = -1;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.advanceStatus(
                        100,
                        "IN_PROGRESS",
                        progress,
                        "Update",
                        teamMember
                )
        );

        // Assert
        assertEquals(
                "Progress must be between 0 and 100.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void advanceStatus_invalidTransition() throws SQLException {

        // Arrange
        when(ticketDao.findByTicketId(100))
                .thenReturn(ticket);

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.advanceStatus(
                        100,
                        "COMPLETED",
                        100,
                        "Done",
                        teamMember
                )
        );

        // Assert
        assertEquals(
                "Cannot move ticket from IN_DEVELOPMENT to COMPLETED",
                ex.getMessage()
        );

        verify(ticketDao).findByTicketId(100);
        verify(ticketDao, never()).updateTicket(any());
    }

    @Test
    void advanceStatus_teamMemberMustBeAssigned() throws SQLException {

        // Arrange
        ticket.setAssignedTo(99);

        when(ticketDao.findByTicketId(100))
                .thenReturn(ticket);

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.advanceStatus(
                        100,
                        "IN_PROGRESS",
                        25,
                        "Started",
                        teamMember
                )
        );

        // Assert
        assertEquals(
                "Only the assigned Team Member, Team Lead or Project Manager can update ticket status.",
                ex.getMessage()
        );

        verify(ticketDao).findByTicketId(100);
        verify(ticketDao, never()).updateTicket(any());
    }

    @Test
    void deleteTicket_unauthorizedUser() {

        // Arrange
        User user = manager;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.deleteTicket(100, user)
        );

        // Assert
        assertEquals(
                "Only ADMIN can delete tickets.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void deleteTicket_invalidUser() {

        // Arrange
        User user = null;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.deleteTicket(100, user)
        );

        // Assert
        assertEquals(
                "Invalid requesting user.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }

    @Test
    void deleteTicket_invalidId() {

        // Arrange
        int ticketId = 0;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.deleteTicket(ticketId, admin)
        );

        // Assert
        assertEquals(
                "Ticket ID must be greater than zero.",
                ex.getMessage()
        );

        verifyNoInteractions(ticketDao);
    }
}