package com.ptms.app.dao;

import com.ptms.app.model.TicketManagement;
import com.ptms.app.util.DataBaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ITicketManagementDao implements TicketManagementDao {
    private final String insertTicket = "INSERT INTO ticket_management (project_id, title, description, priority, " +
            "deadline, assigned_to, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private final String findTicket = "SELECT * FROM ticket_management WHERE id = ?";
    private final String updateTicket = "UPDATE ticket_management SET title = ?, description = ?, priority = ?, " +
            "deadline = ?, assigned_to = ?, status = ? WHERE id = ?";
    private final String ticketStatus = "UPDATE ticket_management SET status = ? WHERE id = ?";
    private final String deleteTicket = "DELETE FROM ticket_management WHERE id = ?";


    @Override
    public int insert(TicketManagement ticket) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertTicket, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, ticket.getProjectId());
            ps.setString(2, ticket.getTitle());
            ps.setString(3, ticket.getDescription());
            ps.setString(4, ticket.getPriority());
            ps.setDate(5, ticket.getDeadline() != null ? Date.valueOf(ticket.getDeadline()) : null);
            setNullableInt(ps, 6, ticket.getAssignedTo());
            ps.setString(7, ticket.getStatus());

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
    public TicketManagement findById(int id) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findTicket)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<TicketManagement> findAll() throws SQLException {
        return runQuery("SELECT * FROM ticket_management ORDER BY id", ps -> {});
    }

    @Override
    public List<TicketManagement> findByProject(int projectId) throws SQLException {
        return runQuery("SELECT * FROM ticket_management WHERE project_id = ? ORDER BY id",
                ps -> ps.setInt(1, projectId));
    }

    @Override
    public List<TicketManagement> findByAssignedTo(int userId) throws SQLException {
        return runQuery("SELECT * FROM ticket_management WHERE assigned_to = ? ORDER BY id",
                ps -> ps.setInt(1, userId));
    }

    @Override
    public List<TicketManagement> findByStatus(String status) throws SQLException {
        return runQuery("SELECT * FROM ticket_management WHERE status = ? ORDER BY id",
                ps -> ps.setString(1, status));
    }

    @Override
    public List<TicketManagement> findByPriority(String priority) throws SQLException {
        return runQuery("SELECT * FROM ticket_management WHERE priority = ? ORDER BY id",
                ps -> ps.setString(1, priority));
    }

    @Override
    public List<TicketManagement> filter(int projectId, String status, String priority, Integer assignedTo)
            throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT * FROM ticket_management WHERE project_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(projectId);

        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (priority != null) {
            sql.append(" AND priority = ?");
            params.add(priority);
        }
        if (assignedTo != null) {
            sql.append(" AND assigned_to = ?");
            params.add(assignedTo);
        }
        sql.append(" ORDER BY id");

        List<TicketManagement> tickets = new ArrayList<>();
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
            }
        }
        return tickets;
    }

    @Override
    public boolean update(TicketManagement ticket) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateTicket)) {

            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getPriority());
            ps.setDate(4, ticket.getDeadline() != null ? Date.valueOf(ticket.getDeadline()) : null);
            setNullableInt(ps, 5, ticket.getAssignedTo());
            ps.setString(6, ticket.getStatus());
            ps.setInt(7, ticket.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int ticketId, String status) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(ticketStatus)) {

            ps.setString(1, status);
            ps.setInt(2, ticketId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteTicket)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @FunctionalInterface
    private interface ParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private List<TicketManagement> runQuery(String sql, ParamSetter setter) throws SQLException {
        List<TicketManagement> tickets = new ArrayList<>();
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
            }
        }
        return tickets;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value != null) {
            ps.setInt(index, value);
        } else {
            ps.setNull(index, Types.INTEGER);
        }
    }

    private TicketManagement mapRow(ResultSet rs) throws SQLException {
        Date deadline = rs.getDate("deadline");
        Timestamp createdAt = rs.getTimestamp("created_at");

        int assignedToRaw = rs.getInt("assigned_to");
        Integer assignedTo = rs.wasNull() ? null : assignedToRaw;

        return new TicketManagement(
                rs.getInt("id"),
                rs.getInt("project_id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("priority"),
                deadline != null ? deadline.toLocalDate() : null,
                assignedTo,
                createdAt != null ? createdAt.toLocalDateTime() : null,
                rs.getString("status")
        );
    }
}