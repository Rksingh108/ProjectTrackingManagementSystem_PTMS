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
class IProjectMemberServiceTest {

    @Mock
    private ProjectMemberDao projectMemberDao;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private UserDao userDao;

    private IProjectMemberService service;

    @BeforeEach
    void setUp() {
        service = new IProjectMemberService(
                projectMemberDao,
                projectDao,
                userDao
        );
    }

    @Test
    void shouldAddMemberSuccessfully() throws SQLException {

        // Arrange
        int projectId = 1;
        int userId = 2;

        User requestingUser = new User();
        requestingUser.setId(10);
        requestingUser.setRole(User.Role.ADMIN);

        Project project = new Project();
        project.setManagerId(20);
        project.setTeamLeadId(30);

        User memberUser = new User();
        memberUser.setId(userId);

        when(projectDao.findByProjectId(projectId))
                .thenReturn(project);

        when(userDao.findByUserId(userId))
                .thenReturn(memberUser);

        when(projectMemberDao.findByProjectId(projectId))
                .thenReturn(List.of());

        // Act
        service.addMember(
                projectId,
                userId,
                "TEAM_MEMBER",
                requestingUser
        );

        // Assert
        verify(projectDao).findByProjectId(projectId);
        verify(userDao).findByUserId(userId);
        verify(projectMemberDao).findByProjectId(projectId);

        verify(projectMemberDao).insertMember(
                argThat(member ->
                        member.getProjectId() == projectId &&
                                member.getUserId() == userId &&
                                member.getRoleInProject().equals("TEAM_MEMBER")
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenProjectDoesNotExist()
            throws SQLException {

        // Arrange
        int projectId = 99;

        User requestingUser = new User();
        requestingUser.setId(10);
        requestingUser.setRole(User.Role.ADMIN);

        when(projectDao.findByProjectId(projectId))
                .thenReturn(null);

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.addMember(
                        projectId,
                        2,
                        "TEAM_MEMBER",
                        requestingUser
                )
        );

        verify(projectDao).findByProjectId(projectId);
        verifyNoInteractions(userDao, projectMemberDao);
    }

    @Test
    void shouldThrowUnauthorizedExceptionWhenUserCannotManageMembers()
            throws SQLException {

        // Arrange
        int projectId = 1;

        User requestingUser = new User();
        requestingUser.setId(50);
        requestingUser.setRole(User.Role.EMPLOYEE);

        Project project = new Project();
        project.setManagerId(20);
        project.setTeamLeadId(30);

        when(projectDao.findByProjectId(projectId))
                .thenReturn(project);

        // Act & Assert
        assertThrows(
                UnauthorizedException.class,
                () -> service.addMember(
                        projectId,
                        2,
                        "TEAM_MEMBER",
                        requestingUser
                )
        );

        verify(projectDao).findByProjectId(projectId);
        verifyNoInteractions(userDao, projectMemberDao);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist()
            throws SQLException {

        // Arrange
        int projectId = 1;
        int userId = 99;

        User requestingUser = new User();
        requestingUser.setId(10);
        requestingUser.setRole(User.Role.ADMIN);

        Project project = new Project();
        project.setManagerId(20);
        project.setTeamLeadId(30);

        when(projectDao.findByProjectId(projectId))
                .thenReturn(project);

        when(userDao.findByUserId(userId))
                .thenReturn(null);

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.addMember(
                        projectId,
                        userId,
                        "TEAM_MEMBER",
                        requestingUser
                )
        );

        verify(userDao).findByUserId(userId);
        verify(projectMemberDao, never()).insertMember(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenUserAlreadyMember()
            throws SQLException {

        // Arrange
        int projectId = 1;
        int userId = 2;

        User requestingUser = new User();
        requestingUser.setId(10);
        requestingUser.setRole(User.Role.ADMIN);

        Project project = new Project();
        project.setManagerId(20);
        project.setTeamLeadId(30);

        User memberUser = new User();
        memberUser.setId(userId);

        ProjectMember existingMember =
                new ProjectMember(projectId, userId, "TEAM_MEMBER");

        when(projectDao.findByProjectId(projectId))
                .thenReturn(project);

        when(userDao.findByUserId(userId))
                .thenReturn(memberUser);

        when(projectMemberDao.findByProjectId(projectId))
                .thenReturn(List.of(existingMember));

        // Act & Assert
        assertThrows(
                ValidationException.class,
                () -> service.addMember(
                        projectId,
                        userId,
                        "TEAM_MEMBER",
                        requestingUser
                )
        );

        verify(projectMemberDao, never()).insertMember(any());
    }

    @Test
    void shouldRemoveMemberSuccessfully()
            throws SQLException {

        // Arrange
        int projectId = 1;
        int userId = 2;

        User requestingUser = new User();
        requestingUser.setId(10);
        requestingUser.setRole(User.Role.ADMIN);

        Project project = new Project();

        when(projectDao.findByProjectId(projectId))
                .thenReturn(project);

        when(projectMemberDao.deleteMember(projectId, userId))
                .thenReturn(1);

        // Act
        service.removeMember(
                projectId,
                userId,
                requestingUser
        );

        // Assert
        verify(projectDao).findByProjectId(projectId);
        verify(projectMemberDao)
                .deleteMember(projectId, userId);
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonMember()
            throws SQLException {

        // Arrange
        int projectId = 1;
        int userId = 2;

        User requestingUser = new User();
        requestingUser.setId(10);
        requestingUser.setRole(User.Role.ADMIN);

        Project project = new Project();

        when(projectDao.findByProjectId(projectId))
                .thenReturn(project);

        when(projectMemberDao.deleteMember(projectId, userId))
                .thenReturn(0);

        // Act & Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.removeMember(
                        projectId,
                        userId,
                        requestingUser
                )
        );

        verify(projectMemberDao)
                .deleteMember(projectId, userId);
    }

    @Test
    void shouldReturnMembersOfProject()
            throws SQLException {

        // Arrange
        int projectId = 1;

        ProjectMember member =
                new ProjectMember(projectId, 2, "TEAM_MEMBER");

        List<ProjectMember> expected =
                List.of(member);

        when(projectMemberDao.findByProjectId(projectId))
                .thenReturn(expected);

        // Act
        List<ProjectMember> actual =
                service.getMembersOfProject(projectId);

        // Assert
        assertEquals(expected, actual);

        verify(projectMemberDao)
                .findByProjectId(projectId);
    }

    @Test
    void shouldReturnProjectsForMember()
            throws SQLException {

        // Arrange
        int userId = 2;

        List<ProjectMember> expected = List.of(
                new ProjectMember(1, userId, "TEAM_MEMBER"),
                new ProjectMember(2, userId, "DEVELOPER")
        );

        when(projectMemberDao.findByUserId(userId))
                .thenReturn(expected);

        // Act
        List<ProjectMember> actual =
                service.getProjectsForMember(userId);

        // Assert
        assertEquals(expected, actual);

        verify(projectMemberDao)
                .findByUserId(userId);
    }
}
