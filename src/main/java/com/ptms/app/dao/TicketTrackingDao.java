package com.ptms.app.dao;

import com.ptms.app.model.TicketTracking;

import java.sql.SQLException;
import java.util.List;

public interface TicketTrackingDao {

    int insert(TicketTracking tracking) throws SQLException;

    TicketTracking findByTicketId(int ticketId) throws SQLException;

    List<TicketTracking> findByUpdatedBy(int userId) throws SQLException;

    int update(TicketTracking tracking) throws SQLException;

    int delete(int ticketId) throws SQLException;
}
