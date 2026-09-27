package com.ptms.app.service;

import com.ptms.app.model.Project;
import com.ptms.app.model.ProjectMember;
import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;

public interface ProjectMemberService {

    void addMember(int projectId, int userId, String roleInProject, User requestingUser) throws SQLException;

    void removeMember(int projectId, int userId, User requestingUser) throws SQLException;

    void updateMemberRole(int projectId, int userId, String roleInProject, User requestingUser) throws SQLException;

    List<ProjectMember> getMembersOfProject(int projectId) throws SQLException;

    List<ProjectMember> getProjectsForMember(int userId) throws SQLException;

    List<ProjectMember> getMyProjects(User loggedInUser) throws SQLException;

    Project getProjectDetails(int projectId, User loggedInUser) throws SQLException;

    List<ProjectMember> getProjectTeam(int projectId, User loggedInUser) throws SQLException;

    ProjectMember getMyMembership(int projectId, User loggedInUser) throws SQLException;
}