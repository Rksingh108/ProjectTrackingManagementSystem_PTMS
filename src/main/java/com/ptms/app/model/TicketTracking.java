package com.ptms.app.model;

import java.time.LocalDateTime;

/**
 * Represents a row in the `ticket_tracking` table.
 * One-to-Many with ticket_management: each row is a single
 * progress/status update, giving a full history per ticket
 * rather than a single overwritten record.
 */
public class TicketTracking {

    private int id;
    private int ticketId;       // FK -> ticket_management.id
    private String status;      // IN_DEVELOPMENT, IN_PROGRESS, IMPLEMENTED, COMPLETED
    private int progress;       // 0-100
    private String comment;
    private Integer updatedBy;  // FK -> users.id
    private LocalDateTime updatedAt;

    public TicketTracking() {
    }

    public TicketTracking(int id, int ticketId, String status, int progress, String comment,
                          Integer updatedBy, LocalDateTime updatedAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.status = status;
        this.progress = progress;
        this.comment = comment;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    // Constructor for creating a new tracking entry before an id/updatedAt is assigned by the DB
    public TicketTracking(int ticketId, String status, int progress, String comment, Integer updatedBy) {
        this(0, ticketId, status, progress, comment, updatedBy, null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        this.progress = progress;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Integer updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "TicketTracking{" +
                "id=" + id +
                ", ticketId=" + ticketId +
                ", status='" + status + '\'' +
                ", progress=" + progress +
                ", comment='" + comment + '\'' +
                ", updatedBy=" + updatedBy +
                ", updatedAt=" + updatedAt +
                '}';
    }
}