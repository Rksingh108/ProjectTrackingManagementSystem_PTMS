package com.ptms.app.model;

public class Roles {

    private int id;
    private String name;

    public Roles() {
    }

    public Roles(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Roles(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Roles{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public String getRoleName() {
            return null;
    }

    public String getRoleDescription() {
        return null;
    }

    public void setRoleName(String roleName) {

    }

    public void setRoleDescription(String roleDescription) {

    }
}