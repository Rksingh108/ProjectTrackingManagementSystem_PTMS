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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IProjectService implements ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(IProjectService.class);

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
        requireRole(requestingUser, User.Role.PROJECT_MANAGER, User.Role.ADMIN);
        validateProject(project);

        if (requestingUser.getRole() == User.Role.PROJECT_MANAGER) {
            project.setManagerId(requestingUser.getId());
        } else {
            project.setManagerId(null);
        }

        project.setTeamLeadId(null);

        if (project.getStatus() == null || project.getStatus().isBlank()) {
            project.setStatus("IN_PROGRESS");
        }

        int rows = projectDao.insertProject(project);

        if (rows == 0) {
            throw new ValidationException("Project could not be created.");
        }

        if (requestingUser.getRole() == User.Role.PROJECT_MANAGER) {
            ProjectMember managerMembership = new ProjectMember(
                    project.getId(),
                    requestingUser.getId(),
                    "PROJECT_MANAGER"
            );

            projectMemberDao.insertMember(managerMembership);
        }

        logger.info("Project created. Project ID: {}, User ID: {}",
                project.getId(), requestingUser.getId());

        return project;
    }

    @Override
    public Project getProjectById(int projectId, User requestingUser) throws SQLException {
        validateId(projectId);

        Project project = findProject(projectId);

        if (!canViewProject(project, requestingUser)) {
            throw new UnauthorizedException("You are not authorized to view this project.");
        }

        logger.info("Project viewed. Project ID: {}, User ID: {}",
                projectId, requestingUser.getId());

        return project;
    }

    @Override
    public List<Project> getAllProjects(User requestingUser) throws SQLException {
        requireRole(requestingUser, User.Role.ADMIN);
        return projectDao.findAll();
    }

    @Override
    public List<Project> getProjectsForUser(User requestingUser) throws SQLException {
        validateUser(requestingUser);

        switch (requestingUser.getRole()) {
            case ADMIN:
                return projectDao.findAll();

            case PROJECT_MANAGER:
                return projectDao.findByManagerId(requestingUser.getId());

            case TEAM_LEAD:
                return projectDao.findByTeamLeadId(requestingUser.getId());

            case TEAM_MEMBER:
                List<ProjectMember> memberships = projectMemberDao.findByUserId(requestingUser.getId());
                List<Project> projects = new ArrayList<>();

                for (ProjectMember membership : memberships) {
                    Project project = projectDao.findByProjectId(membership.getProjectId());

                    if (project != null) {
                        projects.add(project);
                    }
                }

                return projects;

            default:
                throw new UnauthorizedException("Invalid user role.");
        }
    }

    @Override
    public void assignProjectManager(int projectId, int managerUserId, User requestingUser) throws SQLException {
        requireRole(requestingUser, User.Role.ADMIN);

        Project project = findProject(projectId);
        User manager = userDao.findByUserId(managerUserId);

        if (manager == null) {
            throw new ResourceNotFoundException("Project Manager not found.");
        }

        if (manager.getRole() != User.Role.PROJECT_MANAGER) {
            throw new ValidationException("Selected user is not a PROJECT_MANAGER.");
        }

        project.setManagerId(managerUserId);

        int rows = projectDao.updateProject(project);

        if (rows == 0) {
            throw new ValidationException("Project Manager assignment failed.");
        }

        ProjectMember membership = projectMemberDao.findMembership(projectId, managerUserId);

        if (membership == null) {
            projectMemberDao.insertMember(
                    new ProjectMember(projectId, managerUserId, "PROJECT_MANAGER")
            );
        } else {
            projectMemberDao.updateRole(projectId, managerUserId, "PROJECT_MANAGER");
        }

        logger.info("Project Manager assigned. Project ID: {}, Manager ID: {}, Admin ID: {}",
                projectId, managerUserId, requestingUser.getId());
    }

    @Override
    public void assignTeamLead(int projectId, int teamLeadUserId, User requestingUser) throws SQLException {
        requireRole(requestingUser, User.Role.PROJECT_MANAGER, User.Role.ADMIN);

        Project project = findProject(projectId);

        if (requestingUser.getRole() == User.Role.PROJECT_MANAGER
                && !isProjectManager(project, requestingUser)) {
            throw new UnauthorizedException("You can assign a Team Lead only to your own project.");
        }

        User teamLead = userDao.findByUserId(teamLeadUserId);

        if (teamLead == null) {
            throw new ResourceNotFoundException("Team Lead not found.");
        }

        if (teamLead.getRole() != User.Role.TEAM_LEAD) {
            throw new ValidationException("Selected user is not a TEAM_LEAD.");
        }

        project.setTeamLeadId(teamLeadUserId);

        int rows = projectDao.updateProject(project);

        if (rows == 0) {
            throw new ValidationException("Team Lead assignment failed.");
        }

        ProjectMember membership = projectMemberDao.findMembership(projectId, teamLeadUserId);

        if (membership == null) {
            projectMemberDao.insertMember(
                    new ProjectMember(projectId, teamLeadUserId, "TEAM_LEAD")
            );
        } else {
            projectMemberDao.updateRole(projectId, teamLeadUserId, "TEAM_LEAD");
        }

        logger.info("Team Lead assigned. Project ID: {}, Team Lead ID: {}, User ID: {}",
                projectId, teamLeadUserId, requestingUser.getId());
    }

    @Override
    public void updateProject(Project project, User requestingUser) throws SQLException {
        if (project == null) {
            throw new ValidationException("Project cannot be null.");
        }

        requireRole(requestingUser, User.Role.PROJECT_MANAGER, User.Role.ADMIN);

        Project existing = findProject(project.getId());

        if (requestingUser.getRole() == User.Role.PROJECT_MANAGER) {
            if (!isProjectManager(existing, requestingUser)) {
                throw new UnauthorizedException("You can update only your own projects.");
            }

            if ("COMPLETED".equalsIgnoreCase(existing.getStatus())) {
                throw new ValidationException("Completed project cannot be modified.");
            }

            project.setManagerId(existing.getManagerId());
            project.setTeamLeadId(existing.getTeamLeadId());
            project.setClientId(existing.getClientId());
            project.setStatus(existing.getStatus());
        }

        validateProject(project);

        int rows = projectDao.updateProject(project);

        if (rows == 0) {
            throw new ResourceNotFoundException("Project could not be updated.");
        }

        logger.info("Project updated. Project ID: {}, User ID: {}",
                project.getId(), requestingUser.getId());
    }

    @Override
    public void approveProjectCompletion(int projectId, User requestingUser) throws SQLException {
        requireRole(requestingUser, User.Role.PROJECT_MANAGER);

        Project project = findProject(projectId);

        if (!isProjectManager(project, requestingUser)) {
            throw new UnauthorizedException("You can approve completion only for your own projects.");
        }

        if (project.getTeamLeadId() == null) {
            throw new ValidationException("A Team Lead must be assigned before project completion.");
        }

        if ("COMPLETED".equalsIgnoreCase(project.getStatus())) {
            throw new ValidationException("Project is already completed.");
        }

        project.setStatus("COMPLETED");

        int rows = projectDao.updateProject(project);

        if (rows == 0) {
            throw new ValidationException("Project completion approval failed.");
        }

        logger.info("Project completion approved. Project ID: {}, Manager ID: {}",
                projectId, requestingUser.getId());
    }

    @Override
    public void deleteProject(int projectId, User requestingUser) throws SQLException {
        requireRole(requestingUser, User.Role.ADMIN);
        findProject(projectId);

        int rows = projectDao.deleteProject(projectId);

        if (rows == 0) {
            throw new ResourceNotFoundException("Project could not be deleted.");
        }

        logger.info("Project deleted. Project ID: {}, Admin ID: {}",
                projectId, requestingUser.getId());
    }

    private Project findProject(int projectId) throws SQLException {
        validateId(projectId);

        Project project = projectDao.findByProjectId(projectId);

        if (project == null) {
            throw new ResourceNotFoundException("Project not found: " + projectId);
        }

        return project;
    }

    private boolean isProjectManager(Project project, User user) {
        return project.getManagerId() != null && project.getManagerId().equals(user.getId());
    }

    private boolean canViewProject(Project project, User user) throws SQLException {
        if (user == null) {
            return false;
        }

        if (user.getRole() == User.Role.ADMIN) {
            return true;
        }

        if (user.getRole() == User.Role.PROJECT_MANAGER) {
            return isProjectManager(project, user);
        }

        if (user.getRole() == User.Role.TEAM_LEAD) {
            return project.getTeamLeadId() != null && project.getTeamLeadId().equals(user.getId());
        }

        if (user.getRole() == User.Role.TEAM_MEMBER) {
            return projectMemberDao.findMembership(project.getId(), user.getId()) != null;
        }

        return false;
    }

    private void requireRole(User user, User.Role... roles) {
        validateUser(user);

        for (User.Role role : roles) {
            if (user.getRole() == role) {
                return;
            }
        }

        throw new UnauthorizedException("You are not authorized for this operation.");
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new UnauthorizedException("You must be logged in.");
        }

        if (user.getId() <= 0 || user.getRole() == null) {
            throw new UnauthorizedException("Invalid logged-in user.");
        }
    }

    private void validateId(int id) {
        if (id <= 0) {
            throw new ValidationException("ID must be greater than zero.");
        }
    }

    private void validateProject(Project project) {
        if (project == null) {
            throw new ValidationException("Project cannot be null.");
        }

        if (project.getName() == null || project.getName().isBlank()) {
            throw new ValidationException("Project name is required.");
        }

        if (project.getRequirements() == null || project.getRequirements().isBlank()) {
            throw new ValidationException("Project requirements are required.");
        }

        if (project.getPriority() == null || project.getPriority().isBlank()) {
            throw new ValidationException("Project priority is required.");
        }

        String priority = project.getPriority().trim().toUpperCase();

        if (!priority.equals("LOW") && !priority.equals("MEDIUM") && !priority.equals("HIGH")) {
            throw new ValidationException("Priority must be LOW, MEDIUM or HIGH.");
        }

        project.setPriority(priority);

        if (project.getDeadline() != null
                && project.getStartDate() != null
                && project.getDeadline().isBefore(project.getStartDate())) {
            throw new ValidationException("Deadline cannot be before start date.");
        }

        if (project.getCost() != null && project.getCost().signum() < 0) {
            throw new ValidationException("Project cost cannot be negative.");
        }
    }
}