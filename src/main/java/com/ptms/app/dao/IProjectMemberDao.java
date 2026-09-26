package com.ptms.app.dao;

import com.ptms.app.model.ProjectMember;
import com.ptms.app.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class IProjectMemberDao implements ProjectMemberDao {

    private static final Logger logger = Logger.getLogger(IProjectMemberDao.class.getName());

    private final String insertProjectMember =
            "INSERT INTO project_members (project_id, user_id, role_in_project) VALUES (?, ?, ?)";

    private final String findMembersByProjectId =
            "SELECT * FROM project_members WHERE project_id = ? ORDER BY joined_at";

    private final String findMembersByUserId =
            "SELECT * FROM project_members WHERE user_id = ? ORDER BY joined_at";

    private final String updateProjectMemberRole =
            "UPDATE project_members SET role_in_project = ? WHERE project_id = ? AND user_id = ?";

    private final String deleteProjectMember =
            "DELETE FROM project_members WHERE project_id = ? AND user_id = ?";

    @Override
    public int insert(ProjectMember member) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertProjectMember)) {

            ps.setInt(1, member.getProjectId());
            ps.setInt(2, member.getUserId());
            ps.setString(3, member.getRoleInProject());

            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("Project member added successfully, projectId=" + member.getProjectId()
                        + " userId=" + member.getUserId());
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to add project member projectId=" + member.getProjectId()
                    + " userId=" + member.getUserId() + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<ProjectMember> findByProjectId(int projectId) throws SQLException {
        List<ProjectMember> members = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findMembersByProjectId)) {

            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(mapRow(rs));
                }
            }
            return members;

        } catch (SQLException e) {
            logger.severe("Failed to fetch members for projectId=" + projectId + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<ProjectMember> findByUserId(int userId) throws SQLException {
        List<ProjectMember> members = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findMembersByUserId)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(mapRow(rs));
                }
            }
            return members;

        } catch (SQLException e) {
            logger.severe("Failed to fetch project memberships for userId=" + userId + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int updateRole(int projectId, int userId, String roleInProject) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateProjectMemberRole)) {

            ps.setString(1, roleInProject);
            ps.setInt(2, projectId);
            ps.setInt(3, userId);

            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("Project member role updated, projectId=" + projectId + " userId=" + userId);
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to update role projectId=" + projectId + " userId=" + userId + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int delete(int projectId, int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteProjectMember)) {

            ps.setInt(1, projectId);
            ps.setInt(2, userId);

            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("Project member removed, projectId=" + projectId + " userId=" + userId);
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to remove project member projectId=" + projectId + " userId=" + userId + " : " + e.getMessage());
            throw e;
        }
    }

    private ProjectMember mapRow(ResultSet rs) throws SQLException {
        ProjectMember member = new ProjectMember();
        member.setProjectId(rs.getInt("project_id"));
        member.setUserId(rs.getInt("user_id"));
        member.setRoleInProject(rs.getString("role_in_project"));

        java.sql.Timestamp joinedAt = rs.getTimestamp("joined_at");
        member.setJoinedAt(joinedAt != null ? joinedAt.toLocalDateTime() : null);

        return member;
    }
}
