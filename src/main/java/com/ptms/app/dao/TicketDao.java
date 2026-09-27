package com.ptms.app.dao;

import com.ptms.app.model.Ticket;

import java.sql.SQLException;
import java.util.List;

public interface TicketDao {

    int insertTicket(Ticket ticket) throws SQLException;
    Ticket findByTicketId(int id) throws SQLException;
    List<Ticket> findByProjectId(int projectId) throws SQLException;
    List<Ticket> findByAssignedTo(int userId) throws SQLException;
    int updateTicket(Ticket ticket) throws SQLException;
    int deleteTicket(int id) throws SQLException;

}
