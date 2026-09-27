package com.ptms.app.dao;

import com.ptms.app.model.ProjectMember;
import com.ptms.app.util.DBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IProjectMemberDaoTest {

    private IProjectMemberDao dao;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() {
        dao = new IProjectMemberDao();
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
    }

    @Test
    void insertMember() throws Exception {
        ProjectMember member = member();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.insertMember(member));

            verify(ps).setInt(1, 10);
            verify(ps).setInt(2, 20);
            verify(ps).setString(3, "Developer");
        }
    }

    @Test
    void findByProjectId() throws Exception {
        mockResult();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            List<ProjectMember> list = dao.findByProjectId(10);

            assertEquals(1, list.size());
            assertEquals(10, list.get(0).getProjectId());
            assertEquals(20, list.get(0).getUserId());
        }
    }

    @Test
    void findByUserId() throws Exception {
        mockResult();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            List<ProjectMember> list = dao.findByUserId(20);

            assertEquals(1, list.size());
            assertEquals(20, list.get(0).getUserId());
        }
    }

    @Test
    void updateRole() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.updateRole(10, 20, "Team Lead"));

            verify(ps).setString(1, "Team Lead");
            verify(ps).setInt(2, 10);
            verify(ps).setInt(3, 20);
        }
    }

    @Test
    void deleteMember() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.deleteMember(10, 20));

            verify(ps).setInt(1, 10);
            verify(ps).setInt(2, 20);
        }
    }

    @Test
    void findByProjectId_shouldReturnEmptyList() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertTrue(dao.findByProjectId(10).isEmpty());
        }
    }

    @Test
    void insertMember_shouldThrowException() throws Exception {
        ProjectMember member = member();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenThrow(new SQLException());

        try (MockedStatic<DBConnection> db =
                     mockStatic(DBConnection.class)) {

            db.when(DBConnection::getConnection).thenReturn(conn);

            assertThrows(
                    SQLException.class,
                    () -> dao.insertMember(member)
            );
        }
    }

    private ProjectMember member() {
        ProjectMember member = new ProjectMember();
        member.setProjectId(10);
        member.setUserId(20);
        member.setRoleInProject("Developer");
        return member;
    }

    private void mockResult() throws SQLException {
        when(rs.getInt("project_id")).thenReturn(10);
        when(rs.getInt("user_id")).thenReturn(20);
        when(rs.getString("role_in_project"))
                .thenReturn("Developer");
        when(rs.getTimestamp("joined_at"))
                .thenReturn(null);
    }
}