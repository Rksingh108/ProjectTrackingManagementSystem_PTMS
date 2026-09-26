package com.ptms.app.dao;

import com.ptms.app.model.Project;

import java.sql.SQLException;
import java.util.List;

public interface ProjectDao {

    int insert(Project project) throws SQLException;

    Project findById(int id) throws SQLException;

    List<Project> findAll() throws SQLException;

    List<Project> findByManagerId(int managerId) throws SQLException;

    List<Project> findByTeamLeadId(int teamLeadId) throws SQLException;

    List<Project> findByClientId(int clientId) throws SQLException;

    int update(Project project) throws SQLException;

    int delete(int id) throws SQLException;
}
