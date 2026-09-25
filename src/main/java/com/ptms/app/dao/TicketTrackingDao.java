package com.ptms.app.dao;

import com.ptms.app.model.TicketTracking;

import java.sql.SQLException;
import java.util.List;

public interface TicketTrackingDao {

    int insert(TicketTracking tracking) throws SQLException;

    TicketTracking findById(int id) throws SQLException;

    /** Full update history for a ticket, oldest first. */
    List<TicketTracking> findByTicket(int ticketId) throws SQLException;

    /** Most recent tracking record for a ticket (its current status/progress), or null if none exist. */
    TicketTracking findLatestByTicket(int ticketId) throws SQLException;

    List<TicketTracking> findByUpdatedBy(int userId) throws SQLException;
}