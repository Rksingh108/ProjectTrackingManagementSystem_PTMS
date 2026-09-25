package com.ptms.app.dao;

import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDao {

    int insert(User user) throws SQLException;

    User findById(int id) throws SQLException;

    User findByUsername(String username) throws SQLException;

    List<User> findAll() throws SQLException;

    List<User> findByRole(String roleName) throws SQLException;

    List<User> searchByName(String keyword) throws SQLException;

    boolean update(User user) throws SQLException;

    boolean delete(int id) throws SQLException;

    /**
     * Validates login credentials.
     * Returns the matching User if the username exists and the
     * (already-hashed) password matches, otherwise null.
     */
    User validateLogin(String username, String hashedPassword) throws SQLException;
}