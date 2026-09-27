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

    private static final String INSERT_PROJECT_MEMBER = """
            INSERT INTO project_members (project_id, user_id, role_in_project)
            VALUES (?, ?, ?)
            """;

    private static final String FIND_BY_PROJECT_ID = """
            SELECT project_id, user_id, role_in_project, joined_at
            FROM project_members
            WHERE project_id = ?
            ORDER BY joined_at
            """;

    private static final String FIND_BY_USER_ID = """
            SELECT project_id, user_id, role_in_project, joined_at
            FROM project_members
            WHERE user_id = ?
            ORDER BY joined_at
            """;

    private static final String FIND_MEMBERSHIP = """
            SELECT project_id, user_id, role_in_project, joined_at
            FROM project_members
            WHERE project_id = ? AND user_id = ?
            """;

    private static final String UPDATE_ROLE = """
            UPDATE project_members
            SET role_in_project = ?
            WHERE project_id = ? AND user_id = ?
            """;

    private static final String DELETE_PROJECT_MEMBER = """
            DELETE FROM project_members
            WHERE project_id = ? AND user_id = ?
            """;

    @Override
    public int insertMember(ProjectMember member) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_PROJECT_MEMBER)) {

            statement.setInt(1, member.getProjectId());
            statement.setInt(2, member.getUserId());
            statement.setString(3, member.getRoleInProject());

            return statement.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to add project member: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<ProjectMember> findByProjectId(int projectId) throws SQLException {
        List<ProjectMember> members = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_PROJECT_ID)) {

            statement.setInt(1, projectId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    members.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to fetch project members: " + e.getMessage());
            throw e;
        }

        return members;
    }

    @Override
    public List<ProjectMember> findByUserId(int userId) throws SQLException {
        List<ProjectMember> members = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USER_ID)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    members.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to fetch user memberships: " + e.getMessage());
            throw e;
        }

        return members;
    }

    @Override
    public ProjectMember findMembership(int projectId, int userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_MEMBERSHIP)) {

            statement.setInt(1, projectId);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        } catch (SQLException e) {
            logger.severe("Failed to check project membership: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int updateRole(int projectId, int userId, String roleInProject) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ROLE)) {

            statement.setString(1, roleInProject);
            statement.setInt(2, projectId);
            statement.setInt(3, userId);

            return statement.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to update project member role: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int deleteMember(int projectId, int userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_PROJECT_MEMBER)) {

            statement.setInt(1, projectId);
            statement.setInt(2, userId);

            return statement.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Failed to delete project member: " + e.getMessage());
            throw e;
        }
    }

    private ProjectMember mapRow(ResultSet resultSet) throws SQLException {
        ProjectMember member = new ProjectMember();

        member.setProjectId(resultSet.getInt("project_id"));
        member.setUserId(resultSet.getInt("user_id"));
        member.setRoleInProject(resultSet.getString("role_in_project"));

        java.sql.Timestamp joinedAt = resultSet.getTimestamp("joined_at");
        member.setJoinedAt(joinedAt != null ? joinedAt.toLocalDateTime() : null);

        return member;
    }
}