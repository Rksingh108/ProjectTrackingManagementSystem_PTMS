package com.ptms.app.service;

import com.ptms.app.dao.IProjectDao;
import com.ptms.app.dao.IProjectMemberDao;
import com.ptms.app.dao.ITicketDao;
import com.ptms.app.dao.ITicketTrackingDao;
import com.ptms.app.dao.IUserDao;
import com.ptms.app.dao.ProjectDao;
import com.ptms.app.dao.ProjectMemberDao;
import com.ptms.app.dao.TicketDao;
import com.ptms.app.dao.TicketTrackingDao;
import com.ptms.app.dao.UserDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Project;
import com.ptms.app.model.ProjectMember;
import com.ptms.app.model.Ticket;
import com.ptms.app.model.TicketTracking;
import com.ptms.app.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ITicketService implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(ITicketService.class);

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "IN_DEVELOPMENT", Set.of("IN_PROGRESS"),
            "IN_PROGRESS", Set.of("IMPLEMENTED"),
            "IMPLEMENTED", Set.of("COMPLETED", "IN_PROGRESS"),
            "COMPLETED", Set.of()
    );

    private final TicketDao ticketDao;
    private final TicketTrackingDao ticketTrackingDao;
    private final ProjectDao projectDao;
    private final ProjectMemberDao projectMemberDao;
    private final UserDao userDao;

    public ITicketService() {
        this.ticketDao = new ITicketDao();
        this.ticketTrackingDao = new ITicketTrackingDao();
        this.projectDao = new IProjectDao();
        this.projectMemberDao = new IProjectMemberDao();
        this.userDao = new IUserDao();
    }

    public ITicketService(TicketDao ticketDao, TicketTrackingDao ticketTrackingDao) {
        this.ticketDao = ticketDao;
        this.ticketTrackingDao = ticketTrackingDao;
        this.projectDao = new IProjectDao();
        this.projectMemberDao = new IProjectMemberDao();
        this.userDao = new IUserDao();
    }

    @Override
    public Ticket createTicket(Ticket ticket, User requestingUser) throws SQLException {
        validateUser(requestingUser);
        validateTicket(ticket);

        if (requestingUser.getRole() != User.Role.TEAM_LEAD
                && requestingUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only TEAM_LEAD or ADMIN can create tickets.");
        }

        Project project = projectDao.findByProjectId(ticket.getProjectId());

        if (project == null) {
            throw new ResourceNotFoundException("Project not found.");
        }

        if (requestingUser.getRole() == User.Role.TEAM_LEAD
                && (project.getTeamLeadId() == null
                || !project.getTeamLeadId().equals(requestingUser.getId()))) {
            throw new UnauthorizedException("You can create tickets only for projects assigned to you.");
        }

        ticket.setStatus("IN_DEVELOPMENT");

        int rows = ticketDao.insertTicket(ticket);

        if (rows == 0) {
            throw new ValidationException("Ticket could not be created.");
        }

        TicketTracking tracking = new TicketTracking(
                ticket.getId(),
                "IN_DEVELOPMENT",
                0,
                requestingUser.getId()
        );

        tracking.setComment("Ticket created.");
        ticketTrackingDao.insertTicket(tracking);

        logger.info(
                "Ticket created. Ticket ID: {}, Project ID: {}, Created by: {}",
                ticket.getId(),
                ticket.getProjectId(),
                requestingUser.getId()
        );

        return ticket;
    }

    @Override
    public Ticket getTicketById(int ticketId) throws SQLException {
        validateId(ticketId, "Ticket ID");

        Ticket ticket = ticketDao.findByTicketId(ticketId);

        if (ticket == null) {
            throw new ResourceNotFoundException("Ticket not found with id " + ticketId);
        }

        return ticket;
    }

    @Override
    public List<Ticket> getTicketsForProject(int projectId) throws SQLException {
        validateId(projectId, "Project ID");
        return ticketDao.findByProjectId(projectId);
    }

    @Override
    public List<Ticket> getTicketsForUser(int userId) throws SQLException {
        validateId(userId, "User ID");
        return ticketDao.findByAssignedTo(userId);
    }

    @Override
    public void assignTicket(int ticketId, int userId, User requestingUser) throws SQLException {
        validateUser(requestingUser);

        if (requestingUser.getRole() != User.Role.TEAM_LEAD
                && requestingUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only TEAM_LEAD or ADMIN can assign tickets.");
        }

        validateId(ticketId, "Ticket ID");
        validateId(userId, "User ID");

        Ticket ticket = getTicketById(ticketId);
        Project project = projectDao.findByProjectId(ticket.getProjectId());

        if (project == null) {
            throw new ResourceNotFoundException("Project not found.");
        }

        if (requestingUser.getRole() == User.Role.TEAM_LEAD
                && (project.getTeamLeadId() == null
                || !project.getTeamLeadId().equals(requestingUser.getId()))) {
            throw new UnauthorizedException("You can assign tickets only for projects assigned to you.");
        }

        User assignedUser = userDao.findByUserId(userId);

        if (assignedUser == null) {
            throw new ResourceNotFoundException("User not found.");
        }

        if (assignedUser.getRole() != User.Role.TEAM_MEMBER) {
            throw new ValidationException("Tickets can only be assigned to TEAM_MEMBER.");
        }

        ProjectMember membership = projectMemberDao.findMembership(
                ticket.getProjectId(),
                userId
        );

        if (membership == null
                || !"TEAM_MEMBER".equalsIgnoreCase(membership.getRoleInProject())) {
            throw new ValidationException("Selected user is not a TEAM_MEMBER of this project.");
        }

        ticket.setAssignedTo(userId);

        int rows = ticketDao.updateTicket(ticket);

        if (rows == 0) {
            throw new ValidationException("Ticket assignment failed.");
        }

        logger.info(
                "Ticket assigned. Ticket ID: {}, User ID: {}, Assigned by: {}",
                ticketId,
                userId,
                requestingUser.getId()
        );
    }

    @Override
    public void advanceStatus(
            int ticketId,
            String newStatus,
            int progress,
            String comment,
            User requestingUser) throws SQLException {

        logger.info(
                "Status update requested. Ticket ID: {}, User ID: {}",
                ticketId,
                requestingUser != null ? requestingUser.getId() : null
        );

        validateUser(requestingUser);
        validateId(ticketId, "Ticket ID");

        if (newStatus == null || newStatus.isBlank()) {
            throw new ValidationException("Status is required.");
        }

        newStatus = newStatus.trim().toUpperCase();

        if (!Set.of("IN_PROGRESS", "IMPLEMENTED", "COMPLETED").contains(newStatus)) {
            throw new ValidationException("Invalid ticket status.");
        }

        if (progress < 0 || progress > 100) {
            throw new ValidationException("Progress must be between 0 and 100.");
        }

        Ticket ticket = getTicketById(ticketId);
        Project project = projectDao.findByProjectId(ticket.getProjectId());

        if (project == null) {
            throw new ResourceNotFoundException("Project not found.");
        }

        String currentStatus = ticket.getStatus().trim().toUpperCase();
        Set<String> allowedStatuses = ALLOWED_TRANSITIONS.get(currentStatus);

        if (allowedStatuses == null || !allowedStatuses.contains(newStatus)) {
            logger.warn(
                    "Invalid status transition. Ticket: {}, From: {}, To: {}",
                    ticketId,
                    currentStatus,
                    newStatus
            );

            throw new ValidationException(
                    "Cannot move ticket from " + currentStatus + " to " + newStatus
            );
        }

        User.Role role = requestingUser.getRole();
        boolean isProjectManager = role == User.Role.PROJECT_MANAGER;
        boolean isTeamLead = role == User.Role.TEAM_LEAD;
        boolean isTeamMember = role == User.Role.TEAM_MEMBER;

        boolean isAssignedTeamMember = isTeamMember
                && ticket.getAssignedTo() != null
                && ticket.getAssignedTo().equals(requestingUser.getId());

        if (isProjectManager) {
            if (project.getManagerId() == null
                    || !project.getManagerId().equals(requestingUser.getId())) {
                throw new UnauthorizedException(
                        "You can update tickets only for your own projects."
                );
            }
        } else if (isTeamLead) {
            if (project.getTeamLeadId() == null
                    || !project.getTeamLeadId().equals(requestingUser.getId())) {
                throw new UnauthorizedException(
                        "You can update tickets only for projects assigned to you."
                );
            }
        } else if (!isAssignedTeamMember) {
            throw new UnauthorizedException(
                    "Only the assigned Team Member, Team Lead or Project Manager can update ticket status."
            );
        }

        if ("COMPLETED".equals(newStatus)) {
            progress = 100;
        }

        ticket.setStatus(newStatus);

        int ticketRows = ticketDao.updateTicket(ticket);

        if (ticketRows == 0) {
            logger.error("Ticket status update failed. Ticket ID: {}", ticketId);
            throw new ValidationException("Ticket status update failed.");
        }

        TicketTracking tracking = new TicketTracking(
                ticketId,
                newStatus,
                progress,
                requestingUser.getId()
        );

        tracking.setComment(comment);
        ticketTrackingDao.insertTicket(tracking);

        logger.info(
                "Ticket status updated. Ticket ID: {}, Status: {}, Updated by: {}",
                ticketId,
                newStatus,
                requestingUser.getId()
        );
    }

    @Override
    public void deleteTicket(int ticketId, User requestingUser) throws SQLException {
        validateUser(requestingUser);

        if (requestingUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only ADMIN can delete tickets.");
        }

        getTicketById(ticketId);

        int rows = ticketDao.deleteTicket(ticketId);

        if (rows == 0) {
            throw new ResourceNotFoundException("Ticket could not be deleted.");
        }

        logger.info(
                "Ticket deleted. Ticket ID: {}, Deleted by: {}",
                ticketId,
                requestingUser.getId()
        );
    }

    private void validateUser(User user) {
        if (user == null || user.getRole() == null) {
            throw new UnauthorizedException("Invalid requesting user.");
        }
    }

    private void validateId(int id, String field) {
        if (id <= 0) {
            throw new ValidationException(field + " must be greater than zero.");
        }
    }

    private void validateTicket(Ticket ticket) {
        if (ticket == null) {
            throw new ValidationException("Ticket cannot be null.");
        }

        if (ticket.getProjectId() <= 0) {
            throw new ValidationException("Project ID is required.");
        }

        if (ticket.getTitle() == null || ticket.getTitle().isBlank()) {
            throw new ValidationException("Ticket title is required.");
        }

        if (ticket.getDescription() == null || ticket.getDescription().isBlank()) {
            throw new ValidationException("Ticket description is required.");
        }

        if (ticket.getPriority() == null || ticket.getPriority().isBlank()) {
            throw new ValidationException("Ticket priority is required.");
        }

        String priority = ticket.getPriority().trim().toUpperCase();

        if (!Set.of("LOW", "MEDIUM", "HIGH").contains(priority)) {
            throw new ValidationException("Priority must be LOW, MEDIUM or HIGH.");
        }

        ticket.setPriority(priority);
    }
}