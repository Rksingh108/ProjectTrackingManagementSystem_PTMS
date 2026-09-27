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

    // Allowed "from status" -> "to status" moves. Anything not listed here is rejected.
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "IN_DEVELOPMENT", Set.of("IN_PROGRESS"),
            "IN_PROGRESS",    Set.of("IMPLEMENTED"),
            "IMPLEMENTED",    Set.of("COMPLETED", "IN_PROGRESS") // approve, or send back for rework
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
        if (requestingUser.getRole() != User.Role.ADMIN
                && requestingUser.getRole() != User.Role.PROJECT_MANAGER
                && requestingUser.getRole() != User.Role.TEAM_LEAD) {
            throw new UnauthorizedException("Only an Admin, Project Manager, or Team Lead can create a ticket.");
        }

        ticket.setStatus("IN_DEVELOPMENT");
        ticketDao.insertTicket(ticket);

        // Every ticket gets exactly one tracking row (1:1), created alongside it.
        TicketTracking tracking = new TicketTracking(ticket.getId(), "IN_DEVELOPMENT", 0, requestingUser.getId());
        ticketTrackingDao.insertTicket(tracking);

        logger.info("Ticket created: " + ticket.getTitle() + " (id=" + ticket.getId() + ") by user id=" + requestingUser.getId());
        return ticket;
    }

    @Override
    public Ticket getTicketById(int id) throws SQLException {
        Ticket ticket = ticketDao.findByTicketId(id);
        if (ticket == null) {
            throw new ResourceNotFoundException("No ticket found with id " + id);
        }
        return ticket;
    }

    @Override
    public List<Ticket> getTicketsForProject(int projectId) throws SQLException {
        return ticketDao.findByProjectId(projectId);
    }

    @Override
    public List<Ticket> getTicketsForUser(int userId) throws SQLException {
        return ticketDao.findByAssignedTo(userId);
    }

    @Override
    public void assignTicket(int ticketId, int userId, User requestingUser) throws SQLException {
        if (requestingUser.getRole() != User.Role.ADMIN
                && requestingUser.getRole() != User.Role.PROJECT_MANAGER
                && requestingUser.getRole() != User.Role.TEAM_LEAD) {
            throw new UnauthorizedException("Only an Admin, Project Manager, or Team Lead can assign a ticket.");
        }
        Ticket ticket = getTicketById(ticketId);
        ticket.setAssignedTo(userId);
        ticketDao.updateTicket(ticket);
        logger.info("Ticket id=" + ticketId + " assigned to userId=" + userId + " by requestingUser id=" + requestingUser.getId());
    }

    @Override
    public void advanceStatus(int ticketId, String newStatus, int progress, String comment, User requestingUser) throws SQLException {
        Ticket ticket = getTicketById(ticketId);
        String currentStatus = ticket.getStatus();

        Set<String> allowedNext = ALLOWED_TRANSITIONS.get(currentStatus);
        if (allowedNext == null || !allowedNext.contains(newStatus)) {
            throw new ValidationException("Cannot move ticket from " + currentStatus + " to " + newStatus + ".");
        }

        boolean movingIntoReview = newStatus.equals("IMPLEMENTED") || newStatus.equals("IN_PROGRESS") && currentStatus.equals("IN_DEVELOPMENT");
        boolean isAssignee = requestingUser.getId().equals(ticket.getAssignedTo());
        boolean isReviewer = requestingUser.getRole() == User.Role.TEAM_LEAD
                || requestingUser.getRole() == User.Role.PROJECT_MANAGER
                || requestingUser.getRole() == User.Role.ADMIN;

        // The assignee drives the ticket forward day-to-day; only a lead/manager/admin
        // can approve IMPLEMENTED -> COMPLETED or send it back for rework.
        boolean isApprovalOrRejection = currentStatus.equals("IMPLEMENTED");
        if (isApprovalOrRejection && !isReviewer) {
            throw new UnauthorizedException("Only a Team Lead, Project Manager, or Admin can approve or send back a ticket.");
        }
        if (!isApprovalOrRejection && !isAssignee && !isReviewer) {
            throw new UnauthorizedException("Only the assigned Team Member (or a Lead/Manager/Admin) can update this ticket's status.");
        }

        ticket.setStatus(newStatus);
        ticketDao.updateTicket(ticket);

        // COMPLETED always implies 100% progress; otherwise trust the caller's value.
        int effectiveProgress = newStatus.equals("COMPLETED") ? 100 : progress;

        TicketTracking tracking = ticketTrackingDao.findByTicketId(ticketId);
        if (tracking == null) {
            throw new ResourceNotFoundException("No tracking record found for ticket id " + ticketId);
        }
        tracking.setStatus(newStatus);
        tracking.setProgress(effectiveProgress);
        tracking.setComment(comment);
        tracking.setUpdatedBy(requestingUser.getId());
        ticketTrackingDao.updateTicket(tracking);

        logger.info("Ticket id=" + ticketId + " moved " + currentStatus + " -> " + newStatus
                + " by user id=" + requestingUser.getId());
    }

    @Override
    public void deleteTicket(int ticketId, User requestingUser) throws SQLException {
        if (requestingUser.getRole() != User.Role.ADMIN && requestingUser.getRole() != User.Role.PROJECT_MANAGER) {
            throw new UnauthorizedException("Only an Admin or Project Manager can delete a ticket.");
        }
        // ticket_tracking has ON DELETE CASCADE from ticket_management, so deleting
        // the ticket also removes its tracking row automatically.
        int rows = ticketDao.deleteTicket(ticketId);
        if (rows == 0) {
            throw new ResourceNotFoundException("No ticket found with id " + ticketId + " to delete.");
        }
        logger.info("Ticket id=" + ticketId + " deleted by user id=" + requestingUser.getId());
    }
}
