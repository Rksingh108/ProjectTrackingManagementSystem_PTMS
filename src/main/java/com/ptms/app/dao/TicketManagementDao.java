package com.ptms.app.dao;

import com.ptms.app.model.TicketManagement;

import java.sql.SQLException;
import java.util.List;

public interface TicketManagementDao {

    int insert(TicketManagement ticket) throws SQLException;

    TicketManagement findById(int id) throws SQLException;

    List<TicketManagement> findAll() throws SQLException;

    List<TicketManagement> findByProject(int projectId) throws SQLException;

    List<TicketManagement> findByAssignedTo(int userId) throws SQLException;

    List<TicketManagement> findByStatus(String status) throws SQLException;

    List<TicketManagement> findByPriority(String priority) throws SQLException;

    /** Filters tickets belonging to a project by status, priority and/or assignee (any may be null). */
    List<TicketManagement> filter(int projectId, String status, String priority, Integer assignedTo) throws SQLException;

    boolean update(TicketManagement ticket) throws SQLException;

    boolean updateStatus(int ticketId, String status) throws SQLException;

    boolean delete(int id) throws SQLException;
}