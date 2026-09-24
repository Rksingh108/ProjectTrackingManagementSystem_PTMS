package com.ptms.app.dao;

import java.util.List;

class  ProjectMembers implements IProjectMembers
{

    @Override
    public void addProjectMember(ProjectMembers projectMember) {

    }

    @Override
    public void updateProjectMember(ProjectMembers projectMember) {

    }

    @Override
    public void deleteProjectMember(ProjectMembers projectMember) {

    }

    @Override
    public ProjectMembers getProjectMember(int id) {
        return null;
    }

    @Override
    public List<ProjectMembers> getProjectMembers() {
        return List.of();
    }

    @Override
    public List<ProjectMembers> getMembersByProjectId(int projectId) {
        return List.of();
    }

    @Override
    public List<ProjectMembers> getProjectsByEmployeeId(int employeeId) {
        return List.of();
    }
}