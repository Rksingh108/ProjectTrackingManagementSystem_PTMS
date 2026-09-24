package com.ptms.app.dao;

import com.ptms.app.model.Roles;
import com.ptms.app.util.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RolesDao implements IRolesDao {

    @Override
    public void addRole(Roles role) {

        String sql = " INSERT INTO roles (role_name) VALUES (?) ";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

//            pstmt.setString(1, role.getRoleName());
            pstmt.setString(1, role.getName());

            pstmt.executeUpdate();

            System.out.println("Role inserted successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert role", e);
        }
    }

}