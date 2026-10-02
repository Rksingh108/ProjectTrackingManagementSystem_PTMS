package com.ptms.app.service;

import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserService {

    User registerUser(User user) throws SQLException;

    User login(String username, String password) throws SQLException;

    User getUserById(int id) throws SQLException;

    List<User> getAllUsers(User requestingUser) throws SQLException;

    List<User> searchUsers(String keyword, User requestingUser) throws SQLException;

    List<User> getUsersByRole(User.Role role, User requestingUser) throws SQLException;

    void updateProfile(User user, User requestingUser) throws SQLException;

    void changeRole(int userId, User.Role role, User requestingUser) throws SQLException;

    void deleteUser(int userId, User requestingUser) throws SQLException;
}