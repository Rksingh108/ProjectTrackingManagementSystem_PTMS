package com.ptms.app.dao;

import com.ptms.app.model.Projects;
import java.sql.SQLException;
import java.util.List;

public interface ProjectDao {

    int insert(Projects project) throws SQLException;

    Projects findById(int id) throws SQLException;

    List<Projects> findAll() throws SQLException;

    List<Projects> findByManager(int managerId) throws SQLException;

    List<Projects> findByTeamLead(int teamLeadId) throws SQLException;

    List<Projects> findByClient(int clientId) throws SQLException;

    List<Projects> findByStatus(String status) throws SQLException;

    List<Projects> searchByName(String keyword) throws SQLException;

    boolean update(Projects project) throws SQLException;

    boolean assignTeamLead(int projectId, int teamLeadId) throws SQLException;

    boolean updateStatus(int projectId, String status) throws SQLException;

    boolean delete(int id) throws SQLException;
}