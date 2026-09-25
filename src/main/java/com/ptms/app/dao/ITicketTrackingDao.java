package com.ptms.app.dao;

import com.ptms.app.model.TicketTracking;
import com.ptms.app.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketTrackingDAOImpl implements TicketTrackingDAO {

    @Override
    public int insert(TicketTracking tracking) throws SQLException {
        String sql = "INSERT INTO ticket_tracking (ticket_id, status, progress, comment, updated_by) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
        String sql = "SELECT * FROM ticket_tracking WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<TicketTracking> findByTicket(int ticketId) throws SQLException {
        String sql = "SELECT * FROM ticket_tracking WHERE ticket_id = ? ORDER BY updated_at ASC, id ASC";
        List<TicketTracking> history = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
        String sql = "SELECT * FROM ticket_tracking WHERE ticket_id = ? ORDER BY updated_at DESC, id DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<TicketTracking> findByUpdatedBy(int userId) throws SQLException {
        String sql = "SELECT * FROM ticket_tracking WHERE updated_by = ? ORDER BY updated_at DESC";
        List<TicketTracking> history = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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