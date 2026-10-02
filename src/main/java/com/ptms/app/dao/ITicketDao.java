package com.ptms.app.dao;

import com.ptms.app.model.Ticket;
import com.ptms.app.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ITicketDao implements TicketDao {

    private static final Logger logger = LoggerFactory.getLogger(ITicketDao.class);

    private static final String INSERT_TICKET = """
            INSERT INTO ticket_management
            (project_id,title,description,priority,deadline,assigned_to,status)
            VALUES (?,?,?,?,?,?,?)
            """;

    private static final String FIND_BY_ID =
            "SELECT * FROM ticket_management WHERE id=?";

    private static final String FIND_BY_PROJECT = """
            SELECT * FROM ticket_management
            WHERE project_id=?
            ORDER BY id
            """;

    private static final String FIND_BY_ASSIGNED_USER = """
            SELECT * FROM ticket_management
            WHERE assigned_to=?
            ORDER BY id
            """;

    private static final String UPDATE_TICKET = """
            UPDATE ticket_management SET
            title=?,
            description=?,
            priority=?,
            deadline=?,
            assigned_to=?,
            status=?
            WHERE id=?
            """;

    private static final String DELETE_TICKET =
            "DELETE FROM ticket_management WHERE id=?";

    @Override
    public int insertTicket(Ticket ticket) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     INSERT_TICKET, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, ticket.getProjectId());
            statement.setString(2, ticket.getTitle());
            statement.setString(3, ticket.getDescription());
            statement.setString(4, ticket.getPriority());

            if (ticket.getDeadline() != null)
                statement.setDate(5, Date.valueOf(ticket.getDeadline()));
            else
                statement.setNull(5, Types.DATE);

            if (ticket.getAssignedTo() != null)
                statement.setInt(6, ticket.getAssignedTo());
            else
                statement.setNull(6, Types.INTEGER);

            statement.setString(7, ticket.getStatus());

            int rows = statement.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (rs.next())
                        ticket.setId(rs.getInt(1));
                }

                logger.info("Ticket created. ID: {}, Project ID: {}",
                        ticket.getId(), ticket.getProjectId());
            } else {
                logger.warn("Ticket creation affected 0 rows. Project ID: {}",
                        ticket.getProjectId());
            }

            return rows;
        } catch (SQLException e) {
            logger.error("Database error while creating ticket. Project ID: {}",
                    ticket.getProjectId(), e);
            throw e;
        }
    }

    @Override
    public Ticket findByTicketId(int id) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Ticket ticket = mapRow(rs);
                    logger.info("Ticket retrieved. ID: {}", id);
                    return ticket;
                }

                logger.warn("Ticket not found. ID: {}", id);
                return null;
            }
        } catch (SQLException e) {
            logger.error("Database error while finding ticket. ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public List<Ticket> findByProjectId(int projectId) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_PROJECT)) {

            statement.setInt(1, projectId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next())
                    tickets.add(mapRow(rs));
            }

            logger.info("Tickets retrieved. Project ID: {}, Count: {}",
                    projectId, tickets.size());

            return tickets;
        } catch (SQLException e) {
            logger.error("Database error while retrieving project tickets. Project ID: {}",
                    projectId, e);
            throw e;
        }
    }

    @Override
    public List<Ticket> findByAssignedTo(int userId) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ASSIGNED_USER)) {

            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next())
                    tickets.add(mapRow(rs));
            }

            logger.info("Assigned tickets retrieved. User ID: {}, Count: {}",
                    userId, tickets.size());

            return tickets;
        } catch (SQLException e) {
            logger.error("Database error while retrieving assigned tickets. User ID: {}",
                    userId, e);
            throw e;
        }
    }

    @Override
    public int updateTicket(Ticket ticket) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_TICKET)) {

            statement.setString(1, ticket.getTitle());
            statement.setString(2, ticket.getDescription());
            statement.setString(3, ticket.getPriority());

            if (ticket.getDeadline() != null)
                statement.setDate(4, Date.valueOf(ticket.getDeadline()));
            else
                statement.setNull(4, Types.DATE);

            if (ticket.getAssignedTo() != null)
                statement.setInt(5, ticket.getAssignedTo());
            else
                statement.setNull(5, Types.INTEGER);

            statement.setString(6, ticket.getStatus());
            statement.setInt(7, ticket.getId());

            int rows = statement.executeUpdate();

            if (rows > 0)
                logger.info("Ticket updated. ID: {}", ticket.getId());
            else
                logger.warn("Ticket update affected 0 rows. ID: {}", ticket.getId());

            return rows;
        } catch (SQLException e) {
            logger.error("Database error while updating ticket. ID: {}",
                    ticket.getId(), e);
            throw e;
        }
    }

    @Override
    public int deleteTicket(int id) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_TICKET)) {

            statement.setInt(1, id);

            int rows = statement.executeUpdate();

            if (rows > 0)
                logger.info("Ticket deleted. ID: {}", id);
            else
                logger.warn("Ticket deletion affected 0 rows. ID: {}", id);

            return rows;
        } catch (SQLException e) {
            logger.error("Database error while deleting ticket. ID: {}", id, e);
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

        Date deadline = rs.getDate("deadline");
        if (deadline != null)
            ticket.setDeadline(deadline.toLocalDate());

        int assignedTo = rs.getInt("assigned_to");
        ticket.setAssignedTo(rs.wasNull() ? null : assignedTo);

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null)
            ticket.setCreatedAt(createdAt.toLocalDateTime());

        ticket.setStatus(rs.getString("status"));

        return ticket;
    }
}