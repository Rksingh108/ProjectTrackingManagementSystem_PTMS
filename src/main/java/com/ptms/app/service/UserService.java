package com.ptms.app.service;

import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserService {

    User registerUser(User newUser) throws SQLException;

    User login(String username, String password) throws SQLException;

    User getUserById(int id) throws SQLException;

    List<User> getAllUsers() throws SQLException;

    List<User> searchUsers(String keyword) throws SQLException;

    List<User> getUsersByRole(User.Role role) throws SQLException;

    void updateUser(User user) throws SQLException;

    void changeRole(int userId, User.Role newRole, User requestingUser) throws SQLException;

    void deleteUser(int userId, User requestingUser) throws SQLException;
}
