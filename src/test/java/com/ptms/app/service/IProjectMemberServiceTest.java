package com.ptms.app.service;

import com.ptms.app.dao.ProjectDao;
import com.ptms.app.dao.ProjectMemberDao;
import com.ptms.app.dao.UserDao;
import com.ptms.app.exception.*;
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
    private ProjectMemberDao memberDao;

    @Mock
    private ProjectDao projectDao;

    @Mock
    private UserDao userDao;

    private IProjectMemberService service;
    private Project project;
    private User admin;
    private User teamLead;
    private User member;

    @BeforeEach
    void setUp() {
        // Arrange
        service = new IProjectMemberService(memberDao, projectDao, userDao);

        project = new Project();
        project.setId(1);
        project.setManagerId(10);
        project.setTeamLeadId(20);

        admin = new User();
        admin.setId(1);
        admin.setRole(User.Role.ADMIN);

        teamLead = new User();
        teamLead.setId(20);
        teamLead.setRole(User.Role.TEAM_LEAD);

        member = new User();
        member.setId(30);
        member.setRole(User.Role.TEAM_MEMBER);
    }

    @Test
    void addMember_success() throws SQLException {
        // Arrange
        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(userDao.findByUserId(30)).thenReturn(member);
        when(memberDao.findMembership(1, 30)).thenReturn(null);
        when(memberDao.insertMember(any(ProjectMember.class))).thenReturn(1);

        // Act
        service.addMember(1, 30, "TEAM_MEMBER", teamLead);

        // Assert
        verify(memberDao).insertMember(any(ProjectMember.class));
    }

    @Test
    void addMember_unauthorized() throws SQLException {
        // Arrange
        when(projectDao.findByProjectId(1)).thenReturn(project);

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.addMember(1, 30, "TEAM_MEMBER", member)
        );

        // Assert
        assertEquals(
                "Only ADMIN or TEAM_LEAD can manage project members.",
                ex.getMessage()
        );
        verifyNoInteractions(userDao);
    }

    @Test
    void addMember_userNotFound() throws SQLException {
        // Arrange
        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(userDao.findByUserId(30)).thenReturn(null);

        // Act
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.addMember(1, 30, "TEAM_MEMBER", teamLead)
        );

        // Assert
        assertEquals("User not found with ID: 30", ex.getMessage());
    }

    @Test
    void addMember_duplicate() throws SQLException {
        // Arrange
        ProjectMember existing = new ProjectMember(1, 30, "TEAM_MEMBER");

        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(userDao.findByUserId(30)).thenReturn(member);
        when(memberDao.findMembership(1, 30)).thenReturn(existing);

        // Act
        DuplicateResourceException ex = assertThrows(
                DuplicateResourceException.class,
                () -> service.addMember(1, 30, "TEAM_MEMBER", teamLead)
        );

        // Assert
        assertEquals(
                "User is already a member of this project.",
                ex.getMessage()
        );
    }

    @Test
    void removeMember_success() throws SQLException {
        // Arrange
        ProjectMember membership =
                new ProjectMember(1, 30, "TEAM_MEMBER");

        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(memberDao.findMembership(1, 30)).thenReturn(membership);
        when(memberDao.deleteMember(1, 30)).thenReturn(1);

        // Act
        service.removeMember(1, 30, teamLead);

        // Assert
        verify(memberDao).deleteMember(1, 30);
    }

    @Test
    void removeMember_teamLeadCannotBeRemoved() throws SQLException {
        // Arrange
        ProjectMember membership =
                new ProjectMember(1, 20, "TEAM_LEAD");

        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(memberDao.findMembership(1, 20)).thenReturn(membership);

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.removeMember(1, 20, teamLead)
        );

        // Assert
        assertEquals(
                "TEAM_LEAD cannot be removed using this operation.",
                ex.getMessage()
        );
        verify(memberDao, never()).deleteMember(anyInt(), anyInt());
    }

    @Test
    void getMembersOfProject_success() throws SQLException {
        // Arrange
        List<ProjectMember> members = List.of(
                new ProjectMember(1, 30, "TEAM_MEMBER")
        );

        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(memberDao.findByProjectId(1)).thenReturn(members);

        // Act
        List<ProjectMember> result =
                service.getMembersOfProject(1);

        // Assert
        assertEquals(1, result.size());
        verify(memberDao).findByProjectId(1);
    }

    @Test
    void getProjectsForMember_userNotFound() throws SQLException {
        // Arrange
        when(userDao.findByUserId(30)).thenReturn(null);

        // Act
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getProjectsForMember(30)
        );

        // Assert
        assertEquals("User not found with ID: 30", ex.getMessage());
    }

    @Test
    void getMyProjects_success() throws SQLException {
        // Arrange
        when(memberDao.findByUserId(30))
                .thenReturn(List.of(
                        new ProjectMember(1, 30, "TEAM_MEMBER")
                ));

        // Act
        List<ProjectMember> result =
                service.getMyProjects(member);

        // Assert
        assertEquals(1, result.size());
        verify(memberDao).findByUserId(30);
    }

    @Test
    void getMyProjects_nullUser() {
        // Arrange
        User user = null;

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.getMyProjects(user)
        );

        // Assert
        assertEquals("User must be logged in.", ex.getMessage());
        verifyNoInteractions(memberDao);
    }

    @Test
    void getProjectDetails_manager_success() throws SQLException {
        // Arrange
        User manager = new User();
        manager.setId(10);
        manager.setRole(User.Role.PROJECT_MANAGER);

        when(projectDao.findByProjectId(1)).thenReturn(project);

        // Act
        Project result =
                service.getProjectDetails(1, manager);

        // Assert
        assertEquals(project, result);
    }

    @Test
    void getProjectTeam_success() throws SQLException {
        // Arrange
        when(projectDao.findByProjectId(1)).thenReturn(project);
        when(memberDao.findByProjectId(1))
                .thenReturn(List.of(
                        new ProjectMember(1, 30, "TEAM_MEMBER")
                ));

        // Act
        List<ProjectMember> result =
                service.getProjectTeam(1, teamLead);

        // Assert
        assertEquals(1, result.size());
        verify(memberDao).findByProjectId(1);
    }

    @Test
    void getMyMembership_success() throws SQLException {
        // Arrange
        ProjectMember membership =
                new ProjectMember(1, 30, "TEAM_MEMBER");

        when(memberDao.findMembership(1, 30))
                .thenReturn(membership);

        // Act
        ProjectMember result =
                service.getMyMembership(1, member);

        // Assert
        assertEquals(membership, result);
    }

    @Test
    void getMyMembership_notMember() throws SQLException {
        // Arrange
        when(memberDao.findMembership(1, 30))
                .thenReturn(null);

        // Act
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> service.getMyMembership(1, member)
        );

        // Assert
        assertEquals(
                "You are not a member of this project.",
                ex.getMessage()
        );
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
                        service.getMembersOfProject(projectId);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        // Assert
        assertEquals("Invalid project ID.", ex.getMessage());
        verifyNoInteractions(projectDao);
    }
}
