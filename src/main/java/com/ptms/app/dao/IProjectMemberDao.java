package com.ptms.app.dao;

import com.ptms.app.model.ProjectMember;
import com.ptms.app.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class IProjectMemberDao implements ProjectMemberDao {

    private static final Logger logger = LoggerFactory.getLogger(IProjectMemberDao.class);

    private static final String INSERT_PROJECT_MEMBER = """
            INSERT INTO project_members
            (project_id, user_id, role_in_project)
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

            int rows = statement.executeUpdate();

            if (rows > 0) {
                logger.info(
                        "Project member added successfully. Project ID: {}, User ID: {}, Role: {}",
                        member.getProjectId(),
                        member.getUserId(),
                        member.getRoleInProject()
                );
            } else {
                logger.warn(
                        "Project member insert affected 0 rows. Project ID: {}, User ID: {}",
                        member.getProjectId(),
                        member.getUserId()
                );
            }

            return rows;

        } catch (SQLException e) {
            logger.error(
                    "Database error while adding project member. Project ID: {}, User ID: {}",
                    member.getProjectId(),
                    member.getUserId(),
                    e
            );
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

            logger.info(
                    "Retrieved project members. Project ID: {}, Result count: {}",
                    projectId,
                    members.size()
            );

        } catch (SQLException e) {
            logger.error(
                    "Database error while retrieving project members. Project ID: {}",
                    projectId,
                    e
            );
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

            logger.info(
                    "Retrieved projects for user. User ID: {}, Result count: {}",
                    userId,
                    members.size()
            );

        } catch (SQLException e) {
            logger.error(
                    "Database error while retrieving projects for user. User ID: {}",
                    userId,
                    e
            );
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
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }

        } catch (SQLException e) {
            logger.error(
                    "Database error while finding project membership. Project ID: {}, User ID: {}",
                    projectId,
                    userId,
                    e
            );
            throw e;
        }

        return null;
    }

    @Override
    public int updateRole(int projectId, int userId, String roleInProject) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ROLE)) {

            statement.setString(1, roleInProject);
            statement.setInt(2, projectId);
            statement.setInt(3, userId);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                logger.info(
                        "Project member role updated. Project ID: {}, User ID: {}, Role: {}",
                        projectId,
                        userId,
                        roleInProject
                );
            } else {
                logger.warn(
                        "Project member role update affected 0 rows. Project ID: {}, User ID: {}",
                        projectId,
                        userId
                );
            }

            return rows;

        } catch (SQLException e) {
            logger.error(
                    "Database error while updating project member role. Project ID: {}, User ID: {}",
                    projectId,
                    userId,
                    e
            );
            throw e;
        }
    }

    @Override
    public int deleteMember(int projectId, int userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_PROJECT_MEMBER)) {

            statement.setInt(1, projectId);
            statement.setInt(2, userId);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                logger.info(
                        "Project member deleted successfully. Project ID: {}, User ID: {}",
                        projectId,
                        userId
                );
            } else {
                logger.warn(
                        "Project member deletion affected 0 rows. Project ID: {}, User ID: {}",
                        projectId,
                        userId
                );
            }

            return rows;

        } catch (SQLException e) {
            logger.error(
                    "Database error while deleting project member. Project ID: {}, User ID: {}",
                    projectId,
                    userId,
                    e
            );
            throw e;
        }
    }

    private ProjectMember mapRow(ResultSet resultSet) throws SQLException {
        ProjectMember member = new ProjectMember();

        member.setProjectId(resultSet.getInt("project_id"));
        member.setUserId(resultSet.getInt("user_id"));
        member.setRoleInProject(resultSet.getString("role_in_project"));

        Timestamp joinedAt = resultSet.getTimestamp("joined_at");

        if (joinedAt != null) {
            member.setJoinedAt(joinedAt.toLocalDateTime());
        } else {
            member.setJoinedAt(null);
        }

        return member;
    }
}