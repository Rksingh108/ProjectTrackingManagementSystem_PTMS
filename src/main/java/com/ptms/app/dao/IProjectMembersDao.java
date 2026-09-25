package com.ptms.app.dao;

import com.ptms.app.model.ProjectMember;
import com.ptms.app.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectMemberDAOImpl implements ProjectMemberDAO {

    @Override
    public boolean addMember(ProjectMember member) throws SQLException {
        String sql = "INSERT INTO project_members (project_id, user_id, joined_at, role_in_project) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, member.getProjectId());
            ps.setInt(2, member.getUserId());
            ps.setDate(3, member.getJoinedAt() != null ? Date.valueOf(member.getJoinedAt()) : null);
            ps.setString(4, member.getRoleInProject());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeMember(int projectId, int userId) throws SQLException {
        String sql = "DELETE FROM project_members WHERE project_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projectId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean isMember(int projectId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM project_members WHERE project_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projectId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public List<ProjectMember> findByProject(int projectId) throws SQLException {
        String sql = "SELECT * FROM project_members WHERE project_id = ?";
        List<ProjectMember> members = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(mapRow(rs));
                }
            }
        }
        return members;
    }

    @Override
    public List<ProjectMember> findByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM project_members WHERE user_id = ?";
        List<ProjectMember> members = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(mapRow(rs));
                }
            }
        }
        return members;
    }

    @Override
    public boolean updateRole(int projectId, int userId, String roleInProject) throws SQLException {
        String sql = "UPDATE project_members SET role_in_project = ? WHERE project_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, roleInProject);
            ps.setInt(2, projectId);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private ProjectMember mapRow(ResultSet rs) throws SQLException {
        Date joinedAt = rs.getDate("joined_at");
        return new ProjectMember(
                rs.getInt("project_id"),
                rs.getInt("user_id"),
                joinedAt != null ? joinedAt.toLocalDate() : null,
                rs.getString("role_in_project")
        );
    }
}