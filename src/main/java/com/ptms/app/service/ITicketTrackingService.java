package com.ptms.app.service;

import com.ptms.app.dao.ITicketTrackingDao;
import com.ptms.app.dao.TicketTrackingDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.TicketTracking;
import com.ptms.app.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class ITicketTrackingService implements TicketTrackingService {
    private static final Logger logger = LoggerFactory.getLogger(ITicketTrackingService.class);

    private final TicketTrackingDao ticketTrackingDao;

    public ITicketTrackingService() {
        this.ticketTrackingDao = new ITicketTrackingDao();
    }

    public ITicketTrackingService(TicketTrackingDao ticketTrackingDao) {
        this.ticketTrackingDao = ticketTrackingDao;
    }

    @Override
    public TicketTracking getTrackingForTicket(
            int ticketId,
            User requestingUser) throws SQLException {

        validateUser(requestingUser);
        validateId(ticketId, "Ticket ID");

        TicketTracking tracking = ticketTrackingDao.findByTicketId(ticketId);

        if (tracking == null) {
            logger.warn("Tracking not found for ticket {}", ticketId);
            throw new ResourceNotFoundException(
                    "Tracking not found for ticket " + ticketId
            );
        }

        logger.info("Tracking retrieved for ticket {}", ticketId);
        return tracking;
    }

    @Override
    public List<TicketTracking> getUpdatesByUser(
            int userId, User requestingUser) throws SQLException {

        validateUser(requestingUser);
        validateId(userId, "User ID");

        if (requestingUser.getRole() != User.Role.ADMIN &&
                requestingUser.getId() != userId) {

            logger.warn(
                    "Unauthorized tracking access. User: {}, Requested user: {}",
                    requestingUser.getId(),
                    userId
            );

            throw new UnauthorizedException(
                    "You can only view your own tracking updates."
            );
        }

        logger.info("Fetching tracking updates for user {}", userId);
        return ticketTrackingDao.findByUserId(userId);
    }

    private void validateUser(User user) {
        if (user == null || user.getRole() == null)
            throw new UnauthorizedException("Invalid requesting user.");
    }

    private void validateId(int id, String field) {
        if (id <= 0)
            throw new ValidationException(field + " must be greater than 0.");
    }
}