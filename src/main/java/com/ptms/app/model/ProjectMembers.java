package com.ptms.app.model;

import java.time.LocalDateTime;

public class ProjectMembers {

    private int id;
    private int projectId;
    private int employeeId;
    private LocalDateTime joinedAt;

    public ProjectMembers() {
    }

    public ProjectMembers(
            int id,
            int projectId,
            int employeeId,
            LocalDateTime joinedAt) {

        this.id = id;
        this.projectId = projectId;
        this.employeeId = employeeId;
        this.joinedAt = joinedAt;
    }

    public ProjectMembers(int projectId, int employeeId) {
        this.projectId = projectId;
        this.employeeId = employeeId;
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

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    @Override
    public String toString() {
        return "ProjectMembers{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", employeeId=" + employeeId +
                ", joinedAt=" + joinedAt +
                '}';
    }
}