package com.ptms.app.dao;

import com.ptms.app.model.Project;
import com.ptms.app.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IProjectDao implements ProjectDao {

    private static final Logger logger = LoggerFactory.getLogger(IProjectDao.class);

    private static final String INSERT_PROJECT = """
            INSERT INTO projects
            (name, requirements, manager_id, team_lead_id, client_id,
             domain, cost, start_date, deadline, priority, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String FIND_BY_ID = "SELECT * FROM projects WHERE id=?";
    private static final String FIND_ALL = "SELECT * FROM projects ORDER BY id";
    private static final String FIND_BY_MANAGER = "SELECT * FROM projects WHERE manager_id=? ORDER BY id";
    private static final String FIND_BY_TEAM_LEAD = "SELECT * FROM projects WHERE team_lead_id=? ORDER BY id";
    private static final String FIND_BY_CLIENT = "SELECT * FROM projects WHERE client_id=? ORDER BY id";

    private static final String UPDATE_PROJECT = """
            UPDATE projects SET
                name=?,
                requirements=?,
                manager_id=?,
                team_lead_id=?,
                client_id=?,
                domain=?,
                cost=?,
                start_date=?,
                deadline=?,
                priority=?,
                status=?
            WHERE id=?
            """;

    private static final String DELETE_PROJECT = "DELETE FROM projects WHERE id=?";

    @Override
    public int insertProject(Project project) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_PROJECT,
                     Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, project.getName());
            statement.setString(2, project.getRequirements());

            if (project.getManagerId() != null) {
                statement.setInt(3, project.getManagerId());
            } else {
                statement.setNull(3, Types.INTEGER);
            }

            if (project.getTeamLeadId() != null) {
                statement.setInt(4, project.getTeamLeadId());
            } else {
                statement.setNull(4, Types.INTEGER);
            }

            if (project.getClientId() != null) {
                statement.setInt(5, project.getClientId());
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            statement.setString(6, project.getDomain());

            if (project.getCost() != null) {
                statement.setBigDecimal(7, project.getCost());
            } else {
                statement.setNull(7, Types.DECIMAL);
            }

            if (project.getStartDate() != null) {
                statement.setDate(8, Date.valueOf(project.getStartDate()));
            } else {
                statement.setNull(8, Types.DATE);
            }

            if (project.getDeadline() != null) {
                statement.setDate(9, Date.valueOf(project.getDeadline()));
            } else {
                statement.setNull(9, Types.DATE);
            }

            statement.setString(10, project.getPriority());
            statement.setString(11, project.getStatus());

            int rows = statement.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (rs.next()) {
                        project.setId(rs.getInt(1));
                    }
                }

                logger.info(
                        "Project created successfully. Project ID: {}",
                        project.getId()
                );
            } else {
                logger.warn("Project insert affected 0 rows.");
            }

            return rows;

        } catch (SQLException e) {
            logger.error("Database error while creating project.", e);
            throw e;
        }
    }

    @Override
    public Project findByProjectId(int id) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {
            logger.error("Database error while finding project ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public List<Project> findAll() throws SQLException {
        List<Project> projects = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                projects.add(mapRow(rs));
            }

            logger.info("Retrieved all projects. Count: {}", projects.size());
            return projects;

        } catch (SQLException e) {
            logger.error("Database error while retrieving all projects.", e);
            throw e;
        }
    }

    @Override
    public List<Project> findByManagerId(int managerId) throws SQLException {
        return findById(FIND_BY_MANAGER, managerId);
    }

    @Override
    public List<Project> findByTeamLeadId(int teamLeadId) throws SQLException {
        return findById(FIND_BY_TEAM_LEAD, teamLeadId);
    }

    @Override
    public List<Project> findByClientId(int clientId) throws SQLException {
        return findById(FIND_BY_CLIENT, clientId);
    }

    private List<Project> findById(String sql, int id) throws SQLException {
        List<Project> projects = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapRow(rs));
                }
            }

            return projects;

        } catch (SQLException e) {
            logger.error(
                    "Database error while retrieving projects for ID: {}",
                    id,
                    e
            );
            throw e;
        }
    }

    @Override
    public int updateProject(Project project) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PROJECT)) {

            statement.setString(1, project.getName());
            statement.setString(2, project.getRequirements());

            if (project.getManagerId() != null) {
                statement.setInt(3, project.getManagerId());
            } else {
                statement.setNull(3, Types.INTEGER);
            }

            if (project.getTeamLeadId() != null) {
                statement.setInt(4, project.getTeamLeadId());
            } else {
                statement.setNull(4, Types.INTEGER);
            }

            if (project.getClientId() != null) {
                statement.setInt(5, project.getClientId());
            } else {
                statement.setNull(5, Types.INTEGER);
            }

            statement.setString(6, project.getDomain());

            if (project.getCost() != null) {
                statement.setBigDecimal(7, project.getCost());
            } else {
                statement.setNull(7, Types.DECIMAL);
            }

            if (project.getStartDate() != null) {
                statement.setDate(8, Date.valueOf(project.getStartDate()));
            } else {
                statement.setNull(8, Types.DATE);
            }

            if (project.getDeadline() != null) {
                statement.setDate(9, Date.valueOf(project.getDeadline()));
            } else {
                statement.setNull(9, Types.DATE);
            }

            statement.setString(10, project.getPriority());
            statement.setString(11, project.getStatus());
            statement.setInt(12, project.getId());

            int rows = statement.executeUpdate();

            logger.info(
                    "Project update completed. Project ID: {}, Rows: {}",
                    project.getId(),
                    rows
            );

            return rows;

        } catch (SQLException e) {
            logger.error(
                    "Database error while updating project ID: {}",
                    project.getId(),
                    e
            );
            throw e;
        }
    }

    @Override
    public int deleteProject(int id) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_PROJECT)) {

            statement.setInt(1, id);

            int rows = statement.executeUpdate();

            logger.info(
                    "Project deletion completed. Project ID: {}, Rows: {}",
                    id,
                    rows
            );

            return rows;

        } catch (SQLException e) {
            logger.error(
                    "Database error while deleting project ID: {}",
                    id,
                    e
            );
            throw e;
        }
    }

    private Project mapRow(ResultSet rs) throws SQLException {
        Project project = new Project();

        project.setId(rs.getInt("id"));
        project.setName(rs.getString("name"));
        project.setRequirements(rs.getString("requirements"));

        int managerId = rs.getInt("manager_id");
        if (rs.wasNull()) {
            project.setManagerId(null);
        } else {
            project.setManagerId(managerId);
        }

        int teamLeadId = rs.getInt("team_lead_id");
        if (rs.wasNull()) {
            project.setTeamLeadId(null);
        } else {
            project.setTeamLeadId(teamLeadId);
        }

        int clientId = rs.getInt("client_id");
        if (rs.wasNull()) {
            project.setClientId(null);
        } else {
            project.setClientId(clientId);
        }

        project.setDomain(rs.getString("domain"));
        project.setCost(rs.getBigDecimal("cost"));

        Date startDate = rs.getDate("start_date");
        if (startDate != null) {
            project.setStartDate(startDate.toLocalDate());
        }

        Date deadline = rs.getDate("deadline");
        if (deadline != null) {
            project.setDeadline(deadline.toLocalDate());
        }

        project.setPriority(rs.getString("priority"));
        project.setStatus(rs.getString("status"));

        return project;
    }
}