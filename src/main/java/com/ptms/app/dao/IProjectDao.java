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

    private final String insertProject =
            "INSERT INTO projects (name, requirements, manager_id, team_lead_id, client_id, domain, " +
                    "cost, start_date, deadline, priority, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final String findProjectById =
            "SELECT * FROM projects WHERE id = ?";

    private final String findAllProjects =
            "SELECT * FROM projects ORDER BY id";

    private final String findProjectByManagerId =
            "SELECT * FROM projects WHERE manager_id = ? ORDER BY id";

    private final String findProjectByTeamLeadId =
            "SELECT * FROM projects WHERE team_lead_id = ? ORDER BY id";

    private final String findProjectByClientId =
            "SELECT * FROM projects WHERE client_id = ? ORDER BY id";

    private final String updateProject =
            "UPDATE projects SET name = ?, requirements = ?, manager_id = ?, team_lead_id = ?, " +
                    "client_id = ?, domain = ?, cost = ?, start_date = ?, deadline = ?, priority = ?, status = ? WHERE id = ?";

    private final String deleteProject =
            "DELETE FROM projects WHERE id = ?";

    @Override
    public int insert(Project project) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertProject, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, project.getName());
            ps.setString(2, project.getRequirements());
            ps.setInt(3, project.getManagerId());
            ps.setObject(4, project.getTeamLeadId());
            ps.setObject(5, project.getClientId());
            ps.setString(6, project.getDomain());
            ps.setBigDecimal(7, project.getCost());
            ps.setObject(8, project.getStartDate());
            ps.setObject(9, project.getDeadline());
            ps.setString(10, project.getPriority());
            ps.setString(11, project.getStatus());

            int count = ps.executeUpdate();
            if (count > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        project.setId(keys.getInt(1));
                    }
                }
                logger.info("Project inserted successfully, id=" + project.getId());
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to insert project name=" + project.getName() + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Project findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findProjectById)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            logger.severe("Failed to fetch project id=" + id + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Project> findAll() throws SQLException {
        List<Project> projects = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findAllProjects);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                projects.add(mapRow(rs));
            }
            return projects;

        } catch (SQLException e) {
            logger.severe("Failed to fetch all projects : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Project> findByManagerId(int managerId) throws SQLException {
        return findByForeignKey(findProjectByManagerId, managerId, "manager_id");
    }

    @Override
    public List<Project> findByTeamLeadId(int teamLeadId) throws SQLException {
        return findByForeignKey(findProjectByTeamLeadId, teamLeadId, "team_lead_id");
    }

    @Override
    public List<Project> findByClientId(int clientId) throws SQLException {
        return findByForeignKey(findProjectByClientId, clientId, "client_id");
    }

    private List<Project> findByForeignKey(String sql, int value, String columnLabelForLog) throws SQLException {
        List<Project> projects = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapRow(rs));
                }
            }
            return projects;

        } catch (SQLException e) {
            logger.severe("Failed to fetch projects by " + columnLabelForLog + "=" + value + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int update(Project project) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateProject)) {

            ps.setString(1, project.getName());
            ps.setString(2, project.getRequirements());
            ps.setInt(3, project.getManagerId());
            ps.setObject(4, project.getTeamLeadId());
            ps.setObject(5, project.getClientId());
            ps.setString(6, project.getDomain());
            ps.setBigDecimal(7, project.getCost());
            ps.setObject(8, project.getStartDate());
            ps.setObject(9, project.getDeadline());
            ps.setString(10, project.getPriority());
            ps.setString(11, project.getStatus());
            ps.setInt(12, project.getId());

            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("Project updated successfully, id=" + project.getId());
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to update project id=" + project.getId() + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int delete(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteProject)) {

            ps.setInt(1, id);
            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("Project deleted successfully, id=" + id);
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to delete project id=" + id + " : " + e.getMessage());
            throw e;
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
        project.setStartDate(startDate != null ? startDate.toLocalDate() : null);

        java.sql.Date deadline = rs.getDate("deadline");
        project.setDeadline(deadline != null ? deadline.toLocalDate() : null);

        project.setPriority(rs.getString("priority"));
        project.setStatus(rs.getString("status"));
        return project;
    }
}
