package com.ptms.app.service;

import com.ptms.app.dao.IUserDao;
import com.ptms.app.dao.UserDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class IUserService implements UserService {

    private static final Logger logger = Logger.getLogger(IUserService.class.getName());
    private final UserDao userDao;
    public IUserService() {
        this.userDao = new IUserDao();
    }
    public IUserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User registerUser(User newUser) throws SQLException {
        if (userDao.findByUsername(newUser.getUsername()) != null) {
            throw new ValidationException("Username '" + newUser.getUsername() + "' is already taken.");
        }
        int rows = userDao.insertUser(newUser);
        if (rows == 0) {
            throw new ValidationException("Failed to register user '" + newUser.getUsername() + "'.");
        }
        logger.info("Registered new user: " + newUser.getUsername());
        return newUser;
    }

    @Override
    public User login(String username, String password) throws SQLException {
        User user = userDao.findByUsername(username);
        if (user == null) {
            throw new ValidationException("Invalid username or password.");
        }

        if (!user.getPassword().equals(password)) {
            throw new ValidationException("Invalid username or password.");
        }
        logger.info("User logged in: " + username);
        return user;
    }

    @Override
    public User getUserById(int id) throws SQLException {
        User user = userDao.findByUserId(id);
        if (user == null) {
            throw new ResourceNotFoundException("No user found with id " + id);
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        return userDao.findAll();
    }

    @Override
    public List<User> searchUsers(String keyword) throws SQLException {
        return userDao.searchByUserName(keyword);
    }

    @Override
    public List<User> getUsersByRole(User.Role role) throws SQLException {
        return userDao.findByUserRole(role);
    }

    @Override
    public void updateUser(User user) throws SQLException {
        int rows = userDao.updateUser(user);
        if (rows == 0) {
            throw new ResourceNotFoundException("No user found with id " + user.getId() + " to update.");
        }
        logger.info("Updated user id=" + user.getId());
    }

    @Override
    public void changeRole(int userId, User.Role newRole, User requestingUser) throws SQLException {
        if (requestingUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only an Admin can change a user's role.");
        }
        User target = getUserById(userId);
        target.setRole(newRole);
        userDao.updateUser(target);
        logger.info("Role for user id=" + userId + " changed to " + newRole + " by admin id=" + requestingUser.getId());
    }

    @Override
    public void deleteUser(int userId, User requestingUser) throws SQLException {
        if (requestingUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only an Admin can delete a user.");
        }
        int rows = userDao.deleteUser(userId);
        if (rows == 0) {
            throw new ResourceNotFoundException("No user found with id " + userId + " to delete.");
        }
        logger.info("User id=" + userId + " deleted by admin id=" + requestingUser.getId());
    }
}
