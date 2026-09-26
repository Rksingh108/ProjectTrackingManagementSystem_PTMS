package com.ptms.app.dao;

import com.ptms.app.model.User;
import com.ptms.app.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class IUserDao implements UserDao {

    private static final Logger logger = Logger.getLogger(IUserDao.class.getName());

    private final String insertUser =
            "INSERT INTO users (first_name, last_name, username, email, password, " +
                    "role_name, date_of_birth, mobile_number, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final String findUserById =
            "SELECT * FROM users WHERE id = ?";

    private final String findUserByUsername =
            "SELECT * FROM users WHERE username = ?";

    private final String findAllUsers =
            "SELECT * FROM users ORDER BY id";

    private final String searchUserByName =
            "SELECT * FROM users WHERE first_name LIKE ? OR last_name LIKE ? OR username LIKE ? ORDER BY id";

    private final String findUserByRole =
            "SELECT * FROM users WHERE role_name = ? ORDER BY id";

    private final String updateUser =
            "UPDATE users SET first_name = ?, last_name = ?, email = ?, role_name = ?, " +
                    "date_of_birth = ?, mobile_number = ?, gender = ? WHERE id = ?";

    private final String deleteUser =
            "DELETE FROM users WHERE id = ?";

    @Override
    public int insert(User user) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPassword());
            ps.setString(6, user.getRole().name());
            ps.setObject(7, user.getDateOfBirth());
            ps.setString(8, user.getMobileNumber());
            ps.setString(9, user.getGender());

            int count = ps.executeUpdate();
            if (count > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
                    }
                }
                logger.info("User inserted successfully, id=" + user.getId());
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to insert user username=" + user.getUsername() + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public User findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findUserById)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            logger.severe("Failed to fetch user id=" + id + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public User findByUsername(String username) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findUserByUsername)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            logger.severe("Failed to fetch user username=" + username + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findAllUsers);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;

        } catch (SQLException e) {
            logger.severe("Failed to fetch all users : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<User> searchByName(String keyword) throws SQLException {
        List<User> users = new ArrayList<>();
        String likePattern = "%" + keyword + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(searchUserByName)) {

            ps.setString(1, likePattern);
            ps.setString(2, likePattern);
            ps.setString(3, likePattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
            return users;

        } catch (SQLException e) {
            logger.severe("Failed to search users keyword=" + keyword + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<User> findByRole(User.Role role) throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findUserByRole)) {

            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
            return users;

        } catch (SQLException e) {
            logger.severe("Failed to fetch users by role=" + role + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int update(User user) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateUser)) {

            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setObject(5, user.getDateOfBirth());
            ps.setString(6, user.getMobileNumber());
            ps.setString(7, user.getGender());
            ps.setInt(8, user.getId());

            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("User updated successfully, id=" + user.getId());
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to update user id=" + user.getId() + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int delete(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteUser)) {

            ps.setInt(1, id);
            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("User deleted successfully, id=" + id);
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to delete user id=" + id + " : " + e.getMessage());
            throw e;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(User.Role.valueOf(rs.getString("role_name")));

        java.sql.Date dob = rs.getDate("date_of_birth");
        user.setDateOfBirth(dob != null ? dob.toLocalDate() : null);

        user.setMobileNumber(rs.getString("mobile_number"));
        user.setGender(rs.getString("gender"));
        return user;
    }
}