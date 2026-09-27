package com.ptms.app.dao;

import com.ptms.app.model.User;
import com.ptms.app.util.DBConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IUserDaoTest {

    private IUserDao dao;
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() {
        dao = new IUserDao();
        conn = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
    }

    @Test
    void insertUser() throws Exception {
        User user = user();

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(1);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.insertUser(user));
            assertEquals(1, user.getId());
        }
    }

    @Test
    void findByUserId() throws Exception {
        mockResult();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            User user = dao.findByUserId(1);

            assertNotNull(user);
            assertEquals(1, user.getId());
            assertEquals("Karan", user.getFirstName());
        }
    }

    @Test
    void findByUsername() throws Exception {
        mockResult();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            User user = dao.findByUsername("karan");

            assertNotNull(user);
            assertEquals("karan", user.getUsername());
        }
    }

    @Test
    void findAll() throws Exception {
        mockResult();

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            List<User> list = dao.findAll();

            assertEquals(1, list.size());
        }
    }

    @Test
    void searchByUserName() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertTrue(dao.searchByUserName("kar").isEmpty());
            verify(ps).setString(1, "%kar%");
            verify(ps).setString(2, "%kar%");
            verify(ps).setString(3, "%kar%");
        }
    }

    @Test
    void findByUserRole() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertTrue(dao.findByUserRole(User.Role.EMPLOYEE).isEmpty());
            verify(ps).setString(1, "EMPLOYEE");
        }
    }

    @Test
    void updateUser() throws Exception {
        User user = user();
        user.setId(1);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.updateUser(user));
            verify(ps).setInt(8, 1);
        }
    }

    @Test
    void deleteUser() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertEquals(1, dao.deleteUser(1));
            verify(ps).setInt(1, 1);
        }
    }

    @Test
    void findByUserId_shouldReturnNull() throws Exception {
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertNull(dao.findByUserId(1));
        }
    }

    @Test
    void insertUser_shouldThrowException() throws Exception {
        User user = user();

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
        when(ps.executeUpdate()).thenThrow(new SQLException());

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(conn);

            assertThrows(SQLException.class, () -> dao.insertUser(user));
        }
    }

    private User user() {
        User user = new User();
        user.setFirstName("Karan");
        user.setLastName("Singh");
        user.setUsername("karan");
        user.setEmail("karan@gmail.com");
        user.setPassword("password");
        user.setRole(User.Role.EMPLOYEE);
        user.setDateOfBirth(LocalDate.of(2002, 1, 1));
        user.setMobileNumber("9876543210");
        user.setGender("Male");
        return user;
    }

    private void mockResult() throws SQLException {
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("first_name")).thenReturn("Karan");
        when(rs.getString("last_name")).thenReturn("Singh");
        when(rs.getString("username")).thenReturn("karan");
        when(rs.getString("email")).thenReturn("karan@gmail.com");
        when(rs.getString("password")).thenReturn("password");
        when(rs.getString("role_name")).thenReturn("EMPLOYEE");
        when(rs.getDate("date_of_birth"))
                .thenReturn(Date.valueOf("2002-01-01"));
        when(rs.getString("mobile_number")).thenReturn("9876543210");
        when(rs.getString("gender")).thenReturn("Male");
    }
}