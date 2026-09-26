package com.ptms.app.model;

import java.time.LocalDateTime;

/**
 * Maps directly to the `project_members` table — the junction table
 * resolving the many-to-many between users and projects.
 * Its primary key is the (projectId, userId) pair, not a single id column,
 * so there's no separate `id` field here.
 */
public class ProjectMember {

    private Integer projectId;       // PK, FK -> projects.id
    private Integer userId;          // PK, FK -> users.id
    private String roleInProject;
    private LocalDateTime joinedAt;

    public ProjectMember() {
    }

    public ProjectMember(Integer projectId, Integer userId, String roleInProject) {
        this.projectId = projectId;
        this.userId = userId;
        this.roleInProject = roleInProject;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getRoleInProject() {
        return roleInProject;
    }

    public void setRoleInProject(String roleInProject) {
        this.roleInProject = roleInProject;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    @Override
    public String toString() {
        return "ProjectMember{projectId=" + projectId +
                ", userId=" + userId +
                ", roleInProject='" + roleInProject + '\'' +
                '}';
    }
}