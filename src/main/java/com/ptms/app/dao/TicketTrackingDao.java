package com.ptms.app.dao;

import com.ptms.app.model.TicketTracking;

import java.sql.SQLException;
import java.util.List;

public interface TicketTrackingDao {

    int insertTicket(TicketTracking tracking) throws SQLException;
    int updateTicket(TicketTracking tracking) throws SQLException;
    int deleteTicket(int ticketId) throws SQLException;
    TicketTracking findByTicketId(int ticketId) throws SQLException;
    List<TicketTracking> findByUpdatedBy(int userId) throws SQLException;

}
