package com.ptms.app.service;

import com.ptms.app.dao.ProjectDao;
import com.ptms.app.dao.ProjectMemberDao;
import com.ptms.app.dao.UserDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Project;
import com.ptms.app.model.ProjectMember;
import com.ptms.app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IProjectServiceTest {

    @Mock
    private ProjectDao projectDao;

    @Mock
    private ProjectMemberDao memberDao;

    @Mock
    private UserDao userDao;

    private IProjectService service;
    private Project project;
    private User admin;
    private User manager;
    private User teamLead;
    private User member;

    @BeforeEach
    void setUp() {
        // Arrange
        service = new IProjectService(projectDao, memberDao, userDao);

        project = new Project();
        project.setId(1);
        project.setName("PTMS");
        project.setRequirements("Project tracking system");
        project.setPriority("HIGH");
        project.setManagerId(10);
        project.setTeamLeadId(20);
        project.setStatus("IN_PROGRESS");

        admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        manager = new User();
        manager.setId(10);
        manager.setRole(User.Role.PROJECT_MANAGER);

        teamLead = new User();
        teamLead.setId(20);
        teamLead.setRole(User.Role.TEAM_LEAD);

        member = new User();
        member.setId(30);
        member.setRole(User.Role.TEAM_MEMBER);
    }

    @Test
    void createProject_success() throws SQLException {
        // Arrange
        when(projectDao.insertProject(project)).thenReturn(1);

        // Act
        Project result = service.createProject(project, manager);

        // Assert
        assertEquals(project, result);
        assertEquals(10, project.getManagerId());
        assertEquals("IN_PROGRESS", project.getStatus());

        verify(projectDao).insertProject(project);
        verify(memberDao).insertMember(any(ProjectMember.class));
    }

    @Test
    void createProject_unauthorized() {
        // Arrange
        User user = member;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.createProject(project, user)
        );

        // Assert
        assertEquals(
                "You are not authorized for this operation.",
                ex.getMessage()
        );

        verifyNoInteractions(projectDao);
    }

    @Test
    void createProject_invalidProject() {
        // Arrange
        project.setName("");

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.createProject(project, manager)
        );

        // Assert
        assertEquals("Project name is required.", ex.getMessage());

        verifyNoInteractions(projectDao);
    }

    @Test
    void getProjectById_success() throws SQLException {
        // Arrange
        when(projectDao.findByProjectId(1)).thenReturn(project);

        // Act
        Project result = service.getProjectById(1, manager);

        // Assert
        assertEquals(project, result);
        verify(projectDao).findByProjectId(1);
    }

    @Test
    void getProjectById_notFound() throws SQLException {
        // Arrange
        when(projectDao.findByProjectId(1)).thenReturn(null);

        // Act
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getProjectById(1, manager)
        );

        // Assert
        assertEquals("Project not found: 1", ex.getMessage());
    }

    @Test
    void getAllProjects_success() throws SQLException {
        // Arrange
        when(projectDao.findAll()).thenReturn(List.of(project));

        // Act
        List<Project> result = service.getAllProjects(admin);

        // Assert
        assertEquals(1, result.size());
        assertEquals(project, result.get(0));

        verify(projectDao).findAll();
    }

    @Test
    void getProjectsForUser_manager() throws SQLException {
        // Arrange
        when(projectDao.findByManagerId(10))
                .thenReturn(List.of(project));

        // Act
        List<Project> result =
                service.getProjectsForUser(manager);

        // Assert
        assertEquals(1, result.size());
        verify(projectDao).findByManagerId(10);
    }

    @Test
    void getProjectsForUser_teamLead() throws SQLException {
        // Arrange
        when(projectDao.findByTeamLeadId(20))
                .thenReturn(List.of(project));

        // Act
        List<Project> result =
                service.getProjectsForUser(teamLead);

        // Assert
        assertEquals(1, result.size());
        verify(projectDao).findByTeamLeadId(20);
    }

    @Test
    void getProjectsForUser_teamMember() throws SQLException {
        // Arrange
        when(memberDao.findByUserId(30))
                .thenReturn(List.of(
                        new ProjectMember(1, 30, "TEAM_MEMBER")
                ));

        when(projectDao.findByProjectId(1))
                .thenReturn(project);

        // Act
        List<Project> result =
                service.getProjectsForUser(member);

        // Assert
        assertEquals(1, result.size());
        assertEquals(project, result.get(0));
    }

    @Test
    void assignProjectManager_success() throws SQLException {
        // Arrange
        User newManager = new User();
        newManager.setId(11);
        newManager.setRole(User.Role.PROJECT_MANAGER);

        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(userDao.findByUserId(11)).thenReturn(newManager);
        when(projectDao.updateProject(project)).thenReturn(1);
        when(memberDao.findMembership(1, 11)).thenReturn(null);

        // Act
        service.assignProjectManager(1, 11, admin);

        // Assert
        assertEquals(11, project.getManagerId());
        verify(projectDao).updateProject(project);
        verify(memberDao).insertMember(any(ProjectMember.class));
    }

    @Test
    void assignTeamLead_success() throws SQLException {
        // Arrange
        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(userDao.findByUserId(20)).thenReturn(teamLead);
        when(projectDao.updateProject(project)).thenReturn(1);
        when(memberDao.findMembership(1, 20)).thenReturn(null);

        // Act
        service.assignTeamLead(1, 20, manager);

        // Assert
        assertEquals(20, project.getTeamLeadId());
        verify(projectDao).updateProject(project);
        verify(memberDao).insertMember(any(ProjectMember.class));
    }

    @Test
    void assignTeamLead_wrongManager() throws SQLException {
        // Arrange
        User otherManager = new User();
        otherManager.setId(99);
        otherManager.setRole(User.Role.PROJECT_MANAGER);

        when(projectDao.findByProjectId(1)).thenReturn(project);

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.assignTeamLead(1, 20, otherManager)
        );

        // Assert
        assertEquals(
                "You can assign a Team Lead only to your own project.",
                ex.getMessage()
        );

        verifyNoInteractions(userDao);
    }

    @Test
    void updateProject_success() throws SQLException {
        // Arrange
        Project updated = new Project();
        updated.setId(1);
        updated.setName("Updated PTMS");
        updated.setRequirements("Updated requirements");
        updated.setPriority("MEDIUM");

        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(projectDao.updateProject(updated)).thenReturn(1);

        // Act
        service.updateProject(updated, manager);

        // Assert
        verify(projectDao).updateProject(updated);
    }

    @Test
    void updateProject_nullProject() {
        // Arrange
        Project invalidProject = null;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.updateProject(invalidProject, manager)
        );

        // Assert
        assertEquals("Project cannot be null.", ex.getMessage());
        verifyNoInteractions(projectDao);
    }

    @Test
    void approveProjectCompletion_success() throws SQLException {
        // Arrange
        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(projectDao.updateProject(project)).thenReturn(1);

        // Act
        service.approveProjectCompletion(1, manager);

        // Assert
        assertEquals("COMPLETED", project.getStatus());
        verify(projectDao).updateProject(project);
    }

    @Test
    void approveProjectCompletion_noTeamLead() throws SQLException {
        // Arrange
        project.setTeamLeadId(null);
        when(projectDao.findByProjectId(1)).thenReturn(project);

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.approveProjectCompletion(1, manager)
        );

        // Assert
        assertEquals(
                "A Team Lead must be assigned before project completion.",
                ex.getMessage()
        );
    }

    @Test
    void deleteProject_success() throws SQLException {
        // Arrange
        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(projectDao.deleteProject(1)).thenReturn(1);

        // Act
        service.deleteProject(1, admin);

        // Assert
        verify(projectDao).deleteProject(1);
    }

    @Test
    void deleteProject_unauthorized() {
        // Arrange
        User user = manager;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.deleteProject(1, user)
        );

        // Assert
        assertEquals(
                "You are not authorized for this operation.",
                ex.getMessage()
        );

        verifyNoInteractions(projectDao);
    }

    @Test
    void invalidProjectId() {
        // Arrange
        int projectId = 0;

        // Act
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> {
                    try {
                        service.getProjectById(projectId, admin);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        // Assert
        assertEquals("ID must be greater than zero.", ex.getMessage());
    }
}