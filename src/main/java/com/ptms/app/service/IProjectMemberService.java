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

public class IProjectMemberService implements ProjectMemberService {

    private final ProjectMemberDao projectMemberDao;
    private final ProjectDao projectDao;
    private final UserDao userDao;

    public IProjectMemberService() {
        this.projectMemberDao = new IProjectMemberDao();
        this.projectDao = new IProjectDao();
        this.userDao = new IUserDao();
    }

    public IProjectMemberService(
            ProjectMemberDao projectMemberDao,
            ProjectDao projectDao,
            UserDao userDao
    ) {
        this.projectMemberDao = projectMemberDao;
        this.projectDao = projectDao;
        this.userDao = userDao;
    }

    @Override
    public void addMember(
            int projectId,
            int userId,
            String roleInProject,
            User requestingUser
    ) throws SQLException {
        Project project = requireProject(projectId);

        requireCanManageMembers(project, requestingUser);

        if (userDao.findByUserId(userId) == null) {
            throw new ResourceNotFoundException(
                    "No user found with id " + userId
            );
        }

        ProjectMember existing =
                projectMemberDao.findMembership(projectId, userId);

        if (existing != null) {
            throw new ValidationException(
                    "User is already a member of this project."
            );
        }

        if (roleInProject == null || roleInProject.trim().isEmpty()) {
            throw new ValidationException(
                    "Project role cannot be empty."
            );
        }

        ProjectMember member = new ProjectMember(
                projectId,
                userId,
                roleInProject.trim()
        );

        projectMemberDao.insertMember(member);
    }

    @Override
    public void removeMember(
            int projectId,
            int userId,
            User requestingUser
    ) throws SQLException {
        Project project = requireProject(projectId);

        requireCanManageMembers(project, requestingUser);

        int rows = projectMemberDao.deleteMember(projectId, userId);

        if (rows == 0) {
            throw new ResourceNotFoundException(
                    "User is not a member of this project."
            );
        }
    }

    @Override
    public void updateMemberRole(
            int projectId,
            int userId,
            String roleInProject,
            User requestingUser
    ) throws SQLException {
        Project project = requireProject(projectId);

        requireCanManageMembers(project, requestingUser);

        if (roleInProject == null || roleInProject.trim().isEmpty()) {
            throw new ValidationException(
                    "Project role cannot be empty."
            );
        }

        int rows = projectMemberDao.updateRole(
                projectId,
                userId,
                roleInProject.trim()
        );

        if (rows == 0) {
            throw new ResourceNotFoundException(
                    "Project member not found."
            );
        }
    }

    @Override
    public List<ProjectMember> getMembersOfProject(
            int projectId
    ) throws SQLException {
        requireProject(projectId);
        return projectMemberDao.findByProjectId(projectId);
    }

    @Override
    public List<ProjectMember> getProjectsForMember(
            int userId
    ) throws SQLException {
        if (userDao.findByUserId(userId) == null) {
            throw new ResourceNotFoundException(
                    "No user found with id " + userId
            );
        }

        return projectMemberDao.findByUserId(userId);
    }

    @Override
    public List<ProjectMember> getMyProjects(
            User loggedInUser
    ) throws SQLException {
        validateUser(loggedInUser);
        return projectMemberDao.findByUserId(loggedInUser.getId());
    }

    @Override
    public Project getProjectDetails(
            int projectId,
            User loggedInUser
    ) throws SQLException {
        validateUser(loggedInUser);
        requireMembership(projectId, loggedInUser.getId());
        return requireProject(projectId);
    }

    @Override
    public List<ProjectMember> getProjectTeam(
            int projectId,
            User loggedInUser
    ) throws SQLException {
        validateUser(loggedInUser);
        requireMembership(projectId, loggedInUser.getId());
        return projectMemberDao.findByProjectId(projectId);
    }

    @Override
    public ProjectMember getMyMembership(
            int projectId,
            User loggedInUser
    ) throws SQLException {
        validateUser(loggedInUser);
        return requireMembership(projectId, loggedInUser.getId());
    }

    private Project requireProject(int projectId) throws SQLException {
        Project project = projectDao.findByProjectId(projectId);

        if (project == null) {
            throw new ResourceNotFoundException(
                    "No project found with id " + projectId
            );
        }

        return project;
    }

    private ProjectMember requireMembership(
            int projectId,
            int userId
    ) throws SQLException {
        ProjectMember membership =
                projectMemberDao.findMembership(projectId, userId);

        if (membership == null) {
            throw new UnauthorizedException(
                    "You are not a member of this project."
            );
        }

        return membership;
    }

    private void requireCanManageMembers(
            Project project,
            User requestingUser
    ) {
        if (requestingUser == null) {
            throw new UnauthorizedException(
                    "You must be logged in."
            );
        }

        boolean isAdmin =
                requestingUser.getRole() == User.Role.ADMIN;

        boolean isProjectManager =
                requestingUser.getRole() == User.Role.PROJECT_MANAGER
                        && project.getManagerId().equals(requestingUser.getId());

        boolean isTeamLead =
                requestingUser.getRole() == User.Role.TEAM_LEAD
                        && project.getTeamLeadId().equals(requestingUser.getId());

        if (!isAdmin && !isProjectManager && !isTeamLead) {
            throw new UnauthorizedException(
                    "Only Admin, the project's Manager, or the project's Team Lead can manage members."
            );
        }
    }

    private void validateUser(User loggedInUser) {
        if (loggedInUser == null) {
            throw new UnauthorizedException(
                    "You must be logged in."
            );
        }

        if (loggedInUser.getId() <= 0) {
            throw new UnauthorizedException(
                    "Invalid logged-in user."
            );
        }
    }
}