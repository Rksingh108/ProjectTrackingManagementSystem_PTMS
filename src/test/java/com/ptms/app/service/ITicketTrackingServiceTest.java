package com.ptms.app.service;

import com.ptms.app.dao.TicketTrackingDao;
import com.ptms.app.model.TicketTracking;
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
class ITicketTrackingServiceTest {

    @Mock
    private TicketTrackingDao trackingDao;

    private ITicketTrackingService service;

    @BeforeEach
    void setUp() {
        service = new ITicketTrackingService(trackingDao);
    }

    @Test
    void shouldGetTrackingForTicket() throws SQLException {

        // Arrange
        TicketTracking tracking =
                new TicketTracking(1, "IN_PROGRESS", 50, 2);

        when(trackingDao.findByTicketId(1))
                .thenReturn(tracking);

        // Act
        TicketTracking result =
                service.getTrackingForTicket(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTicketId());
        assertEquals("IN_PROGRESS", result.getStatus());
        assertEquals(50, result.getProgress());

        verify(trackingDao).findByTicketId(1);
    }

    @Test
    void shouldGetUpdatesByUser() throws SQLException {

        // Arrange
        List<TicketTracking> expected = List.of(
                new TicketTracking(1, "IN_PROGRESS", 50, 2),
                new TicketTracking(2, "IMPLEMENTED", 90, 2)
        );

        when(trackingDao.findByUpdatedBy(2))
                .thenReturn(expected);

        // Act
        List<TicketTracking> result =
                service.getUpdatesByUser(2);

        // Assert
        assertEquals(2, result.size());
        assertEquals(expected, result);

        verify(trackingDao).findByUpdatedBy(2);
    }
}