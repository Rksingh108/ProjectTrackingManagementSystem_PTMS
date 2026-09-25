package com.ptms.app.dao;

import com.ptms.app.model.TicketTracking;
import com.ptms.app.util.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ITicketTrackingDao implements TicketTrackingDao {
    private final String insertTicket = "INSERT INTO ticket_tracking (ticket_id, status, progress, comment, updated_by) " +
            "VALUES (?, ?, ?, ?, ?)";
    private final String findTicketById = "SELECT * FROM ticket_tracking WHERE id = ?";
    private final String findByTicket = "SELECT * FROM ticket_tracking WHERE ticket_id = ? ORDER BY updated_at ASC, id ASC";
    private final String latestTicket = "SELECT * FROM ticket_tracking WHERE ticket_id = ? ORDER BY updated_at DESC, id DESC LIMIT 1";
    private final String ticketupdatedBy = "SELECT * FROM ticket_tracking WHERE updated_by = ? ORDER BY updated_at DESC";

    @Override
    public int insert(TicketTracking tracking) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertTicket, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tracking.getTicketId());
            ps.setString(2, tracking.getStatus());
            ps.setInt(3, tracking.getProgress());
            ps.setString(4, tracking.getComment());
            if (tracking.getUpdatedBy() != null) {
                ps.setInt(5, tracking.getUpdatedBy());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

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
    public TicketTracking findById(int id) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findTicketById)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<TicketTracking> findByTicket(int ticketId) throws SQLException {

        List<TicketTracking> history = new ArrayList<>();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findByTicket)) {

            ps.setInt(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(mapRow(rs));
                }
            }
        }
        return history;
    }

    @Override
    public TicketTracking findLatestByTicket(int ticketId) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(latestTicket)) {

            ps.setInt(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<TicketTracking> findByUpdatedBy(int userId) throws SQLException {

        List<TicketTracking> history = new ArrayList<>();

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(ticketupdatedBy)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(mapRow(rs));
                }
            }
        }
        return history;
    }

    private TicketTracking mapRow(ResultSet rs) throws SQLException {
        Timestamp updatedAt = rs.getTimestamp("updated_at");

        int updatedByRaw = rs.getInt("updated_by");
        Integer updatedBy = rs.wasNull() ? null : updatedByRaw;

        return new TicketTracking(
                rs.getInt("id"),
                rs.getInt("ticket_id"),
                rs.getString("status"),
                rs.getInt("progress"),
                rs.getString("comment"),
                updatedBy,
                updatedAt != null ? updatedAt.toLocalDateTime() : null
        );
    }
}