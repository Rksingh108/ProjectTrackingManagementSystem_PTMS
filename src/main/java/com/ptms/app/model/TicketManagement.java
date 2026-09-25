package com.ptms.app.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a row in the `ticket_management` table.
 * A ticket is a unit of work belonging to a project, assigned
 * to a team member.
 */
public class TicketManagement {

    private int id;
    private int projectId;         // FK -> projects.id
    private String title;
    private String description;
    private String priority;       // e.g. LOW, MEDIUM, HIGH
    private LocalDate deadline;
    private Integer assignedTo;    // FK -> users.id (nullable until assigned)
    private LocalDateTime createdAt;
    private String status;         // IN_DEVELOPMENT, IN_PROGRESS, IMPLEMENTED, COMPLETED

    public TicketManagement() {
    }

    public TicketManagement(int id, int projectId, String title, String description, String priority,
                            LocalDate deadline, Integer assignedTo, LocalDateTime createdAt, String status) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.deadline = deadline;
        this.assignedTo = assignedTo;
        this.createdAt = createdAt;
        this.status = status;
    }

    // Constructor for creating a new ticket before an id/createdAt is assigned by the DB
    public TicketManagement(int projectId, String title, String description, String priority,
                            LocalDate deadline, Integer assignedTo, String status) {
        this(0, projectId, title, description, priority, deadline, assignedTo, null, status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public Integer getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Integer assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TicketManagement{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", priority='" + priority + '\'' +
                ", deadline=" + deadline +
                ", assignedTo=" + assignedTo +
                ", createdAt=" + createdAt +
                ", status='" + status + '\'' +
                '}';
    }
}