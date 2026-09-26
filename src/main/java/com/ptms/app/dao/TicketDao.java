package com.ptms.app.dao;

import com.ptms.app.model.Ticket;

import java.sql.SQLException;
import java.util.List;

public interface TicketDao {

    int insert(Ticket ticket) throws SQLException;

    Ticket findById(int id) throws SQLException;

    List<Ticket> findByProjectId(int projectId) throws SQLException;

    List<Ticket> findByAssignedTo(int userId) throws SQLException;

    int update(Ticket ticket) throws SQLException;

    int delete(int id) throws SQLException;
}
