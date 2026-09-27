package com.ptms.app.dao;

import com.ptms.app.model.Ticket;
import com.ptms.app.util.DBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ITicketDaoTest {

    private ITicketDao dao;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() {
        dao = new ITicketDao();
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
    }

    @Test
    void insertTicket() throws Exception {
        Ticket ticket = ticket();

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.insertTicket(ticket));
            assertEquals(1, ticket.getId());
        }
    }

    @Test
    void findByTicketId() throws Exception {
        mockResult();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            Ticket ticket = dao.findByTicketId(1);

            assertNotNull(ticket);
            assertEquals(1, ticket.getId());
            assertEquals("Login Issue", ticket.getTitle());
        }
    }

    @Test
    void findByProjectId() throws Exception {
        mockResult();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            List<Ticket> list = dao.findByProjectId(10);

            assertEquals(1, list.size());
            assertEquals(10, list.get(0).getProjectId());
        }
    }

    @Test
    void findByAssignedTo() throws Exception {
        mockResult();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            List<Ticket> list = dao.findByAssignedTo(20);

            assertEquals(1, list.size());
            assertEquals(20, list.get(0).getAssignedTo());
        }
    }

    @Test
    void updateTicket() throws Exception {
        Ticket ticket = ticket();
        ticket.setId(1);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.updateTicket(ticket));
            verify(ps).setInt(7, 1);
        }
    }

    @Test
    void deleteTicket() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.deleteTicket(1));
            verify(ps).setInt(1, 1);
        }
    }

    @Test
    void findByTicketId_shouldReturnNull() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertNull(dao.findByTicketId(1));
        }
    }

    @Test
    void insertTicket_shouldThrowException() throws Exception {
        Ticket ticket = ticket();

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
        when(ps.executeUpdate()).thenThrow(new SQLException());

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertThrows(SQLException.class, () -> dao.insertTicket(ticket));
        }
    }

    private Ticket ticket() {
        Ticket ticket = new Ticket();
        ticket.setProjectId(10);
        ticket.setTitle("Login Issue");
        ticket.setDescription("Login is not working");
        ticket.setPriority("HIGH");
        ticket.setDeadline(LocalDate.of(2026, 12, 31));
        ticket.setAssignedTo(20);
        ticket.setStatus("OPEN");
        return ticket;
    }

    private void mockResult() throws SQLException {
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getInt("project_id")).thenReturn(10);
        when(rs.getString("title")).thenReturn("Login Issue");
        when(rs.getString("description")).thenReturn("Login is not working");
        when(rs.getString("priority")).thenReturn("HIGH");
        when(rs.getDate("deadline"))
                .thenReturn(Date.valueOf("2026-12-31"));
        when(rs.getInt("assigned_to")).thenReturn(20);
        when(rs.wasNull()).thenReturn(false);
        when(rs.getTimestamp("created_at")).thenReturn(null);
        when(rs.getString("status")).thenReturn("OPEN");
    }
}