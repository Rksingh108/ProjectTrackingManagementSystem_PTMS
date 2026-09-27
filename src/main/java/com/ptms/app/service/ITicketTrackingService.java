package com.ptms.app.service;

import com.ptms.app.dao.ITicketTrackingDao;
import com.ptms.app.dao.TicketTrackingDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.model.TicketTracking;

import java.sql.SQLException;
import java.util.List;

public class ITicketTrackingService implements TicketTrackingService {

    private final TicketTrackingDao ticketTrackingDao;

    public ITicketTrackingService() {
        this.ticketTrackingDao = new ITicketTrackingDao();
    }

    public ITicketTrackingService(TicketTrackingDao ticketTrackingDao) {
        this.ticketTrackingDao = ticketTrackingDao;
    }

    @Override
    public TicketTracking getTrackingForTicket(int ticketId) throws SQLException {
        TicketTracking tracking = ticketTrackingDao.findByTicketId(ticketId);
        if (tracking == null) {
            throw new ResourceNotFoundException("No tracking record found for ticket id " + ticketId);
        }
        return tracking;
    }

    @Override
    public List<TicketTracking> getUpdatesByUser(int userId) throws SQLException {
        return ticketTrackingDao.findByUpdatedBy(userId);
    }
}
