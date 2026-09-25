package com.ptms.app.dao;

import com.ptms.app.model.User;
import com.ptms.app.util.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IUserDao implements UserDao {
    private final String insertUser = "INSERT INTO users (first_name, last_name, username, email, password, " +
            "role_name, date_of_birth, mobile_number, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final String findUserById = "SELECT * FROM users WHERE id = ?";
    private final String findUserByName = "SELECT * FROM users WHERE username = ?";
    private final String findAllUser = "SELECT * FROM users ORDER BY id";
    private final String findUserByRole = "SELECT * FROM users WHERE role_name = ? ORDER BY id";
    private final String searchByname = "SELECT * FROM users WHERE first_name LIKE ? OR last_name LIKE ? OR username LIKE ? ORDER BY id";
    private final String updateUser = "UPDATE users SET first_name = ?, last_name = ?, username = ?, email = ?, " +
            "role_name = ?, date_of_birth = ?, mobile_number = ?, gender = ? WHERE id = ?";
    private final String deleteUser = "DELETE FROM users WHERE id = ?";
    private final String userLoginValidation = "SELECT * FROM users WHERE username = ? AND password = ?";

    @Override
    public int insert(User user) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPassword());
            ps.setString(6, user.getRoleName());
            ps.setDate(7, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
            ps.setString(8, user.getMobileNumber());
            ps.setString(9, user.getGender());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    @Override
    public User findById(int id) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findUserById)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public User findByUsername(String username) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findUserByName)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<User> findAll() throws SQLException {

        List<User> users = new ArrayList<>();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findAllUser);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    @Override
    public List<User> findByRole(String roleName) throws SQLException {

        List<User> users = new ArrayList<>();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findUserByRole)) {

            ps.setString(1, roleName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }
        return users;
    }

    @Override
    public List<User> searchByName(String keyword) throws SQLException {

        List<User> users = new ArrayList<>();
        String pattern = "%" + keyword + "%";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(searchByname)) {

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
    public boolean update(User user) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateUser)) {

            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRoleName());
            ps.setDate(6, user.getDateOfBirth() != null ? Date.valueOf(user.getDateOfBirth()) : null);
            ps.setString(7, user.getMobileNumber());
            ps.setString(8, user.getGender());
            ps.setInt(9, user.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteUser)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public User validateLogin(String username, String hashedPassword) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(userLoginValidation)) {

            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        Date dob = rs.getDate("date_of_birth");
        return new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("role_name"),
                dob != null ? dob.toLocalDate() : null,
                rs.getString("mobile_number"),
                rs.getString("gender")
        );
    }
}