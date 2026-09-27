package com.ptms.app.dao;

import com.ptms.app.model.Project;
import com.ptms.app.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class IProjectDao implements ProjectDao {

    private static final Logger logger = Logger.getLogger(IProjectDao.class.getName());

    private static final String INSERT_PROJECT = """
            INSERT INTO projects (
                name, requirements, manager_id, team_lead_id, client_id,
                domain, cost, start_date, deadline, priority, status
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String FIND_BY_ID = "SELECT * FROM projects WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM projects ORDER BY id";
    private static final String FIND_BY_MANAGER = "SELECT * FROM projects WHERE manager_id = ? ORDER BY id";
    private static final String FIND_BY_TEAM_LEAD = "SELECT * FROM projects WHERE team_lead_id = ? ORDER BY id";
    private static final String FIND_BY_CLIENT = "SELECT * FROM projects WHERE client_id = ? ORDER BY id";

    private static final String UPDATE_PROJECT = """
            UPDATE projects SET
                name = ?,
                requirements = ?,
                manager_id = ?,
                team_lead_id = ?,
                client_id = ?,
                domain = ?,
                cost = ?,
                start_date = ?,
                deadline = ?,
                priority = ?,
                status = ?
            WHERE id = ?
            """;

    private static final String DELETE_PROJECT = "DELETE FROM projects WHERE id = ?";

    @Override
    public int insertProject(Project project) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_PROJECT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, project.getName());
            statement.setString(2, project.getRequirements());
            statement.setInt(3, project.getManagerId());
            statement.setObject(4, project.getTeamLeadId());
            statement.setObject(5, project.getClientId());
            statement.setString(6, project.getDomain());
            statement.setBigDecimal(7, project.getCost());
            statement.setObject(8, project.getStartDate());
            statement.setObject(9, project.getDeadline());
            statement.setString(10, project.getPriority());
            statement.setString(11, project.getStatus());

            int rows = statement.executeUpdate();

            if (rows > 0) {
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        project.setId(keys.getInt(1));
                    }
                }

                logger.info("Project created. ID: " + project.getId());
            }

            return rows;
        }
    }

    @Override
    public Project findByProjectId(int id) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        }
    }

    @Override
    public List<Project> findAll() throws SQLException {
        List<Project> projects = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                projects.add(mapRow(resultSet));
            }
        }

        return projects;
    }

    @Override
    public List<Project> findByManagerId(int managerId) throws SQLException {
        return findByForeignKey(FIND_BY_MANAGER, managerId);
    }

    @Override
    public List<Project> findByTeamLeadId(int teamLeadId) throws SQLException {
        return findByForeignKey(FIND_BY_TEAM_LEAD, teamLeadId);
    }

    @Override
    public List<Project> findByClientId(int clientId) throws SQLException {
        return findByForeignKey(FIND_BY_CLIENT, clientId);
    }

    private List<Project> findByForeignKey(String sql, int value) throws SQLException {
        List<Project> projects = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, value);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    projects.add(mapRow(resultSet));
                }
            }
        }

        return projects;
    }

    @Override
    public int updateProject(Project project) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PROJECT)) {

            statement.setString(1, project.getName());
            statement.setString(2, project.getRequirements());
            statement.setInt(3, project.getManagerId());
            statement.setObject(4, project.getTeamLeadId());
            statement.setObject(5, project.getClientId());
            statement.setString(6, project.getDomain());
            statement.setBigDecimal(7, project.getCost());
            statement.setObject(8, project.getStartDate());
            statement.setObject(9, project.getDeadline());
            statement.setString(10, project.getPriority());
            statement.setString(11, project.getStatus());
            statement.setInt(12, project.getId());

            return statement.executeUpdate();
        }
    }

    @Override
    public int deleteProject(int id) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_PROJECT)) {

            statement.setInt(1, id);
            return statement.executeUpdate();
        }
    }

    private Project mapRow(ResultSet rs) throws SQLException {
        Project project = new Project();

        project.setId(rs.getInt("id"));
        project.setName(rs.getString("name"));
        project.setRequirements(rs.getString("requirements"));
        project.setManagerId(rs.getInt("manager_id"));

        int teamLeadId = rs.getInt("team_lead_id");
        project.setTeamLeadId(rs.wasNull() ? null : teamLeadId);

        int clientId = rs.getInt("client_id");
        project.setClientId(rs.wasNull() ? null : clientId);

        project.setDomain(rs.getString("domain"));
        project.setCost(rs.getBigDecimal("cost"));

        java.sql.Date startDate = rs.getDate("start_date");
        project.setStartDate(startDate == null ? null : startDate.toLocalDate());

        java.sql.Date deadline = rs.getDate("deadline");
        project.setDeadline(deadline == null ? null : deadline.toLocalDate());

        project.setPriority(rs.getString("priority"));
        project.setStatus(rs.getString("status"));

        return project;
    }
}