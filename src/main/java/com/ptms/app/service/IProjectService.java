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

public class IProjectService implements ProjectService {

    private final ProjectDao projectDao;
    private final ProjectMemberDao projectMemberDao;
    private final UserDao userDao;

    public IProjectService() {
        this.projectDao = new IProjectDao();
        this.projectMemberDao = new IProjectMemberDao();
        this.userDao = new IUserDao();
    }

    public IProjectService(
            ProjectDao projectDao,
            ProjectMemberDao projectMemberDao,
            UserDao userDao
    ) {
        this.projectDao = projectDao;
        this.projectMemberDao = projectMemberDao;
        this.userDao = userDao;
    }

    @Override
    public Project createProject(
            Project project,
            User requestingUser
    ) throws SQLException {
        requireRole(
                requestingUser,
                User.Role.ADMIN,
                User.Role.PROJECT_MANAGER
        );

        validateProject(project);

        if (project.getManagerId() == null) {
            project.setManagerId(requestingUser.getId());
        }

        if (requestingUser.getRole() == User.Role.PROJECT_MANAGER
                && project.getManagerId() != requestingUser.getId()) {
            throw new UnauthorizedException(
                    "A Project Manager can only create projects for themselves."
            );
        }

        projectDao.insertProject(project);

        ProjectMember managerMembership = new ProjectMember(
                project.getId(),
                project.getManagerId(),
                "PROJECT_MANAGER"
        );

        projectMemberDao.insertMember(managerMembership);

        return project;
    }

    @Override
    public Project getProjectById(
            int projectId,
            User requestingUser
    ) throws SQLException {
        Project project = findProject(projectId);

        if (!canViewProject(project, requestingUser)) {
            throw new UnauthorizedException(
                    "You are not authorized to view this project."
            );
        }

        return project;
    }

    @Override
    public List<Project> getAllProjects(User requestingUser) throws SQLException {
        if (requestingUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException(
                    "Only Admin can view all projects."
            );
        }

        return projectDao.findAll();
    }

