package com.ptms.app.model;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * Maps directly to the `ticket_management` table.
 * Named `Ticket` rather than `TicketManagement` for readability — the class
 * name doesn't have to match the table name exactly.
 */
public class Ticket {
    private Integer id;              // null until saved (auto-increment in DB)
    private Integer projectId;       // FK -> projects.id
    private String title;
    private String description;
    private String priority;         // "LOW", "MEDIUM", "HIGH"
    private LocalDate deadline;
    private Integer assignedTo;      // FK -> users.id, nullable
    private LocalDateTime createdAt;
    private String status;           // "IN_DEVELOPMENT", "IN_PROGRESS", "IMPLEMENTED", "COMPLETED"
    public Ticket() {
    }
    public Ticket(Integer projectId, String title, String description, String priority) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = "IN_DEVELOPMENT";
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getProjectId() {
        return projectId;
    }
    public void setProjectId(Integer projectId) {
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
        return "Ticket{id=" + id +
                ", projectId=" + projectId +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }
}
