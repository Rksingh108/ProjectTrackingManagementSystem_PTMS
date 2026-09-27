package com.ptms.app.dao;

import com.ptms.app.model.TicketTracking;
import com.ptms.app.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ITicketTrackingDao implements TicketTrackingDao {

    private static final Logger logger =
            Logger.getLogger(ITicketTrackingDao.class.getName());

    private static final String INSERT_TRACKING = "INSERT INTO ticket_tracking " +
            "(ticket_id, status, progress, comment, updated_by) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String FIND_BY_TICKET_ID = "SELECT * FROM ticket_tracking " +
            "WHERE ticket_id = ?";

    private static final String FIND_BY_UPDATED_BY = "SELECT * FROM ticket_tracking " +
            "WHERE updated_by = ? ORDER BY updated_at DESC";

    private static final String UPDATE_TRACKING = "UPDATE ticket_tracking SET " +
            "status = ?, progress = ?, comment = ?, updated_by = ? " +
            "WHERE ticket_id = ?";

    private static final String DELETE_TRACKING = "DELETE FROM ticket_tracking " +
            "WHERE ticket_id = ?";

    @Override
    public int insertTicket(TicketTracking tracking)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     INSERT_TRACKING,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tracking.getTicketId());
            ps.setString(2, tracking.getStatus());
            ps.setInt(3, tracking.getProgress());
            ps.setString(4, tracking.getComment());

            if (tracking.getUpdatedBy() != null) {
                ps.setInt(5, tracking.getUpdatedBy());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            int rows = ps.executeUpdate();

            if (rows > 0) {

                try (ResultSet rs = ps.getGeneratedKeys()) {

                    if (rs.next()) {
                        tracking.setId(rs.getInt(1));
                    }
                }
            }

            return rows;

        } catch (SQLException e) {

            logger.severe("Failed to insert ticket tracking: " + e.getMessage());

            throw e;
        }
    }

    @Override
    public TicketTracking findByTicketId(int ticketId)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_TICKET_ID)) {

            ps.setInt(1, ticketId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapRow(rs);
                }

                return null;
            }

        } catch (SQLException e) {

            logger.severe("Failed to find tracking for ticket "
                    + ticketId + ": " + e.getMessage());

            throw e;
        }
    }

    @Override
    public List<TicketTracking> findByUpdatedBy(int userId)
            throws SQLException {

        List<TicketTracking> trackings = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_UPDATED_BY)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    trackings.add(mapRow(rs));
                }
            }

            return trackings;

        } catch (SQLException e) {

            logger.severe("Failed to find tracking updates for user "
                    + userId + ": " + e.getMessage());

            throw e;
        }
    }

    @Override
    public int updateTicket(TicketTracking tracking)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_TRACKING)) {

            ps.setString(1, tracking.getStatus());
            ps.setInt(2, tracking.getProgress());
            ps.setString(3, tracking.getComment());

            if (tracking.getUpdatedBy() != null) {
                ps.setInt(4, tracking.getUpdatedBy());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setInt(5, tracking.getTicketId());

            return ps.executeUpdate();

        } catch (SQLException e) {

            logger.severe("Failed to update tracking for ticket "
                    + tracking.getTicketId() + ": " + e.getMessage());

            throw e;
        }
    }

    @Override
    public int deleteTicket(int ticketId)
            throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE_TRACKING)) {

            ps.setInt(1, ticketId);

            return ps.executeUpdate();

        } catch (SQLException e) {

            logger.severe("Failed to delete tracking for ticket "
                    + ticketId + ": " + e.getMessage());

            throw e;
        }
    }

    private TicketTracking mapRow(ResultSet rs)
            throws SQLException {

        TicketTracking tracking = new TicketTracking();

        tracking.setId(rs.getInt("id"));
        tracking.setTicketId(rs.getInt("ticket_id"));
        tracking.setStatus(rs.getString("status"));
        tracking.setProgress(rs.getInt("progress"));
        tracking.setComment(rs.getString("comment"));

        int updatedBy = rs.getInt("updated_by");

        if (rs.wasNull()) {
            tracking.setUpdatedBy(null);
        } else {
            tracking.setUpdatedBy(updatedBy);
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");

        if (updatedAt != null) {
            tracking.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return tracking;
    }
}