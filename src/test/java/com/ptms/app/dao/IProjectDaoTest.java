package com.ptms.app.dao;

import com.ptms.app.model.Project;
import com.ptms.app.util.DBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IProjectDaoTest {

    private IProjectDao dao;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() {
        dao = new IProjectDao();
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
    }

    @Test
    void insertProject() throws Exception {
        Project p = project();

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.insertProject(p));
            assertEquals(1, p.getId());
        }
    }

    @Test
    void findByProjectId() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("name")).thenReturn("PTMS");
        when(rs.getString("requirements")).thenReturn("Project Tracking");
        when(rs.getInt("manager_id")).thenReturn(10);
        when(rs.getInt("team_lead_id")).thenReturn(20);
        when(rs.getInt("client_id")).thenReturn(30);
        when(rs.wasNull()).thenReturn(false);
        when(rs.getString("domain")).thenReturn("IT");
        when(rs.getBigDecimal("cost"))
                .thenReturn(new BigDecimal("500000"));
        when(rs.getDate("start_date"))
                .thenReturn(Date.valueOf("2026-09-01"));
        when(rs.getDate("deadline"))
                .thenReturn(Date.valueOf("2026-12-31"));
        when(rs.getString("priority")).thenReturn("HIGH");
        when(rs.getString("status")).thenReturn("IN_PROGRESS");

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            Project p = dao.findByProjectId(1);

            assertNotNull(p);
            assertEquals(1, p.getId());
            assertEquals("PTMS", p.getName());
        }
    }

    @Test
    void findAll() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            List<Project> list = dao.findAll();

            assertTrue(list.isEmpty());
        }
    }

    @Test
    void findByManagerId() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertTrue(dao.findByManagerId(10).isEmpty());
            verify(ps).setInt(1, 10);
        }
    }

    @Test
    void findByTeamLeadId() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertTrue(dao.findByTeamLeadId(20).isEmpty());
            verify(ps).setInt(1, 20);
        }
    }

    @Test
    void findByClientId() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertTrue(dao.findByClientId(30).isEmpty());
            verify(ps).setInt(1, 30);
        }
    }

    @Test
    void updateProject() throws Exception {
        Project p = project();
        p.setId(1);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.updateProject(p));
            verify(ps).setInt(12, 1);
            verify(ps).executeUpdate();
        }
    }

    @Test
    void deleteProject() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.deleteProject(1));
            verify(ps).setInt(1, 1);
        }
    }

    @Test
    void findByProjectId_shouldThrowException() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenThrow(new SQLException());

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertThrows(
                    SQLException.class,
                    () -> dao.findByProjectId(1)
            );
        }
    }

    private Project project() {
        Project p = new Project();

        p.setName("PTMS");
        p.setRequirements("Project Tracking");
        p.setManagerId(10);
        p.setTeamLeadId(20);
        p.setClientId(30);
        p.setDomain("IT");
        p.setCost(new BigDecimal("500000"));
        p.setStartDate(LocalDate.of(2026, 9, 1));
        p.setDeadline(LocalDate.of(2026, 12, 31));
        p.setPriority("HIGH");
        p.setStatus("IN_PROGRESS");

        return p;
    }
}