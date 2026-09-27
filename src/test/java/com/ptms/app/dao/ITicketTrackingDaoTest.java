package com.ptms.app.dao;

import com.ptms.app.model.TicketTracking;
import com.ptms.app.util.DBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ITicketTrackingDaoTest {

    private ITicketTrackingDao dao;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() {
        dao = new ITicketTrackingDao();
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
    }

    @Test
    void insertTicket() throws Exception {
        TicketTracking tracking = tracking();

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.insertTicket(tracking));
            assertEquals(1, tracking.getId());
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

            TicketTracking tracking = dao.findByTicketId(10);

            assertNotNull(tracking);
            assertEquals(1, tracking.getId());
            assertEquals(10, tracking.getTicketId());
        }
    }

    @Test
    void findByUpdatedBy() throws Exception {
        mockResult();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            List<TicketTracking> list = dao.findByUpdatedBy(20);

            assertEquals(1, list.size());
            assertEquals(20, list.get(0).getUpdatedBy());
        }
    }

    @Test
    void updateTicket() throws Exception {
        TicketTracking tracking = tracking();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.updateTicket(tracking));

            verify(ps).setString(1, "IN_PROGRESS");
            verify(ps).setInt(2, 50);
            verify(ps).setInt(5, 10);
        }
    }

    @Test
    void deleteTicket() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.deleteTicket(10));
            verify(ps).setInt(1, 10);
        }
    }

    @Test
    void findByTicketId_shouldReturnNull() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertNull(dao.findByTicketId(10));
        }
    }

    @Test
    void insertTicket_shouldThrowException() throws Exception {
        TicketTracking tracking = tracking();

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
        when(ps.executeUpdate()).thenThrow(new SQLException());

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertThrows(SQLException.class, () -> dao.insertTicket(tracking));
        }
    }

    private TicketTracking tracking() {
        TicketTracking tracking = new TicketTracking();
        tracking.setTicketId(10);
        tracking.setStatus("IN_PROGRESS");
        tracking.setProgress(50);
        tracking.setComment("Development in progress");
        tracking.setUpdatedBy(20);
        return tracking;
    }

    private void mockResult() throws SQLException {
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getInt("ticket_id")).thenReturn(10);
        when(rs.getString("status")).thenReturn("IN_PROGRESS");
        when(rs.getInt("progress")).thenReturn(50);
        when(rs.getString("comment")).thenReturn("Development in progress");
        when(rs.getInt("updated_by")).thenReturn(20);
        when(rs.wasNull()).thenReturn(false);
        when(rs.getTimestamp("updated_at")).thenReturn(null);
    }
}