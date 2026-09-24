package com.ptms.app.dao;

import java.util.List;

public interface IProjectMembers {

        void addProjectMember(ProjectMembers projectMember);
        void updateProjectMember(ProjectMembers projectMember);

        void deleteProjectMember(ProjectMembers projectMember);

        ProjectMembers getProjectMember(int id);

        List<ProjectMembers> getProjectMembers();

        List<ProjectMembers> getMembersByProjectId(int projectId);

        List<ProjectMembers> getProjectsByEmployeeId(int employeeId);
    }

