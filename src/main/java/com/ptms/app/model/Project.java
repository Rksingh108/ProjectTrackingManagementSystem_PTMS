package com.ptms.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Maps directly to the `projects` table.
 * manager_id / team_lead_id / client_id are stored here just as the FK ids
 * (Integer) rather than nested User/Client objects — keeps the DAO's
 * mapRow() simple. If a screen needs the manager's name etc., the service
 * layer joins that in, not this model.
 */
public class Project {

    private Integer id;              // null until saved (auto-increment in DB)
    private String name;
    private String requirements;
    private Integer managerId;       // FK -> users.id, required
    private Integer teamLeadId;      // FK -> users.id, nullable
    private Integer clientId;        // FK -> clients.id, nullable
    private String domain;
    private BigDecimal cost;
    private LocalDate startDate;
    private LocalDate deadline;
    private String priority;         // "LOW", "MEDIUM", "HIGH"
    private String status;           // e.g. "ACTIVE", "COMPLETED"

    public Project() {
    }

    public Project(String name, String requirements, Integer managerId, String priority) {
        this.name = name;
        this.requirements = requirements;
        this.managerId = managerId;
        this.priority = priority;
        this.status = "ACTIVE";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public Integer getTeamLeadId() {
        return teamLeadId;
    }

    public void setTeamLeadId(Integer teamLeadId) {
        this.teamLeadId = teamLeadId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Project{id=" + id +
                ", name='" + name + '\'' +
                ", managerId=" + managerId +
                ", status='" + status + '\'' +
                '}';
    }
}


