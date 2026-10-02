package com.ptms.app.service;

import com.ptms.app.model.Ticket;
import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;

public interface TicketService {
    Ticket createTicket(Ticket ticket, User requestingUser) throws SQLException;
    Ticket getTicketById(int ticketId) throws SQLException;
    List<Ticket> getTicketsForProject(int projectId) throws SQLException;
    List<Ticket> getTicketsForUser(int userId) throws SQLException;
    void assignTicket(int ticketId, int userId, User requestingUser) throws SQLException;
    void advanceStatus(int ticketId, String newStatus, int progress,
                       String comment, User requestingUser) throws SQLException;
    void deleteTicket(int ticketId, User requestingUser) throws SQLException;
}