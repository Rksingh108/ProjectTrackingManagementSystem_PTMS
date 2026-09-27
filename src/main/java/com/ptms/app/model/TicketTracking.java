package com.ptms.app.model;
import java.time.LocalDateTime;

public class TicketTracking {
    private Integer id;
    private Integer ticketId;
    private String status;
    private int progress;
    private String comment;
    private Integer updatedBy;
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
