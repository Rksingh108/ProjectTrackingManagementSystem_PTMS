package com.ptms.app.service;

import com.ptms.app.model.Project;
import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;

public interface ProjectService {

    Project createProject(Project project, User requestingUser) throws SQLException;

    Project getProjectById(int projectId, User requestingUser) throws SQLException;

    List<Project> getAllProjects(User requestingUser) throws SQLException;

    List<Project> getProjectsForUser(User requestingUser) throws SQLException;

    void assignTeamLead(int projectId, int teamLeadUserId, User requestingUser) throws SQLException;

    void updateProject(Project project, User requestingUser) throws SQLException;

    void deleteProject(int projectId, User requestingUser) throws SQLException;
}