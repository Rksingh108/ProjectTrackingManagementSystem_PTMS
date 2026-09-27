package com.ptms.app.dao;

import com.ptms.app.model.User;
import com.ptms.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class IUserDao implements UserDao {

    private static final Logger logger =
            Logger.getLogger(IUserDao.class.getName());

    private static final String INSERT = "INSERT INTO users " +
            "(first_name, last_name, username, email, password, " +
            "role_name, date_of_birth, mobile_number, gender) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID = "SELECT * FROM users WHERE id = ?";

    private static final String FIND_BY_USERNAME = "SELECT * FROM users WHERE username = ?";

    private static final String FIND_BY_EMAIL = "SELECT * FROM users WHERE email = ?";

    private static final String FIND_ALL = "SELECT * FROM users ORDER BY id";

    private static final String SEARCH = "SELECT * FROM users " +
            "WHERE first_name LIKE ? " +
            "OR last_name LIKE ? " +
            "OR username LIKE ? " +
            "ORDER BY id";

    private static final String FIND_BY_ROLE =
            "SELECT * FROM users WHERE role_name = ? ORDER BY id";

    private static final String UPDATE_PROFILE = "UPDATE users SET " +
            "first_name = ?, last_name = ?, email = ?, " +
            "date_of_birth = ?, mobile_number = ?, gender = ? " +
            "WHERE id = ?";

    private static final String UPDATE_ROLE =
            "UPDATE users SET role_name = ? WHERE id = ?";

    private static final String DELETE =
            "DELETE FROM users WHERE id = ?";

    @Override
    public int insertUser(User user) throws SQLException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     INSERT,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPassword());
            ps.setString(6, user.getRole().name());
            ps.setObject(7, user.getDateOfBirth());
            ps.setString(8, user.getMobileNumber());
            ps.setString(9, user.getGender());

            int rows = ps.executeUpdate();

            if (rows > 0) {

                try (ResultSet rs = ps.getGeneratedKeys()) {

                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
            }

            return rows;
        }
    }

    @Override
    public User findByUserId(int id) throws SQLException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapRow(rs);
                }

                return null;
            }
        }
    }

    @Override
    public User findByUsername(String username)
            throws SQLException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_USERNAME)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapRow(rs);
                }

                return null;
            }
        }
    }

    @Override
    public User findByEmail(String email)
            throws SQLException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_EMAIL)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapRow(rs);
                }

                return null;
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {

        List<User> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }

        return users;
    }

    @Override
    public List<User> searchByUserName(String keyword)
            throws SQLException {

        List<User> users = new ArrayList<>();

        String pattern = "%" + keyword + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SEARCH)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }

        return users;
    }

    @Override
    public List<User> findByUserRole(User.Role role)
            throws SQLException {

        List<User> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ROLE)) {

            ps.setString(1, role.name());

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }

        return users;
    }

    @Override
    public int updateProfile(User user)
            throws SQLException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_PROFILE)) {

            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setObject(4, user.getDateOfBirth());
            ps.setString(5, user.getMobileNumber());
            ps.setString(6, user.getGender());
            ps.setInt(7, user.getId());

            return ps.executeUpdate();
        }
    }

    @Override
    public int updateRole(int userId, User.Role role)
            throws SQLException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_ROLE)) {

            ps.setString(1, role.name());
            ps.setInt(2, userId);

            return ps.executeUpdate();
        }
    }

    @Override
    public int deleteUser(int id) throws SQLException {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {

            ps.setInt(1, id);

            return ps.executeUpdate();
        }
    }

    private User mapRow(ResultSet rs)
            throws SQLException {

        User user = new User();

        user.setId(rs.getInt("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));

        user.setRole(
                User.Role.fromString(
                        rs.getString("role_name")
                )
        );

        Date dob = rs.getDate("date_of_birth");

        if (dob != null) {
            user.setDateOfBirth(dob.toLocalDate());
        }

        user.setMobileNumber(rs.getString("mobile_number"));
        user.setGender(rs.getString("gender"));

        return user;
    }
}