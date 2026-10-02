package com.ptms.app.dao;

import com.ptms.app.model.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDao {
    int insertUser(User user) throws SQLException;
    int updateProfile(User user) throws SQLException;
    int updateRole(int userId, User.Role role) throws SQLException;
    int deleteUser(int id) throws SQLException;
    User findByUserId(int id) throws SQLException;
    User findByUsername(String username) throws SQLException;
    User findByEmail(String email) throws SQLException;
    List<User> findAll() throws SQLException;
    List<User> searchByUserName(String keyword) throws SQLException;
    List<User> findByUserRole(User.Role role) throws SQLException;
}