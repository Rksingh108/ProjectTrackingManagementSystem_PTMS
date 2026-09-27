package com.ptms.app.dao;

import com.ptms.app.model.Ticket;
import com.ptms.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ITicketDao implements TicketDao {

    private static final Logger logger =
            Logger.getLogger(ITicketDao.class.getName());

    private static final String INSERT_TICKET = "INSERT INTO ticket_management " +
                    "(project_id, title, description, priority, deadline, assigned_to, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ID = "SELECT * FROM ticket_management WHERE id = ?";

    private static final String FIND_BY_PROJECT = "SELECT * FROM ticket_management " +
                    "WHERE project_id = ? ORDER BY id";

    private static final String FIND_BY_ASSIGNED_USER = "SELECT * FROM ticket_management " +
                    "WHERE assigned_to = ? ORDER BY id";

    private static final String UPDATE_TICKET = "UPDATE ticket_management SET " +
                    "title = ?, description = ?, priority = ?, deadline = ?, " +
                    "assigned_to = ?, status = ? " +
                    "WHERE id = ?";

    private static final String DELETE_TICKET = "DELETE FROM ticket_management WHERE id = ?";

    @Override
    public int insertTicket(Ticket ticket) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement( INSERT_TICKET,Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, ticket.getProjectId());
            ps.setString(2, ticket.getTitle());
            ps.setString(3, ticket.getDescription());
            ps.setString(4, ticket.getPriority());

            if (ticket.getDeadline() != null) {
                ps.setDate(
                        5,
                        Date.valueOf(ticket.getDeadline())
                );
            } else {
                ps.setNull(5, Types.DATE);
            }

            if (ticket.getAssignedTo() != null) {
                ps.setInt(6, ticket.getAssignedTo());
            } else {
                ps.setNull(6, Types.INTEGER);
            }

            ps.setString(7, ticket.getStatus());

            int rows = ps.executeUpdate();

            if (rows > 0) {

                try (ResultSet rs = ps.getGeneratedKeys()) {

                    if (rs.next()) {
                        ticket.setId(rs.getInt(1));
                    }
                }
            }

            return rows;

        } catch (SQLException e) {

            logger.severe("Failed to insert ticket: " + e.getMessage());

            throw e;
        }
    }

    @Override
    public Ticket findByTicketId(int id) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {

            logger.severe("Failed to find ticket: " + e.getMessage());

            throw e;
        }
    }

    @Override
    public List<Ticket> findByProjectId(int projectId)
            throws SQLException {
        List<Ticket> tickets = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_PROJECT)) {

            ps.setInt(1, projectId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
            }

            return tickets;

        } catch (SQLException e) {

            logger.severe("Failed to find project tickets: " + e.getMessage());

            throw e;
        }
    }

    @Override
    public List<Ticket> findByAssignedTo(int userId)
            throws SQLException {

        List<Ticket> tickets = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_ASSIGNED_USER)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
            }

            return tickets;

        } catch (SQLException e) {

            logger.severe("Failed to find assigned tickets: "+ e.getMessage());

            throw e;
        }
    }

    @Override
    public int updateTicket(Ticket ticket)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_TICKET)) {

            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getPriority());

            if (ticket.getDeadline() != null) {
                ps.setDate(4,Date.valueOf(ticket.getDeadline()));
            } else {ps.setNull(4, Types.DATE);
            }

            if (ticket.getAssignedTo() != null) {
                ps.setInt(5, ticket.getAssignedTo());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setString(6, ticket.getStatus());
            ps.setInt(7, ticket.getId());

            return ps.executeUpdate();

        } catch (SQLException e) {

            logger.severe("Failed to update ticket: "+ e.getMessage());

            throw e;
        }
    }

    @Override
    public int deleteTicket(int id)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE_TICKET)) {

            ps.setInt(1, id);

            return ps.executeUpdate();

        } catch (SQLException e) {

            logger.severe( "Failed to delete ticket: " + e.getMessage());


            throw e;
        }
    }

    private Ticket mapRow(ResultSet rs)
            throws SQLException {

        Ticket ticket = new Ticket();

        ticket.setId(rs.getInt("id"));
        ticket.setProjectId(rs.getInt("project_id"));
        ticket.setTitle(rs.getString("title"));
        ticket.setDescription(rs.getString("description"));
        ticket.setPriority(rs.getString("priority"));

        Date deadline = rs.getDate("deadline");

        if (deadline != null) {
            ticket.setDeadline(deadline.toLocalDate());
        }

        int assignedTo = rs.getInt("assigned_to");

        if (rs.wasNull()) {
            ticket.setAssignedTo(null);
        } else {
            ticket.setAssignedTo(assignedTo);
        }

        Timestamp createdAt =
                rs.getTimestamp("created_at");

        if (createdAt != null) {
            ticket.setCreatedAt(
                    createdAt.toLocalDateTime()
            );
        }

        ticket.setStatus(rs.getString("status"));

        return ticket;
    }
}