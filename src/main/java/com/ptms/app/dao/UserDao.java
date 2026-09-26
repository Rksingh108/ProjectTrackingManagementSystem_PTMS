package com.ptms.app.dao;

import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDao {

    int insert(User user) throws SQLException;

    User findById(int id) throws SQLException;

    User findByUsername(String username) throws SQLException;

    List<User> findAll() throws SQLException;

    List<User> searchByName(String keyword) throws SQLException;

    List<User> findByRole(User.Role role) throws SQLException;

    int update(User user) throws SQLException;

    int delete(int id) throws SQLException;
}
