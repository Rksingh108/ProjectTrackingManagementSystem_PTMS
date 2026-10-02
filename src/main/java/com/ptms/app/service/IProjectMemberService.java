package com.ptms.app.service;

import com.ptms.app.dao.IProjectDao;
import com.ptms.app.dao.IProjectMemberDao;
import com.ptms.app.dao.IUserDao;
import com.ptms.app.dao.ProjectDao;
import com.ptms.app.dao.ProjectMemberDao;
import com.ptms.app.dao.UserDao;
import com.ptms.app.exception.DuplicateResourceException;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Project;
import com.ptms.app.model.ProjectMember;
import com.ptms.app.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class IProjectMemberService implements ProjectMemberService {

    private static final Logger logger = LoggerFactory.getLogger(IProjectMemberService.class);

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
        logger.info("Adding project member. Project ID: {}, User ID: {}, Requested by: {}",
                projectId, userId, requestingUser != null ? requestingUser.getUsername() : "unknown");

        Project project = requireProject(projectId);
        requireCanManageMembers(project, requestingUser);

        if (roleInProject == null || roleInProject.isBlank()) {
            throw new ValidationException("Project role cannot be empty.");
        }

        String role = roleInProject.trim().toUpperCase();

        if (!"TEAM_MEMBER".equals(role)) {
            throw new ValidationException("Only TEAM_MEMBER can be added.");
        }

        User user = userDao.findByUserId(userId);

        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        if (user.getRole() != User.Role.TEAM_MEMBER) {
            throw new ValidationException("Selected user is not a TEAM_MEMBER.");
        }

        ProjectMember existing = projectMemberDao.findMembership(projectId, userId);

        if (existing != null) {
            throw new DuplicateResourceException("User is already a member of this project.");
        }

        ProjectMember member = new ProjectMember(projectId, userId, "TEAM_MEMBER");
        int rows = projectMemberDao.insertMember(member);

        if (rows == 0) {
            throw new ValidationException("Team member could not be added.");
        }

        logger.info("Team member added successfully. Project ID: {}, User ID: {}, Added by: {}",
                projectId, userId, requestingUser.getId());
    }

    @Override
    public void removeMember(int projectId, int userId, User requestingUser) throws SQLException {
        logger.info("Removing project member. Project ID: {}, User ID: {}, Requested by: {}",
                projectId, userId, requestingUser != null ? requestingUser.getUsername() : "unknown");

        Project project = requireProject(projectId);
        requireCanManageMembers(project, requestingUser);

        ProjectMember membership = requireMembership(projectId, userId);
        String role = membership.getRoleInProject();

        if ("PROJECT_MANAGER".equalsIgnoreCase(role)) {
            throw new UnauthorizedException("PROJECT_MANAGER cannot be removed from the project.");
        }

        if ("TEAM_LEAD".equalsIgnoreCase(role)) {
            throw new UnauthorizedException("TEAM_LEAD cannot be removed using this operation.");
        }

        int rows = projectMemberDao.deleteMember(projectId, userId);

        if (rows == 0) {
            throw new ResourceNotFoundException("Project member not found.");
        }

        logger.info("Project member removed successfully. Project ID: {}, User ID: {}, Removed by: {}",
                projectId, userId, requestingUser.getId());
    }

    @Override
    public void updateMemberRole(int projectId, int userId, String roleInProject, User requestingUser) throws SQLException {
        logger.info("Updating project member role. Project ID: {}, User ID: {}, Requested by: {}",
                projectId, userId, requestingUser != null ? requestingUser.getUsername() : "unknown");

        Project project = requireProject(projectId);
        requireCanManageMembers(project, requestingUser);

        if (!"TEAM_MEMBER".equalsIgnoreCase(roleInProject)) {
            throw new ValidationException("Only TEAM_MEMBER role can be assigned.");
        }

        ProjectMember membership = requireMembership(projectId, userId);

        if ("PROJECT_MANAGER".equalsIgnoreCase(membership.getRoleInProject())) {
            throw new UnauthorizedException("PROJECT_MANAGER role cannot be changed here.");
        }

        if ("TEAM_LEAD".equalsIgnoreCase(membership.getRoleInProject())) {
            throw new UnauthorizedException("TEAM_LEAD role cannot be changed here.");
        }

        int rows = projectMemberDao.updateRole(projectId, userId, "TEAM_MEMBER");

        if (rows == 0) {
            throw new ResourceNotFoundException("Project member not found.");
        }

        logger.info("Project member role updated. Project ID: {}, User ID: {}, Updated by: {}",
                projectId, userId, requestingUser.getId());
    }

    @Override
    public List<ProjectMember> getMembersOfProject(int projectId) throws SQLException {
        requireProject(projectId);
        return projectMemberDao.findByProjectId(projectId);
    }

    @Override
    public List<ProjectMember> getProjectsForMember(int userId) throws SQLException {
        User user = userDao.findByUserId(userId);

        if (user == null) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        return projectMemberDao.findByUserId(userId);
    }

    @Override
    public List<ProjectMember> getMyProjects(User loggedInUser) throws SQLException {
        if (loggedInUser == null) {
            throw new UnauthorizedException("User must be logged in.");
        }

        return projectMemberDao.findByUserId(loggedInUser.getId());
    }

    @Override
    public Project getProjectDetails(int projectId, User loggedInUser) throws SQLException {
        Project project = requireProject(projectId);
        requireCanViewProject(project, loggedInUser);
        return project;
    }

    @Override
    public List<ProjectMember> getProjectTeam(int projectId, User loggedInUser) throws SQLException {
        Project project = requireProject(projectId);
        requireCanViewProject(project, loggedInUser);
        return projectMemberDao.findByProjectId(projectId);
    }

    @Override
    public ProjectMember getMyMembership(int projectId, User loggedInUser) throws SQLException {
        if (loggedInUser == null) {
            throw new UnauthorizedException("User must be logged in.");
        }

        return requireMembership(projectId, loggedInUser.getId());
    }

    private Project requireProject(int projectId) throws SQLException {
        if (projectId <= 0) {
            throw new ValidationException("Invalid project ID.");
        }

        Project project = projectDao.findByProjectId(projectId);

        if (project == null) {
            throw new ResourceNotFoundException("Project not found with ID: " + projectId);
        }

        return project;
    }

    private ProjectMember requireMembership(int projectId, int userId) throws SQLException {
        ProjectMember membership = projectMemberDao.findMembership(projectId, userId);

        if (membership == null) {
            throw new UnauthorizedException("You are not a member of this project.");
        }

        return membership;
    }

    private void requireCanManageMembers(Project project, User requestingUser) {
        if (requestingUser == null) {
            throw new UnauthorizedException("User must be logged in.");
        }

        if (requestingUser.getRole() == User.Role.ADMIN) {
            return;
        }

        if (requestingUser.getRole() == User.Role.TEAM_LEAD) {
            if (project.getTeamLeadId() != null && project.getTeamLeadId().equals(requestingUser.getId())) {
                return;
            }

            throw new UnauthorizedException("You can manage members only for projects assigned to you.");
        }

        throw new UnauthorizedException("Only ADMIN or TEAM_LEAD can manage project members.");
    }

    private void requireCanViewProject(Project project, User requestingUser) throws SQLException {
        if (requestingUser == null) {
            throw new UnauthorizedException("User must be logged in.");
        }

        if (requestingUser.getRole() == User.Role.ADMIN) {
            return;
        }

        if (requestingUser.getRole() == User.Role.PROJECT_MANAGER) {
            if (project.getManagerId() != null && project.getManagerId().equals(requestingUser.getId())) {
                return;
            }

            throw new UnauthorizedException("You can view only your own projects.");
        }

        if (requestingUser.getRole() == User.Role.TEAM_LEAD) {
            if (project.getTeamLeadId() != null && project.getTeamLeadId().equals(requestingUser.getId())) {
                return;
            }

            throw new UnauthorizedException("You can view only projects assigned to you.");
        }

        ProjectMember membership = projectMemberDao.findMembership(project.getId(), requestingUser.getId());

        if (membership == null) {
            throw new UnauthorizedException("You are not a member of this project.");
        }
    }
}