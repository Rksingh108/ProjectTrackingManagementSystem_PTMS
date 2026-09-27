package com.ptms.app.service;

import com.ptms.app.model.TicketTracking;

import java.sql.SQLException;
import java.util.List;

public interface TicketTrackingService {

    TicketTracking getTrackingForTicket(int ticketId) throws SQLException;

    List<TicketTracking> getUpdatesByUser(int userId) throws SQLException;
}