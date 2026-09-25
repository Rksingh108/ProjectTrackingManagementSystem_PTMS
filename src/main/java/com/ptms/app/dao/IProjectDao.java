package com.ptms.app.dao;

import com.ptms.app.model.Projects;
import com.ptms.app.util.DataBaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IProjectDao implements ProjectDao {

    private final String insertProject = "INSERT INTO projects (name, requirements, manager_id, team_lead_id, client_id, " +
            "domain, cost, start_date, deadline, priority, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final String findProjectById = "SELECT * FROM projects WHERE id = ?";
    private final String projectStatus = "SELECT * FROM projects WHERE status = ? ORDER BY id";
    private final String searchById = "SELECT * FROM projects WHERE name LIKE ? ORDER BY id";
    private final String updateProject = "UPDATE projects SET name = ?, requirements = ?, client_id = ?, domain = ?, " +
            "cost = ?, start_date = ?, deadline = ?, priority = ?, status = ? WHERE id = ?";

    private final String projectTeamLead = "UPDATE projects SET team_lead_id = ? WHERE id = ?";
    private final String projectUpdateStatus = "UPDATE projects SET status = ? WHERE id = ?";
    private final String deleteProject = "DELETE FROM projects WHERE id = ?";

    @Override
    public int insert(Projects project) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertProject, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, project.getName());
            ps.setString(2, project.getRequirements());
            ps.setInt(3, project.getManagerId());
            setNullableInt(ps, 4, project.getTeamLeadId());
            setNullableInt(ps, 5, project.getClientId());
            ps.setString(6, project.getDomain());
            ps.setBigDecimal(7, project.getCost());
            ps.setDate(8, project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null);
            ps.setDate(9, project.getDeadline() != null ? Date.valueOf(project.getDeadline()) : null);
            ps.setString(10, project.getPriority());
            ps.setString(11, project.getStatus());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    @Override
    public Projects findById(int id) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findProjectById)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Projects> findAll() throws SQLException {
        return runListQuery("SELECT * FROM projects ORDER BY id", null);
    }

    @Override
    public List<Projects> findByManager(int managerId) throws SQLException {
        return runListQuery("SELECT * FROM projects WHERE manager_id = ? ORDER BY id", managerId);
    }

    @Override
    public List<Projects> findByTeamLead(int teamLeadId) throws SQLException {
        return runListQuery("SELECT * FROM projects WHERE team_lead_id = ? ORDER BY id", teamLeadId);
    }

    @Override
    public List<Projects> findByClient(int clientId) throws SQLException {
        return runListQuery("SELECT * FROM projects WHERE client_id = ? ORDER BY id", clientId);
    }

    @Override
    public List<Projects> findByStatus(String status) throws SQLException {

        List<Projects> projects = new ArrayList<>();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(projectStatus)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapRow(rs));
                }
            }
        }
        return projects;
    }

    @Override
    public List<Projects> searchByName(String keyword) throws SQLException {

        List<Projects> projects = new ArrayList<>();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(searchById)) {

            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapRow(rs));
                }
            }
        }
        return projects;
    }

    @Override
    public boolean update(Projects project) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateProject)) {

            ps.setString(1, project.getName());
            ps.setString(2, project.getRequirements());
            setNullableInt(ps, 3, project.getClientId());
            ps.setString(4, project.getDomain());
            ps.setBigDecimal(5, project.getCost());
            ps.setDate(6, project.getStartDate() != null ? Date.valueOf(project.getStartDate()) : null);
            ps.setDate(7, project.getDeadline() != null ? Date.valueOf(project.getDeadline()) : null);
            ps.setString(8, project.getPriority());
            ps.setString(9, project.getStatus());
            ps.setInt(10, project.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean assignTeamLead(int projectId, int teamLeadId) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(projectTeamLead)) {

            ps.setInt(1, teamLeadId);
            ps.setInt(2, projectId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int projectId, String status) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(projectUpdateStatus)) {

            ps.setString(1, status);
            ps.setInt(2, projectId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteProject)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Projects> runListQuery(String sql, Integer param) throws SQLException {
        List<Projects> projects = new ArrayList<>();
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (param != null) {
                ps.setInt(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    projects.add(mapRow(rs));
                }
            }
        }
        return projects;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    private Projects mapRow(ResultSet rs) throws SQLException {
        Date startDate = rs.getDate("start_date");
        Date deadline = rs.getDate("deadline");

        int teamLeadIdRaw = rs.getInt("team_lead_id");
        Integer teamLeadId = rs.wasNull() ? null : teamLeadIdRaw;

        int clientIdRaw = rs.getInt("client_id");
        Integer clientId = rs.wasNull() ? null : clientIdRaw;

        return new Projects(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("requirements"),
                rs.getInt("manager_id"),
                teamLeadId,
                clientId,
                rs.getString("domain"),
                rs.getBigDecimal("cost"),
                startDate != null ? startDate.toLocalDate() : null,
                deadline != null ? deadline.toLocalDate() : null,
                rs.getString("priority"),
                rs.getString("status")
        );
    }
}