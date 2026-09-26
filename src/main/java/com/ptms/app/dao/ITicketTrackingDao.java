package com.ptms.app.dao;
import com.ptms.app.model.TicketTracking;
import com.ptms.app.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
public class ITicketTrackingDao implements TicketTrackingDao {
    private static final Logger logger = Logger.getLogger(ITicketTrackingDao.class.getName());
    private final String insertTicketTracking =
            "INSERT INTO ticket_tracking (ticket_id, status, progress, comment, updated_by) VALUES (?, ?, ?, ?, ?)";
    private final String findTrackingByTicketId =
            "SELECT * FROM ticket_tracking WHERE ticket_id = ?";
    private final String findTrackingByUpdatedBy =
            "SELECT * FROM ticket_tracking WHERE updated_by = ? ORDER BY updated_at DESC";
    // ticket_id is UNIQUE (one tracking row per ticket), so it's the natural
    // key callers already have on hand — used here instead of the surrogate id.
    private final String updateTicketTracking =
            "UPDATE ticket_tracking SET status = ?, progress = ?, comment = ?, updated_by = ? WHERE ticket_id = ?";
    private final String deleteTicketTracking =
            "DELETE FROM ticket_tracking WHERE ticket_id = ?";
    @Override
    public int insert(TicketTracking tracking) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertTicketTracking, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tracking.getTicketId());
            ps.setString(2, tracking.getStatus());
            ps.setInt(3, tracking.getProgress());
            ps.setString(4, tracking.getComment());
            ps.setObject(5, tracking.getUpdatedBy());
            int count = ps.executeUpdate();
            if (count > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        tracking.setId(keys.getInt(1));
                    }
                }
                logger.info("Ticket tracking inserted successfully, ticketId=" + tracking.getTicketId());
            }
            return count;
        } catch (SQLException e) {
            logger.severe("Failed to insert ticket tracking ticketId=" + tracking.getTicketId() + " : " + e.getMessage());
            throw e;
        }
    }
    @Override
    public TicketTracking findByTicketId(int ticketId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findTrackingByTicketId)) {
            ps.setInt(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException e) {
            logger.severe("Failed to fetch ticket tracking for ticketId=" + ticketId + " : " + e.getMessage());
            throw e;
        }
    }
    @Override
    public List<TicketTracking> findByUpdatedBy(int userId) throws SQLException {
        List<TicketTracking> trackings = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findTrackingByUpdatedBy)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trackings.add(mapRow(rs));
                }
            }
            return trackings;
        } catch (SQLException e) {
            logger.severe("Failed to fetch ticket tracking updates by userId=" + userId + " : " + e.getMessage());
            throw e;
        }
    }
    @Override
    public int update(TicketTracking tracking) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateTicketTracking)) {
            ps.setString(1, tracking.getStatus());
            ps.setInt(2, tracking.getProgress());
            ps.setString(3, tracking.getComment());
            ps.setObject(4, tracking.getUpdatedBy());
            ps.setInt(5, tracking.getTicketId());
            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("Ticket tracking updated successfully, ticketId=" + tracking.getTicketId());
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to update ticket tracking ticketId=" + tracking.getTicketId() + " : " + e.getMessage());
            throw e;
        }
    }
    @Override
    public int delete(int ticketId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteTicketTracking)) {

            ps.setInt(1, ticketId);
            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("Ticket tracking deleted successfully, ticketId=" + ticketId);
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to delete ticket tracking ticketId=" + ticketId + " : " + e.getMessage());
            throw e;
        }
    }
    private TicketTracking mapRow(ResultSet rs) throws SQLException {
        TicketTracking tracking = new TicketTracking();
        tracking.setId(rs.getInt("id"));
        tracking.setTicketId(rs.getInt("ticket_id"));
        tracking.setStatus(rs.getString("status"));
        tracking.setProgress(rs.getInt("progress"));
        tracking.setComment(rs.getString("comment"));
        int updatedBy = rs.getInt("updated_by");
        tracking.setUpdatedBy(rs.wasNull() ? null : updatedBy);
        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        tracking.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
        return tracking;
    }
}
