package com.ptms.app.model;
import java.time.LocalDateTime;
/**
 * Maps directly to the `ticket_tracking` table.
 * ticketId is 1:1 with Ticket (unique + not null in the schema) — each
 * ticket has exactly one tracking record holding its current progress.
 */
public class TicketTracking {
    private Integer id;              // null until saved (auto-increment in DB)
    private Integer ticketId;        // FK -> ticket_management.id, UNIQUE (enforces 1:1)
    private String status;           // "IN_DEVELOPMENT", "IN_PROGRESS", "IMPLEMENTED", "COMPLETED"
    private int progress;            // 0-100
    private String comment;
    private Integer updatedBy;       // FK -> users.id, nullable
    private LocalDateTime updatedAt;
    public TicketTracking() {
    }
    public TicketTracking(Integer ticketId, String status, int progress, Integer updatedBy) {
        this.ticketId = ticketId;
        this.status = status;
        this.progress = progress;
        this.updatedBy = updatedBy;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getTicketId() {
        return ticketId;
    }
    public void setTicketId(Integer ticketId) {
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
    /** Clamps to 0-100 so a bad value can't slip past the CHECK constraint's intent even in memory. */
    public void setProgress(int progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100, got: " + progress);
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
        return "TicketTracking{id=" + id +
                ", ticketId=" + ticketId +
                ", status=" + status +
                ", progress=" + progress +
                '}';
    }
}
