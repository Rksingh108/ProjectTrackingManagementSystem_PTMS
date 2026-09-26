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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class IProjectService implements ProjectService {

    private static final Logger logger = Logger.getLogger(IProjectService.class.getName());

    private final ProjectDao projectDao;
    private final ProjectMemberDao projectMemberDao;
    private final UserDao userDao;

    public IProjectService() {
        this.projectDao = new IProjectDao();
        this.projectMemberDao = new IProjectMemberDao();
        this.userDao = new IUserDao();
    }

    public IProjectService(ProjectDao projectDao, ProjectMemberDao projectMemberDao, UserDao userDao) {
        this.projectDao = projectDao;
        this.projectMemberDao = projectMemberDao;
        this.userDao = userDao;
    }

    @Override
    public Project createProject(Project project, User requestingUser) throws SQLException {
        if (requestingUser.getRole() != User.Role.ADMIN && requestingUser.getRole() != User.Role.PROJECT_MANAGER) {
            throw new UnauthorizedException("Only an Admin or Project Manager can create a project.");
        }
        // The manager on record is whoever is creating it, unless an Admin is
        // explicitly assigning it to a different manager.
        if (project.getManagerId() == null) {
            project.setManagerId(requestingUser.getId());
        }
        projectDao.insert(project);

        // Whoever manages the project is automatically a member of it.
        ProjectMember creatorMembership = new ProjectMember(project.getId(), project.getManagerId(), "PROJECT_MANAGER");
        projectMemberDao.insert(creatorMembership);

        logger.info("Project created: " + project.getName() + " (id=" + project.getId() + ") by user id=" + requestingUser.getId());
        return project;
    }

    @Override
    public Project getProjectById(int id) throws SQLException {
        Project project = projectDao.findById(id);
        if (project == null) {
            throw new ResourceNotFoundException("No project found with id " + id);
        }
        return project;
    }

    @Override
    public List<Project> getAllProjects() throws SQLException {
        return projectDao.findAll();
    }

    @Override
    public List<Project> getProjectsForUser(User user) throws SQLException {
        switch (user.getRole()) {
            case ADMIN:
                return projectDao.findAll();
            case PROJECT_MANAGER:
                return projectDao.findByManagerId(user.getId());
            case TEAM_LEAD:
                return projectDao.findByTeamLeadId(user.getId());
            case TEAM_MEMBER:
            default:
                List<ProjectMember> memberships = projectMemberDao.findByUserId(user.getId());
                List<Project> projects = new ArrayList<>();
                for (ProjectMember membership : memberships) {
                    Project project = projectDao.findById(membership.getProjectId());
                    if (project != null) {
                        projects.add(project);
                    }
                }
                return projects;
        }
    }

    @Override
    public void assignTeamLead(int projectId, int teamLeadUserId, User requestingUser) throws SQLException {
        if (requestingUser.getRole() != User.Role.ADMIN && requestingUser.getRole() != User.Role.PROJECT_MANAGER) {
            throw new UnauthorizedException("Only an Admin or Project Manager can assign a team lead.");
        }
        User candidate = userDao.findById(teamLeadUserId);
        if (candidate == null) {
            throw new ResourceNotFoundException("No user found with id " + teamLeadUserId);
        }
        if (candidate.getRole() != User.Role.TEAM_LEAD) {
            throw new ValidationException("User id=" + teamLeadUserId + " does not have the TEAM_LEAD role.");
        }

        Project project = getProjectById(projectId);
        project.setTeamLeadId(teamLeadUserId);
        projectDao.update(project);

        // Team lead should also show up as a project member.
        if (projectMemberDao.findByProjectId(projectId).stream().noneMatch(m -> m.getUserId().equals(teamLeadUserId))) {
            projectMemberDao.insert(new ProjectMember(projectId, teamLeadUserId, "TEAM_LEAD"));
        }

        logger.info("Team lead assigned: userId=" + teamLeadUserId + " to projectId=" + projectId);
    }

    @Override
    public void updateProject(Project project, User requestingUser) throws SQLException {
        if (requestingUser.getRole() != User.Role.ADMIN
                && !(requestingUser.getRole() == User.Role.PROJECT_MANAGER
                && project.getManagerId().equals(requestingUser.getId()))) {
            throw new UnauthorizedException("Only an Admin, or the managing Project Manager, can update this project.");
        }
        int rows = projectDao.update(project);
        if (rows == 0) {
            throw new ResourceNotFoundException("No project found with id " + project.getId() + " to update.");
        }
        logger.info("Project updated id=" + project.getId() + " by user id=" + requestingUser.getId());
    }

    @Override
    public void deleteProject(int id, User requestingUser) throws SQLException {
        if (requestingUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only an Admin can delete a project.");
        }
        int rows = projectDao.delete(id);
        if (rows == 0) {
            throw new ResourceNotFoundException("No project found with id " + id + " to delete.");
        }
        logger.info("Project deleted id=" + id + " by admin id=" + requestingUser.getId());
    }
}
