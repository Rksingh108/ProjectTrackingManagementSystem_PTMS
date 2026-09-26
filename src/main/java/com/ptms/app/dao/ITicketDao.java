package com.ptms.app.dao;

import com.ptms.app.model.Ticket;
import com.ptms.app.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ITicketDao implements TicketDao {

    private static final Logger logger = Logger.getLogger(ITicketDao.class.getName());

    private final String insertTicket =
            "INSERT INTO ticket_management (project_id, title, description, priority, deadline, " +
                    "assigned_to, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private final String findTicketById =
            "SELECT * FROM ticket_management WHERE id = ?";

    private final String findTicketsByProjectId =
            "SELECT * FROM ticket_management WHERE project_id = ? ORDER BY id";

    private final String findTicketsByAssignedTo =
            "SELECT * FROM ticket_management WHERE assigned_to = ? ORDER BY id";

    private final String updateTicket =
            "UPDATE ticket_management SET title = ?, description = ?, priority = ?, deadline = ?, " +
                    "assigned_to = ?, status = ? WHERE id = ?";

    private final String deleteTicket =
            "DELETE FROM ticket_management WHERE id = ?";

    @Override
    public int insert(Ticket ticket) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertTicket, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, ticket.getProjectId());
            ps.setString(2, ticket.getTitle());
            ps.setString(3, ticket.getDescription());
            ps.setString(4, ticket.getPriority());
            ps.setObject(5, ticket.getDeadline());
            ps.setObject(6, ticket.getAssignedTo());
            ps.setString(7, ticket.getStatus());

            int count = ps.executeUpdate();
            if (count > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        ticket.setId(keys.getInt(1));
                    }
                }
                logger.info("Ticket inserted successfully, id=" + ticket.getId());
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to insert ticket title=" + ticket.getTitle() + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Ticket findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findTicketById)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            logger.severe("Failed to fetch ticket id=" + id + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Ticket> findByProjectId(int projectId) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findTicketsByProjectId)) {

            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
            }
            return tickets;

        } catch (SQLException e) {
            logger.severe("Failed to fetch tickets for projectId=" + projectId + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Ticket> findByAssignedTo(int userId) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findTicketsByAssignedTo)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
            }
            return tickets;

        } catch (SQLException e) {
            logger.severe("Failed to fetch tickets assigned to userId=" + userId + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int update(Ticket ticket) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateTicket)) {

            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getPriority());
            ps.setObject(4, ticket.getDeadline());
            ps.setObject(5, ticket.getAssignedTo());
            ps.setString(6, ticket.getStatus());
            ps.setInt(7, ticket.getId());

            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("Ticket updated successfully, id=" + ticket.getId());
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to update ticket id=" + ticket.getId() + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int delete(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteTicket)) {

            ps.setInt(1, id);
            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("Ticket deleted successfully, id=" + id);
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to delete ticket id=" + id + " : " + e.getMessage());
            throw e;
        }
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getInt("id"));
        ticket.setProjectId(rs.getInt("project_id"));
        ticket.setTitle(rs.getString("title"));
        ticket.setDescription(rs.getString("description"));
        ticket.setPriority(rs.getString("priority"));

        java.sql.Date deadline = rs.getDate("deadline");
        ticket.setDeadline(deadline != null ? deadline.toLocalDate() : null);

        int assignedTo = rs.getInt("assigned_to");
        ticket.setAssignedTo(rs.wasNull() ? null : assignedTo);

        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        ticket.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        ticket.setStatus(rs.getString("status"));
        return ticket;
    }
}
