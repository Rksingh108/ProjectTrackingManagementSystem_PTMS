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
    private ProjectMemberDao projectMemberDao;

    @Mock
    private UserDao userDao;

    private IProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new IProjectService(
                projectDao,
                projectMemberDao,
                userDao
        );
    }

    @Test
    void shouldCreateProjectSuccessfullyForAdmin()
            throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        Project project = new Project();
        project.setName("PTMS");
        project.setManagerId(2);

        when(projectDao.insertProject(project))
                .thenAnswer(invocation -> {
                    project.setId(100);
                    return 1;
                });

        // Act
        Project result =
                projectService.createProject(project, admin);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getId());
        assertEquals("PTMS", result.getName());
        assertEquals(2, result.getManagerId());

        verify(projectDao).insertProject(project);

        verify(projectMemberDao).insertMember(
                argThat(member ->
                        member.getProjectId() == 100 &&
                                member.getUserId() == 2 &&
                                member.getRoleInProject()
                                        .equals("PROJECT_MANAGER")
                )
        );
    }

    @Test
    void shouldSetRequestingUserAsManagerWhenManagerIdIsNull()
            throws SQLException {

        // Arrange
        User manager = new User();
        manager.setId(5);
        manager.setRole(User.Role.PROJECT_MANAGER);

        Project project = new Project();
        project.setName("Employee Management");
        project.setManagerId(null);

        when(projectDao.insertProject(project))
                .thenAnswer(invocation -> {
                    project.setId(101);
                    return 1;
                });

        // Act
        Project result =
                projectService.createProject(project, manager);

        // Assert
        assertEquals(5, result.getManagerId());
        assertEquals(101, result.getId());

        verify(projectDao).insertProject(project);

        verify(projectMemberDao).insertMember(
                argThat(member ->
                        member.getProjectId() == 101 &&
                                member.getUserId() == 5 &&
                                member.getRoleInProject()
                                        .equals("PROJECT_MANAGER")
                )
        );
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUnauthorizedUserCreatesProject()
            throws SQLException {

        // Arrange
        User employee = new User();
        employee.setId(10);
        employee.setRole(User.Role.EMPLOYEE);

        Project project = new Project();
        project.setName("Unauthorized Project");

        // Act & Assert
        assertThrows(
                UnauthorizedException.class,
                () -> projectService.createProject(
                        project,
                        employee
                )
        );

        verifyNoInteractions(
                projectDao,
                projectMemberDao,
                userDao
        );
    }

    @Test
    void shouldGetProjectByIdSuccessfully()
            throws SQLException {

        // Arrange
        Project project = new Project();
        project.setId(1);
        project.setName("PTMS");

        when(projectDao.findByProjectId(1))
                .thenReturn(project);

        // Act
        Project result =
                projectService.getProjectById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("PTMS", result.getName());

        verify(projectDao).findByProjectId(1);
    }

    @Test
    void shouldThrowResourceNotFoundWhenProjectDoesNotExist()
            throws SQLException {

        // Arrange
        when(projectDao.findByProjectId(999))
                .thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> projectService.getProjectById(999)
                );

        assertEquals(
                "No project found with id 999",
                exception.getMessage()
        );

        verify(projectDao).findByProjectId(999);
    }

    @Test
    void shouldGetAllProjectsSuccessfully()
            throws SQLException {

        // Arrange
        Project project1 = new Project();
        project1.setId(1);
        project1.setName("PTMS");

        Project project2 = new Project();
        project2.setId(2);
        project2.setName("CRM");

        List<Project> expected =
                List.of(project1, project2);

        when(projectDao.findAll())
                .thenReturn(expected);

        // Act
        List<Project> result =
                projectService.getAllProjects();

        // Assert
        assertEquals(2, result.size());
        assertEquals(expected, result);

        verify(projectDao).findAll();
    }

    @Test
    void shouldGetProjectsForAdmin()
            throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        List<Project> expected =
                List.of(
                        new Project(),
                        new Project()
                );

        when(projectDao.findAll())
                .thenReturn(expected);

        // Act
        List<Project> result =
                projectService.getProjectsForUser(admin);

        // Assert
        assertEquals(expected, result);

        verify(projectDao).findAll();
        verifyNoInteractions(projectMemberDao);
    }

    @Test
    void shouldGetProjectsForProjectManager()
            throws SQLException {

        // Arrange
        User manager = new User();
        manager.setId(5);
        manager.setRole(User.Role.PROJECT_MANAGER);

        List<Project> expected =
                List.of(new Project());

        when(projectDao.findByManagerId(5))
                .thenReturn(expected);

        // Act
        List<Project> result =
                projectService.getProjectsForUser(manager);

        // Assert
        assertEquals(expected, result);

        verify(projectDao).findByManagerId(5);
        verifyNoInteractions(projectMemberDao);
    }

    @Test
    void shouldGetProjectsForTeamLead()
            throws SQLException {

        // Arrange
        User teamLead = new User();
        teamLead.setId(7);
        teamLead.setRole(User.Role.TEAM_LEAD);

        List<Project> expected =
                List.of(new Project());

        when(projectDao.findByTeamLeadId(7))
                .thenReturn(expected);

        // Act
        List<Project> result =
                projectService.getProjectsForUser(teamLead);

        // Assert
        assertEquals(expected, result);

        verify(projectDao).findByTeamLeadId(7);
        verifyNoInteractions(projectMemberDao);
    }

    @Test
    void shouldGetProjectsForTeamMember()
            throws SQLException {

        // Arrange
        User teamMember = new User();
        teamMember.setId(10);
        teamMember.setRole(User.Role.TEAM_MEMBER);

        ProjectMember membership1 =
                new ProjectMember(1, 10, "TEAM_MEMBER");

        ProjectMember membership2 =
                new ProjectMember(2, 10, "TEAM_MEMBER");

        Project project1 = new Project();
        project1.setId(1);

        Project project2 = new Project();
        project2.setId(2);

        when(projectMemberDao.findByUserId(10))
                .thenReturn(
                        List.of(membership1, membership2)
                );

        when(projectDao.findByProjectId(1))
                .thenReturn(project1);

        when(projectDao.findByProjectId(2))
                .thenReturn(project2);

        // Act
        List<Project> result =
                projectService.getProjectsForUser(teamMember);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());

        verify(projectMemberDao).findByUserId(10);
        verify(projectDao).findByProjectId(1);
        verify(projectDao).findByProjectId(2);
    }

    @Test
    void shouldIgnoreMissingProjectForTeamMember()
            throws SQLException {

        // Arrange
        User teamMember = new User();
        teamMember.setId(10);
        teamMember.setRole(User.Role.TEAM_MEMBER);

        ProjectMember membership =
                new ProjectMember(99, 10, "TEAM_MEMBER");

        when(projectMemberDao.findByUserId(10))
                .thenReturn(List.of(membership));

        when(projectDao.findByProjectId(99))
                .thenReturn(null);

        // Act
        List<Project> result =
                projectService.getProjectsForUser(teamMember);

        // Assert
        assertTrue(result.isEmpty());

        verify(projectMemberDao).findByUserId(10);
        verify(projectDao).findByProjectId(99);
    }

    @Test
    void shouldAssignTeamLeadSuccessfully()
            throws SQLException {

        // Arrange
        int projectId = 1;
        int teamLeadUserId = 20;

        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        User candidate = new User();
        candidate.setId(teamLeadUserId);
        candidate.setRole(User.Role.TEAM_LEAD);

        Project project = new Project();
        project.setId(projectId);
        project.setName("PTMS");
        project.setManagerId(5);

        when(userDao.findByUserId(teamLeadUserId))
                .thenReturn(candidate);

        when(projectDao.findByProjectId(projectId))
                .thenReturn(project);

        when(projectMemberDao.findByProjectId(projectId))
                .thenReturn(List.of());

        when(projectDao.updateProject(project))
                .thenReturn(1);

        // Act
        projectService.assignTeamLead(
                projectId,
                teamLeadUserId,
                admin
        );

        // Assert
        assertEquals(
                teamLeadUserId,
                project.getTeamLeadId()
        );

        verify(userDao).findByUserId(teamLeadUserId);
        verify(projectDao).findByProjectId(projectId);
        verify(projectDao).updateProject(project);

        verify(projectMemberDao).insertMember(
                argThat(member ->
                        member.getProjectId() == projectId &&
                                member.getUserId() == teamLeadUserId &&
                                member.getRoleInProject()
                                        .equals("TEAM_LEAD")
                )
        );
    }

    @Test
    void shouldNotAddTeamLeadAsMemberWhenAlreadyMember()
            throws SQLException {

        // Arrange
        int projectId = 1;
        int teamLeadUserId = 20;

        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        User candidate = new User();
        candidate.setId(teamLeadUserId);
        candidate.setRole(User.Role.TEAM_LEAD);

        Project project = new Project();
        project.setId(projectId);
        project.setManagerId(5);

        ProjectMember existingMember =
                new ProjectMember(
                        projectId,
                        teamLeadUserId,
                        "TEAM_MEMBER"
                );

        when(userDao.findByUserId(teamLeadUserId))
                .thenReturn(candidate);

        when(projectDao.findByProjectId(projectId))
                .thenReturn(project);

        when(projectMemberDao.findByProjectId(projectId))
                .thenReturn(List.of(existingMember));

        // Act
        projectService.assignTeamLead(
                projectId,
                teamLeadUserId,
                admin
        );

        // Assert
        assertEquals(
                teamLeadUserId,
                project.getTeamLeadId()
        );

        verify(projectDao).updateProject(project);

        verify(projectMemberDao, never())
                .insertMember(any(ProjectMember.class));
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUnauthorizedUserAssignsTeamLead()
            throws SQLException {

        // Arrange
        User employee = new User();
        employee.setId(10);
        employee.setRole(User.Role.EMPLOYEE);

        // Act & Assert
        assertThrows(
                UnauthorizedException.class,
                () -> projectService.assignTeamLead(
                        1,
                        20,
                        employee
                )
        );

        verifyNoInteractions(
                userDao,
                projectDao,
                projectMemberDao
        );
    }

    @Test
    void shouldThrowResourceNotFoundWhenTeamLeadUserDoesNotExist()
            throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        when(userDao.findByUserId(20))
                .thenReturn(null);

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.assignTeamLead(
                        1,
                        20,
                        admin
                )
        );

        verify(userDao).findByUserId(20);
        verify(projectDao, never()).findByProjectId(anyInt());
    }

    @Test
    void shouldThrowValidationExceptionWhenCandidateIsNotTeamLead()
            throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        User candidate = new User();
        candidate.setId(20);
        candidate.setRole(User.Role.EMPLOYEE);

        when(userDao.findByUserId(20))
                .thenReturn(candidate);

        // Act & Assert
        assertThrows(
                ValidationException.class,
                () -> projectService.assignTeamLead(
                        1,
                        20,
                        admin
                )
        );

        verify(userDao).findByUserId(20);
        verify(projectDao, never()).findByProjectId(anyInt());
    }

    @Test
    void shouldThrowResourceNotFoundWhenAssigningToNonExistingProject()
            throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        User candidate = new User();
        candidate.setId(20);
        candidate.setRole(User.Role.TEAM_LEAD);

        when(userDao.findByUserId(20))
                .thenReturn(candidate);

        when(projectDao.findByProjectId(99))
                .thenReturn(null);

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.assignTeamLead(
                        99,
                        20,
                        admin
                )
        );

        verify(userDao).findByUserId(20);
        verify(projectDao).findByProjectId(99);
        verify(projectDao, never()).updateProject(any());
    }

    @Test
    void shouldUpdateProjectSuccessfullyForAdmin()
            throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        Project project = new Project();
        project.setId(10);
        project.setName("Updated PTMS");
        project.setManagerId(5);

        when(projectDao.updateProject(project))
                .thenReturn(1);

        // Act
        projectService.updateProject(
                project,
                admin
        );

        // Assert
        verify(projectDao)
                .updateProject(project);
    }

    @Test
    void shouldUpdateProjectSuccessfullyForManagingProjectManager()
            throws SQLException {

        // Arrange
        User manager = new User();
        manager.setId(5);
        manager.setRole(User.Role.PROJECT_MANAGER);

        Project project = new Project();
        project.setId(10);
        project.setManagerId(5);
        project.setName("Updated Project");

        when(projectDao.updateProject(project))
                .thenReturn(1);

        // Act
        projectService.updateProject(
                project,
                manager
        );

        // Assert
        verify(projectDao)
                .updateProject(project);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenOtherManagerUpdatesProject()
            throws SQLException {

        // Arrange
        User manager = new User();
        manager.setId(20);
        manager.setRole(User.Role.PROJECT_MANAGER);

        Project project = new Project();
        project.setId(10);
        project.setManagerId(5);

        // Act & Assert
        assertThrows(
                UnauthorizedException.class,
                () -> projectService.updateProject(
                        project,
                        manager
                )
        );

        verifyNoInteractions(projectDao);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenTeamLeadUpdatesProject()
            throws SQLException {

        // Arrange
        User teamLead = new User();
        teamLead.setId(20);
        teamLead.setRole(User.Role.TEAM_LEAD);

        Project project = new Project();
        project.setId(10);
        project.setManagerId(5);

        // Act & Assert
        assertThrows(
                UnauthorizedException.class,
                () -> projectService.updateProject(
                        project,
                        teamLead
                )
        );

        verifyNoInteractions(projectDao);
    }

    @Test
    void shouldThrowResourceNotFoundWhenUpdateAffectsNoRows()
            throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        Project project = new Project();
        project.setId(99);
        project.setManagerId(5);

        when(projectDao.updateProject(project))
                .thenReturn(0);

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> projectService.updateProject(
                                project,
                                admin
                        )
                );

        assertEquals(
                "No project found with id 99 to update.",
                exception.getMessage()
        );

        verify(projectDao)
                .updateProject(project);
    }

    @Test
    void shouldDeleteProjectSuccessfullyForAdmin()
            throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        when(projectDao.deleteProject(10))
                .thenReturn(1);

        // Act
        projectService.deleteProject(
                10,
                admin
        );

        // Assert
        verify(projectDao)
                .deleteProject(10);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenNonAdminDeletesProject()
            throws SQLException {

        // Arrange
        User manager = new User();
        manager.setId(5);
        manager.setRole(User.Role.PROJECT_MANAGER);

        // Act & Assert
        assertThrows(
                UnauthorizedException.class,
                () -> projectService.deleteProject(
                        10,
                        manager
                )
        );

        verifyNoInteractions(projectDao);
    }

    @Test
    void shouldThrowResourceNotFoundWhenDeleteAffectsNoRows()
            throws SQLException {

        // Arrange
        User admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        when(projectDao.deleteProject(99))
                .thenReturn(0);

        // Act & Assert
        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> projectService.deleteProject(
                                99,
                                admin
                        )
                );

        assertEquals(
                "No project found with id 99 to delete.",
                exception.getMessage()
        );

        verify(projectDao)
                .deleteProject(99);
    }
}