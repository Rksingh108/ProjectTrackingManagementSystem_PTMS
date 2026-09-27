package com.ptms.app.service;

import com.ptms.app.dao.ITicketDao;
import com.ptms.app.dao.ITicketTrackingDao;
import com.ptms.app.dao.TicketDao;
import com.ptms.app.dao.TicketTrackingDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Ticket;
import com.ptms.app.model.TicketTracking;
import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ITicketService implements TicketService {

    private static final Logger logger = Logger.getLogger(ITicketService.class.getName());

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "IN_DEVELOPMENT", Set.of("IN_PROGRESS"),
            "IN_PROGRESS", Set.of("IMPLEMENTED"),
            "IMPLEMENTED", Set.of("COMPLETED", "IN_PROGRESS")
    );

    private final TicketDao ticketDao;
    private final TicketTrackingDao ticketTrackingDao;

    public ITicketService() {
        this.ticketDao = new ITicketDao();
        this.ticketTrackingDao = new ITicketTrackingDao();
    }

    public ITicketService(TicketDao ticketDao, TicketTrackingDao ticketTrackingDao) {
        this.ticketDao = ticketDao;
        this.ticketTrackingDao = ticketTrackingDao;
    }

    @Override
    public Ticket createTicket(Ticket ticket, User requestingUser) throws SQLException {
        validateUser(requestingUser);
        validateTicket(ticket);

        ticket.setStatus("IN_DEVELOPMENT");
        ticketDao.insertTicket(ticket);

        TicketTracking tracking = new TicketTracking(
                ticket.getId(),
                "IN_DEVELOPMENT",
                0,
                requestingUser.getId()
        );

        ticketTrackingDao.insertTicket(tracking);

        logger.info("Ticket created. ID=" + ticket.getId());

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
    public void assignTicket(
            int ticketId,
            int userId,
            User requestingUser
    ) throws SQLException {
        validateManagerAccess(requestingUser);
        validateId(ticketId, "Ticket ID");
        validateId(userId, "User ID");

        Ticket ticket = getTicketById(ticketId);
        ticket.setAssignedTo(userId);

        int rows = ticketDao.updateTicket(ticket);

        if (rows == 0) {
            throw new ValidationException("Ticket assignment failed.");
        }

        logger.info("Ticket " + ticketId + " assigned to user " + userId);
    }

    @Override
    public void advanceStatus(
            int ticketId,
            String newStatus,
            int progress,
            String comment,
            User requestingUser
    ) throws SQLException {
        validateId(ticketId, "Ticket ID");

        if (newStatus == null || newStatus.isBlank()) {
            throw new ValidationException("Status is required.");
        }

        newStatus = newStatus.trim().toUpperCase();

        if (progress < 0 || progress > 100) {
            throw new ValidationException("Progress must be between 0 and 100.");
        }

        Ticket ticket = getTicketById(ticketId);
        String currentStatus = ticket.getStatus();

        Set<String> allowed = ALLOWED_TRANSITIONS.get(currentStatus);

        if (allowed == null || !allowed.contains(newStatus)) {
            throw new ValidationException(
                    "Cannot move ticket from " + currentStatus + " to " + newStatus
            );
        }

        boolean isAdmin = requestingUser.getRole() == User.Role.ADMIN;
        boolean isProjectManager = requestingUser.getRole() == User.Role.PROJECT_MANAGER;
        boolean isTeamLead = requestingUser.getRole() == User.Role.TEAM_LEAD;
        boolean isReviewer = isAdmin || isProjectManager || isTeamLead;

        boolean isAssignee = ticket.getAssignedTo() != null
                && ticket.getAssignedTo().equals(requestingUser.getId());

        if (currentStatus.equals("IMPLEMENTED")) {
            if (!isReviewer) {
                throw new UnauthorizedException(
                        "Only Admin, Project Manager or Team Lead can approve or reject a ticket."
                );
            }
        } else if (!isAssignee && !isReviewer) {
            throw new UnauthorizedException(
                    "Only the assigned Team Member, Team Lead, Project Manager or Admin can update this ticket."
            );
        }

        if (newStatus.equals("COMPLETED")) {
            progress = 100;
        }

        ticket.setStatus(newStatus);

        int rows = ticketDao.updateTicket(ticket);

        if (rows == 0) {
            throw new ValidationException("Ticket status update failed.");
        }

        TicketTracking tracking = ticketTrackingDao.findByTicketId(ticketId);

        if (tracking == null) {
            throw new ResourceNotFoundException(
                    "Tracking record not found for ticket " + ticketId
            );
        }

        tracking.setStatus(newStatus);
        tracking.setProgress(progress);
        tracking.setComment(comment);
        tracking.setUpdatedBy(requestingUser.getId());

        ticketTrackingDao.updateTicket(tracking);

        logger.info(
                "Ticket " + ticketId
                        + " changed from " + currentStatus
                        + " to " + newStatus
        );
    }

    @Override
    public void deleteTicket(int ticketId, User requestingUser) throws SQLException {
        if (requestingUser == null) {
            throw new UnauthorizedException("User is not logged in.");
        }

        if (requestingUser.getRole() != User.Role.ADMIN
                && requestingUser.getRole() != User.Role.PROJECT_MANAGER) {
            throw new UnauthorizedException(
                    "Only Admin or Project Manager can delete tickets."
            );
        }

        validateId(ticketId, "Ticket ID");
        getTicketById(ticketId);

        int rows = ticketDao.deleteTicket(ticketId);

        if (rows == 0) {
            throw new ResourceNotFoundException(
                    "Ticket not found with id " + ticketId
            );
        }

        logger.info("Ticket deleted. ID=" + ticketId);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new UnauthorizedException("User is not logged in.");
        }
    }

    private void validateManagerAccess(User user) {
        validateUser(user);

        User.Role role = user.getRole();

        if (role != User.Role.ADMIN
                && role != User.Role.PROJECT_MANAGER
                && role != User.Role.TEAM_LEAD) {
            throw new UnauthorizedException(
                    "Only Admin, Project Manager or Team Lead can perform this operation."
            );
        }
    }

    private void validateId(int id, String field) {
        if (id <= 0) {
            throw new ValidationException(
                    field + " must be greater than 0."
            );
        }
    }

    private void validateTicket(Ticket ticket) {
        if (ticket == null) {
            throw new ValidationException("Ticket cannot be null.");
        }

        validateId(ticket.getProjectId(), "Project ID");

        if (ticket.getTitle() == null || ticket.getTitle().isBlank()) {
            throw new ValidationException("Ticket title is required.");
        }

        if (ticket.getTitle().length() > 100) {
            throw new ValidationException(
                    "Ticket title cannot exceed 100 characters."
            );
        }

        if (ticket.getPriority() == null || ticket.getPriority().isBlank()) {
            throw new ValidationException("Ticket priority is required.");
        }

        String priority = ticket.getPriority().trim().toUpperCase();

        if (!Set.of("LOW", "MEDIUM", "HIGH").contains(priority)) {
            throw new ValidationException(
                    "Priority must be LOW, MEDIUM or HIGH."
            );
        }

        ticket.setPriority(priority);
    }
}