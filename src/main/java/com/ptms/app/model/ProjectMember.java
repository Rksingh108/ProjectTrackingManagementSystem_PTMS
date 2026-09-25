package com.ptms.app.model;

import java.time.LocalDate;

/**
 * Represents a row in the `project_members` table.
 * Junction/resolver table for the many-to-many relationship
 * between users and projects. Composite key: (projectId, userId).
 */
public class ProjectMember {

    private int projectId;   // PK, FK -> projects.id
    private int userId;      // PK, FK -> users.id
    private LocalDate joinedAt;
    private String roleInProject;

    public ProjectMember() {
    }

    public ProjectMember(int projectId, int userId, LocalDate joinedAt, String roleInProject) {
        this.projectId = projectId;
        this.userId = userId;
        this.joinedAt = joinedAt;
        this.roleInProject = roleInProject;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDate joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getRoleInProject() {
        return roleInProject;
    }

    public void setRoleInProject(String roleInProject) {
        this.roleInProject = roleInProject;
    }

    @Override
    public String toString() {
        return "ProjectMember{" +
                "projectId=" + projectId +
                ", userId=" + userId +
                ", joinedAt=" + joinedAt +
                ", roleInProject='" + roleInProject + '\'' +
                '}';
    }
}