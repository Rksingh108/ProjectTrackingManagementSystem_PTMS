package com.ptms.app.service;

import com.ptms.app.dao.IUserDao;
import com.ptms.app.dao.UserDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class IUserService implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(IUserService.class);
    private final UserDao userDao;

    public IUserService() {
        this.userDao = new IUserDao();
    }

    public IUserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User registerUser(User user) throws SQLException {
        logger.info("Registering new user.");

        try {
            validateUser(user);

            if (userDao.findByUsername(user.getUsername()) != null) {
                logger.warn("Registration failed. Username already exists: {}", user.getUsername());
                throw new ValidationException("Username already exists.");
            }

            if (userDao.findByEmail(user.getEmail()) != null) {
                logger.warn("Registration failed. Email already exists: {}", user.getEmail());
                throw new ValidationException("Email already exists.");
            }

            if (user.getRole() == null) {
                logger.warn("Registration failed. Role is missing.");
                throw new ValidationException("Role is required.");
            }

            validateRegistrationRole(user.getRole());

            int rows = userDao.insertUser(user);

            if (rows == 0) {
                logger.warn("User registration failed for username: {}", user.getUsername());
                throw new ValidationException("User registration failed.");
            }

            logger.info("User registered successfully. User ID: {}, Role: {}", user.getId(), user.getRole());
            return user;

        } catch (ValidationException e) {
            logger.warn("User registration validation failed: {}", e.getMessage());
            throw e;
        } catch (SQLException e) {
            logger.error("Database error while registering user: {}",
                    user != null ? user.getUsername() : "unknown", e);
            throw e;
        }
    }

    @Override
    public User login(String username, String password) throws SQLException {
        if (isBlank(username) || isBlank(password)) {
            throw new ValidationException("Username and password are required.");
        }

        User user = userDao.findByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            throw new ValidationException("Invalid username or password.");
        }

        logger.info("User logged in successfully. User ID: {}", user.getId());
        return user;
    }

    @Override
    public User getUserById(int id) throws SQLException {
        if (id <= 0) {
            throw new ValidationException("Invalid user id.");
        }

        User user = userDao.findByUserId(id);

        if (user == null) {
            throw new ResourceNotFoundException("User not found with id " + id);
        }

        return user;
    }

    @Override
    public List<User> getAllUsers(User requestingUser) throws SQLException {
        requireAdmin(requestingUser);
        return userDao.findAll();
    }

    @Override
    public List<User> searchUsers(String keyword, User requestingUser) throws SQLException {
        requireAdmin(requestingUser);

        if (isBlank(keyword)) {
            throw new ValidationException("Search keyword is required.");
        }

        return userDao.searchByUserName(keyword.trim());
    }

    @Override
    public List<User> getUsersByRole(User.Role role, User requestingUser) throws SQLException {
        requireAdmin(requestingUser);

        if (role == null) {
            throw new ValidationException("Role is required.");
        }

        return userDao.findByUserRole(role);
    }

    @Override
    public void updateProfile(User user, User requestingUser) throws SQLException {
        validateUser(requestingUser);

        if (user == null) {
            throw new ValidationException("User is required.");
        }

        if (requestingUser.getRole() != User.Role.ADMIN && requestingUser.getId() != user.getId()) {
            throw new UnauthorizedException("You can update only your own profile.");
        }

        validateProfile(user);

        int rows = userDao.updateProfile(user);

        if (rows == 0) {
            throw new ResourceNotFoundException("User not found.");
        }

        logger.info("User profile updated. User ID: {}", user.getId());
    }

    @Override
    public void changeRole(int userId, User.Role role, User requestingUser) throws SQLException {
        requireAdmin(requestingUser);

        if (userId <= 0 || role == null) {
            throw new ValidationException("User ID and role are required.");
        }

        User user = userDao.findByUserId(userId);

        if (user == null) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }

        if (userId == requestingUser.getId() && role != User.Role.ADMIN) {
            throw new ValidationException("Admin cannot change their own role.");
        }

        int rows = userDao.updateRole(userId, role);

        if (rows == 0) {
            throw new ValidationException("Role update failed.");
        }

        logger.info("User role updated. User ID: {}, Role: {}", userId, role);
    }

    @Override
    public void deleteUser(int userId, User requestingUser) throws SQLException {
        requireAdmin(requestingUser);

        if (userId <= 0) {
            throw new ValidationException("Invalid user id.");
        }

        if (userId == requestingUser.getId()) {
            throw new ValidationException("Admin cannot delete their own account.");
        }

        if (userDao.findByUserId(userId) == null) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }

        int rows = userDao.deleteUser(userId);

        if (rows == 0) {
            throw new ValidationException("User deletion failed.");
        }

        logger.info("User deleted. User ID: {}", userId);
    }

    private void requireAdmin(User user) {
        validateUser(user);

        if (user.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN can perform this operation.");
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("User is required.");
        }

        if (isBlank(user.getFirstName())
                || isBlank(user.getLastName())
                || isBlank(user.getUsername())
                || isBlank(user.getEmail())
                || isBlank(user.getPassword())) {
            throw new ValidationException(
                    "First name, last name, username, email and password are required.");
        }
    }

    private void validateProfile(User user) {
        if (isBlank(user.getFirstName())
                || isBlank(user.getLastName())
                || isBlank(user.getEmail())) {
            throw new ValidationException("First name, last name and email are required.");
        }
    }

    private void validateRegistrationRole(User.Role role) {
        if (role != User.Role.ADMIN
                && role != User.Role.PROJECT_MANAGER
                && role != User.Role.TEAM_LEAD
                && role != User.Role.TEAM_MEMBER) {
            logger.warn("Invalid registration role: {}", role);
            throw new ValidationException("Invalid registration role.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}