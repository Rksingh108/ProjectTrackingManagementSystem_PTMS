package com.ptms.app.service;

import com.ptms.app.dao.IUserDao;
import com.ptms.app.dao.UserDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.User;
import com.ptms.app.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class IUserService implements UserService {

    private final UserDao userDao;

    public IUserService() {
        this.userDao = new IUserDao();
    }

    public IUserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User registerUser(User user) throws SQLException {
        validateUser(user);

        if (userDao.findByUsername(user.getUsername()) != null) {
            throw new ValidationException("Username already exists.");
        }

        if (userDao.findByEmail(user.getEmail()) != null) {
            throw new ValidationException("Email already exists.");
        }

        user.setRole(User.Role.TEAM_MEMBER);

        int rows = userDao.insertUser(user);

        if (rows == 0) {
            throw new ValidationException("User registration failed.");
        }

        return user;
    }

    @Override
    public User login(String username, String password) throws SQLException {
        if (ValidationUtil.isBlank(username)
                || ValidationUtil.isBlank(password)) {
            throw new ValidationException("Username and password are required.");
        }

        User user = userDao.findByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            throw new ValidationException("Invalid username or password.");
        }

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

        if (ValidationUtil.isBlank(keyword)) {
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
    public void updateProfile(User user) throws SQLException {
        if (user == null || user.getId() <= 0) {
            throw new ValidationException("Invalid user.");
        }

        validateProfile(user);

        User existing = getUserById(user.getId());

        if (!existing.getEmail().equalsIgnoreCase(user.getEmail())) {
            User emailUser = userDao.findByEmail(user.getEmail());

            if (emailUser != null && emailUser.getId() != user.getId()) {
                throw new ValidationException("Email already exists.");
            }
        }

        int rows = userDao.updateProfile(user);

        if (rows == 0) {
            throw new ResourceNotFoundException("User not found.");
        }
    }

    @Override
    public void changeRole(
            int userId,
            User.Role newRole,
            User requestingUser
    ) throws SQLException {
        requireAdmin(requestingUser);

        if (userId <= 0) {
            throw new ValidationException("Invalid user id.");
        }

        if (newRole == null) {
            throw new ValidationException("Role is required.");
        }

        if (userId == requestingUser.getId() && newRole != User.Role.ADMIN) {
            throw new ValidationException("You cannot remove your own ADMIN role.");
        }

        getUserById(userId);

        int rows = userDao.updateRole(userId, newRole);

        if (rows == 0) {
            throw new ResourceNotFoundException("Unable to change user role.");
        }
    }

    @Override
    public void deleteUser(
            int userId,
            User requestingUser
    ) throws SQLException {
        requireAdmin(requestingUser);

        if (userId <= 0) {
            throw new ValidationException("Invalid user id.");
        }

        if (userId == requestingUser.getId()) {
            throw new ValidationException("You cannot delete your own account.");
        }

        getUserById(userId);

        int rows = userDao.deleteUser(userId);

        if (rows == 0) {
            throw new ResourceNotFoundException("User not found.");
        }
    }

    private void requireAdmin(User user) {
        if (user == null || user.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN can perform this operation.");
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("User cannot be null.");
        }

        validateProfile(user);

        if (ValidationUtil.isBlank(user.getUsername())) {
            throw new ValidationException("Username is required.");
        }

        if (!ValidationUtil.isValidUsername(user.getUsername())) {
            throw new ValidationException(
                    "Username must contain 4-30 characters using letters, numbers or underscore."
            );
        }

        if (ValidationUtil.isBlank(user.getPassword())) {
            throw new ValidationException("Password is required.");
        }

        if (!ValidationUtil.isValidPassword(user.getPassword())) {
            throw new ValidationException("Password must contain at least 8 characters.");
        }
    }

    private void validateProfile(User user) {
        if (!ValidationUtil.isValidName(user.getFirstName())) {
            throw new ValidationException("Invalid first name.");
        }

        if (!ValidationUtil.isValidName(user.getLastName())) {
            throw new ValidationException("Invalid last name.");
        }

        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new ValidationException("Invalid email.");
        }

        if (!ValidationUtil.isBlank(user.getMobileNumber())
                && !ValidationUtil.isValidPhone(user.getMobileNumber())) {
            throw new ValidationException("Invalid mobile number.");
        }
    }
}