package com.ptms.app.service;

import com.ptms.app.model.TicketTracking;
import com.ptms.app.model.User;
import java.sql.SQLException;
import java.util.List;

public interface TicketTrackingService {
    TicketTracking getTrackingForTicket(int ticketId, User requestingUser) throws SQLException;
    List<TicketTracking> getUpdatesByUser(int userId, User requestingUser) throws SQLException;
}