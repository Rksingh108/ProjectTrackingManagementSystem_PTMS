package com.ptms.app.service;

import com.ptms.app.dao.IProjectDao;
import com.ptms.app.dao.IProjectMemberDao;
import com.ptms.app.dao.IUserDao;
import com.ptms.app.dao.ProjectDao;
import com.ptms.app.dao.ProjectMemberDao;
import com.ptms.app.dao.UserDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Project;
import com.ptms.app.model.ProjectMember;
import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class IProjectMemberService implements ProjectMemberService {

    private static final Logger logger = Logger.getLogger(IProjectMemberService.class.getName());

    private final ProjectMemberDao projectMemberDao;
    private final ProjectDao projectDao;
    private final UserDao userDao;

    public IProjectMemberService() {
        this.projectMemberDao = new IProjectMemberDao();
        this.projectDao = new IProjectDao();
        this.userDao = new IUserDao();
    }

    public IProjectMemberService(ProjectMemberDao projectMemberDao, ProjectDao projectDao, UserDao userDao) {
        this.projectMemberDao = projectMemberDao;
        this.projectDao = projectDao;
        this.userDao = userDao;
    }

    @Override
    public void addMember(int projectId, int userId, String roleInProject, User requestingUser) throws SQLException {
        Project project = requireProject(projectId);
        requireCanManageMembers(project, requestingUser);

        if (userDao.findById(userId) == null) {
            throw new ResourceNotFoundException("No user found with id " + userId);
        }
        boolean alreadyMember = projectMemberDao.findByProjectId(projectId).stream()
                .anyMatch(m -> m.getUserId() == userId);
        if (alreadyMember) {
            throw new ValidationException("User id=" + userId + " is already a member of project id=" + projectId);
        }

        projectMemberDao.insert(new ProjectMember(projectId, userId, roleInProject));
        logger.info("Added userId=" + userId + " to projectId=" + projectId + " by requestingUser id=" + requestingUser.getId());
    }

    @Override
    public void removeMember(int projectId, int userId, User requestingUser) throws SQLException {
        Project project = requireProject(projectId);
        requireCanManageMembers(project, requestingUser);

        int rows = projectMemberDao.delete(projectId, userId);
        if (rows == 0) {
            throw new ResourceNotFoundException("User id=" + userId + " is not a member of project id=" + projectId);
        }
        logger.info("Removed userId=" + userId + " from projectId=" + projectId + " by requestingUser id=" + requestingUser.getId());
    }

    @Override
    public List<ProjectMember> getMembersOfProject(int projectId) throws SQLException {
        return projectMemberDao.findByProjectId(projectId);
    }

    @Override
    public List<ProjectMember> getProjectsForMember(int userId) throws SQLException {
        return projectMemberDao.findByUserId(userId);
    }

    private Project requireProject(int projectId) throws SQLException {
        Project project = projectDao.findById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("No project found with id " + projectId);
        }
        return project;
    }

    private void requireCanManageMembers(Project project, User requestingUser) {
        boolean isAdmin = requestingUser.getRole() == User.Role.ADMIN;
        boolean isProjectManager = requestingUser.getRole() == User.Role.PROJECT_MANAGER
                && project.getManagerId().equals(requestingUser.getId());
        boolean isProjectTeamLead = requestingUser.getRole() == User.Role.TEAM_LEAD
                && requestingUser.getId().equals(project.getTeamLeadId());

        if (!isAdmin && !isProjectManager && !isProjectTeamLead) {
            throw new UnauthorizedException("Only an Admin, this project's Manager, or its Team Lead can manage members.");
        }
    }
}
