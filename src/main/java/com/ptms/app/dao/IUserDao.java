package com.ptms.app.dao;

import com.ptms.app.model.User;
import com.ptms.app.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IUserDao implements UserDao {

    private static final Logger logger = LoggerFactory.getLogger(IUserDao.class);

    private static final String INSERT = """
            INSERT INTO users
            (first_name,last_name,username,email,password,
             role_name,date_of_birth,mobile_number,gender)
            VALUES (?,?,?,?,?,?,?,?,?)
            """;

    private static final String FIND_BY_ID = "SELECT * FROM users WHERE id=?";

    private static final String FIND_BY_USERNAME = "SELECT * FROM users WHERE username=?";

    private static final String FIND_BY_EMAIL = "SELECT * FROM users WHERE email=?";

    private static final String FIND_ALL = "SELECT * FROM users ORDER BY id";

    private static final String SEARCH = """
            SELECT * FROM users
            WHERE first_name LIKE ?
            OR last_name LIKE ?
            OR username LIKE ?
            ORDER BY id
            """;

    private static final String FIND_BY_ROLE = "SELECT * FROM users WHERE role_name=? ORDER BY id";

    private static final String UPDATE_PROFILE = """
            UPDATE users SET
                first_name=?,
                last_name=?,
                email=?,
                date_of_birth=?,
                mobile_number=?,
                gender=?
            WHERE id=?
            """;

    private static final String UPDATE_ROLE = "UPDATE users SET role_name=? WHERE id=?";

    private static final String DELETE = "DELETE FROM users WHERE id=?";

    @Override
    public int insertUser(User user) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getUsername());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPassword());
            statement.setString(6, user.getRole().name());
            statement.setObject(7, user.getDateOfBirth());
            statement.setString(8, user.getMobileNumber());
            statement.setString(9, user.getGender());

            int rows = statement.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }

                logger.info("User created successfully. User ID: {}, Username: {}",
                        user.getId(), user.getUsername());
            } else {
                logger.warn("User creation affected 0 rows. Username: {}",
                        user.getUsername());
            }

            return rows;
        } catch (SQLException e) {
            logger.error("Database error while creating user. Username: {}",
                    user.getUsername(), e);
            throw e;
        }
    }

    @Override
    public User findByUserId(int id) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    @Override
    public User findByUsername(String username) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME)) {

            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    @Override
    public User findByEmail(String email) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL)) {

            statement.setString(1, email);

            try (ResultSet rs = statement.executeQuery()) {
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

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }

        return users;
    }

    @Override
    public List<User> searchByUserName(String keyword) throws SQLException {
        List<User> users = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SEARCH)) {

            String value = "%" + keyword + "%";
            statement.setString(1, value);
            statement.setString(2, value);
            statement.setString(3, value);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }

        return users;
    }

    @Override
    public List<User> findByUserRole(User.Role role) throws SQLException {
        List<User> users = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ROLE)) {

            statement.setString(1, role.name());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }

        return users;
    }

    @Override
    public int updateProfile(User user) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PROFILE)) {

            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setObject(4, user.getDateOfBirth());
            statement.setString(5, user.getMobileNumber());
            statement.setString(6, user.getGender());
            statement.setInt(7, user.getId());

            return statement.executeUpdate();
        }
    }

    @Override
    public int updateRole(int userId, User.Role role) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ROLE)) {

            statement.setString(1, role.name());
            statement.setInt(2, userId);

            return statement.executeUpdate();
        }
    }

    @Override
    public int deleteUser(int id) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {

            statement.setInt(1, id);
            return statement.executeUpdate();
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

        user.setRole(User.Role.fromString(rs.getString("role_name")));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            user.setDateOfBirth(dob.toLocalDate());
        }

        user.setMobileNumber(rs.getString("mobile_number"));
        user.setGender(rs.getString("gender"));

        return user;
    }
}