    @Override
    public List<Project> getProjectsForUser(User requestingUser) throws SQLException {
        switch (requestingUser.getRole()) {
            case ADMIN:
                return projectDao.findAll();

            case PROJECT_MANAGER:
                return projectDao.findByManagerId(requestingUser.getId());

            case TEAM_LEAD:
                return projectDao.findByTeamLeadId(requestingUser.getId());

            case TEAM_MEMBER:
                List<ProjectMember> memberships =
                        projectMemberDao.findByUserId(requestingUser.getId());

                List<Project> projects = new ArrayList<>();

                for (ProjectMember membership : memberships) {
                    Project project =
                            projectDao.findByProjectId(membership.getProjectId());

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
    public void assignTeamLead(
            int projectId,
            int teamLeadUserId,
            User requestingUser
    ) throws SQLException {
        requireRole(
                requestingUser,
                User.Role.ADMIN,
                User.Role.PROJECT_MANAGER
        );

        Project project = findProject(projectId);

        if (requestingUser.getRole() == User.Role.PROJECT_MANAGER
                && !isProjectManager(project, requestingUser)) {
            throw new UnauthorizedException(
                    "You can assign a Team Lead only to your own project."
            );
        }

        User teamLead = userDao.findByUserId(teamLeadUserId);

        if (teamLead == null) {
            throw new ResourceNotFoundException(
                    "Team Lead user not found: " + teamLeadUserId
            );
        }

        if (teamLead.getRole() != User.Role.TEAM_LEAD) {
            throw new ValidationException(
                    "Selected user is not a TEAM_LEAD."
            );
        }

        project.setTeamLeadId(teamLeadUserId);
        projectDao.updateProject(project);

        List<ProjectMember> members =
                projectMemberDao.findByProjectId(projectId);

        boolean alreadyMember = members.stream()
                .anyMatch(member ->
                        member.getUserId().equals(teamLeadUserId));

        if (!alreadyMember) {
            ProjectMember member = new ProjectMember(
                    projectId,
                    teamLeadUserId,
                    "TEAM_LEAD"
            );

            projectMemberDao.insertMember(member);
        }
    }

    @Override
    public void updateProject(
            Project project,
            User requestingUser
    ) throws SQLException {
        Project existing = findProject(project.getId());

        if (requestingUser.getRole() == User.Role.ADMIN) {
            validateProject(project);

            int rows = projectDao.updateProject(project);

            if (rows == 0) {
                throw new ResourceNotFoundException(
                        "Project could not be updated."
                );
            }

            return;
        }

        if (requestingUser.getRole() == User.Role.PROJECT_MANAGER) {
            if (!isProjectManager(existing, requestingUser)) {
                throw new UnauthorizedException(
                        "You can update only your own projects."
                );
            }

            project.setManagerId(existing.getManagerId());

            int rows = projectDao.updateProject(project);

            if (rows == 0) {
                throw new ResourceNotFoundException(
                        "Project could not be updated."
                );
            }

            return;
        }

        throw new UnauthorizedException(
                "You are not authorized to update projects."
        );
    }

    @Override
    public void deleteProject(
            int projectId,
            User requestingUser
    ) throws SQLException {
        if (requestingUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException(
                    "Only Admin can delete projects."
            );
        }

        findProject(projectId);

        int rows = projectDao.deleteProject(projectId);

        if (rows == 0) {
            throw new ResourceNotFoundException(
                    "Project could not be deleted."
            );
        }
    }

    private Project findProject(int projectId) throws SQLException {
        if (projectId <= 0) {
            throw new ValidationException(
                    "Project ID must be greater than zero."
            );
        }

        Project project = projectDao.findByProjectId(projectId);

        if (project == null) {
            throw new ResourceNotFoundException(
                    "No project found with id " + projectId
            );
        }

        return project;
    }

    private boolean isProjectManager(Project project, User user) {
        return project.getManagerId() != null
                && project.getManagerId() == user.getId();
    }

    private boolean canViewProject(
            Project project,
            User user
    ) throws SQLException {
        switch (user.getRole()) {
            case ADMIN:
                return true;

            case PROJECT_MANAGER:
                return isProjectManager(project, user);

            case TEAM_LEAD:
                return project.getTeamLeadId() != null
                        && project.getTeamLeadId() == user.getId();

            case TEAM_MEMBER:
                List<ProjectMember> members =
                        projectMemberDao.findByProjectId(project.getId());

                return members.stream()
                        .anyMatch(member ->
                                member.getUserId().equals(user.getId()));

            default:
                return false;
        }
    }

    private void requireRole(
            User user,
            User.Role... allowedRoles
    ) {
        for (User.Role role : allowedRoles) {
            if (user.getRole() == role) {
                return;
            }
        }

        throw new UnauthorizedException(
                "You are not authorized to perform this operation."
        );
    }

    private void validateProject(Project project) {
        if (project == null) {
            throw new ValidationException("Project cannot be null.");
        }

        if (project.getName() == null || project.getName().isBlank()) {
            throw new ValidationException("Project name is required.");
        }

        if (project.getName().length() > 100) {
            throw new ValidationException(
                    "Project name cannot exceed 100 characters."
            );
        }

        if (project.getPriority() == null || project.getPriority().isBlank()) {
            throw new ValidationException("Project priority is required.");
        }

        String priority = project.getPriority().trim().toUpperCase();

        if (!priority.equals("LOW")
                && !priority.equals("MEDIUM")
                && !priority.equals("HIGH")) {
            throw new ValidationException(
                    "Priority must be LOW, MEDIUM or HIGH."
            );
        }

        project.setPriority(priority);

        if (project.getCost() != null && project.getCost().signum() < 0) {
            throw new ValidationException(
                    "Project cost cannot be negative."
            );
        }

        if (project.getStartDate() != null
                && project.getDeadline() != null
                && project.getDeadline().isBefore(project.getStartDate())) {
            throw new ValidationException(
                    "Deadline cannot be before start date."
            );
        }
    }
}