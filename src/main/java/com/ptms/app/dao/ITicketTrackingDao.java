package com.ptms.app.dao;

import com.ptms.app.model.TicketTracking;
import com.ptms.app.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ITicketTrackingDao implements TicketTrackingDao {
    private static final Logger logger = LoggerFactory.getLogger(ITicketTrackingDao.class);

    private static final String INSERT = """
        INSERT INTO ticket_tracking
        (ticket_id,status,progress,comment,updated_by)
        VALUES (?,?,?,?,?)
        """;

    private static final String FIND_BY_TICKET =
            "SELECT * FROM ticket_tracking WHERE ticket_id=? ORDER BY id";

    private static final String FIND_BY_USER =
            "SELECT * FROM ticket_tracking WHERE updated_by=? ORDER BY id";

    private static final String UPDATE = """
        UPDATE ticket_tracking
        SET status=?,progress=?,comment=?,updated_by=?
        WHERE id=?
        """;

    @Override
    public int insertTicket(TicketTracking tracking) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tracking.getTicketId());
            ps.setString(2, tracking.getStatus());
            ps.setInt(3, tracking.getProgress());
            ps.setString(4, tracking.getComment());
            ps.setInt(5, tracking.getUpdatedBy());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) tracking.setId(rs.getInt(1));
                }
                logger.info("Ticket tracking created. Ticket ID: {}", tracking.getTicketId());
            }

            return rows;
        } catch (SQLException e) {
            logger.error("Error creating ticket tracking.", e);
            throw e;
        }
    }

    @Override
    public TicketTracking findByTicketId(int ticketId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_TICKET)) {

            ps.setInt(1, ticketId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

            return null;
        } catch (SQLException e) {
            logger.error("Error finding tracking for ticket {}.", ticketId, e);
            throw e;
        }
    }

    @Override
    public List<TicketTracking> findByUserId(int userId) throws SQLException {
        List<TicketTracking> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_USER)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }

            logger.info("Tracking records retrieved for user {}: {}", userId, list.size());
            return list;
        } catch (SQLException e) {
            logger.error("Error finding tracking for user {}.", userId, e);
            throw e;
        }
    }

    @Override
    public int updateTicket(TicketTracking tracking) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {

            ps.setString(1, tracking.getStatus());
            ps.setInt(2, tracking.getProgress());
            ps.setString(3, tracking.getComment());
            ps.setInt(4, tracking.getUpdatedBy());
            ps.setInt(5, tracking.getId());

            int rows = ps.executeUpdate();
            logger.info("Ticket tracking updated. ID: {}", tracking.getId());
            return rows;
        } catch (SQLException e) {
            logger.error("Error updating ticket tracking {}.", tracking.getId(), e);
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
        tracking.setUpdatedBy(rs.getInt("updated_by"));

        Timestamp timestamp = rs.getTimestamp("updated_at");
        if (timestamp != null)
            tracking.setUpdatedAt(timestamp.toLocalDateTime());

        return tracking;
    }
}