package com.ptms.app.dao;

import com.ptms.app.model.ProjectMember;

import java.sql.SQLException;
import java.util.List;

public interface ProjectMemberDao {

    int insertMember(ProjectMember member) throws SQLException;

    List<ProjectMember> findByProjectId(int projectId) throws SQLException;

    List<ProjectMember> findByUserId(int userId) throws SQLException;

    ProjectMember findMembership(int projectId, int userId) throws SQLException;

    int updateRole(int projectId, int userId, String roleInProject) throws SQLException;

    int deleteMember(int projectId, int userId) throws SQLException;
}