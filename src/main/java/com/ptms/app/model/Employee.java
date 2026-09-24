package com.ptms.app.model;

public class Employee {

    private int id;
    private int userId;
    private String designation;
    private String department;

    public Employee() {
    }

    public Employee(int userId, String designation, String department) {
        this.userId = userId;
        this.designation = designation;
        this.department = department;
    }

    public Employee(int id, int userId, String designation, String department) {
        this.id = id;
        this.userId = userId;
        this.designation = designation;
        this.department = department;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", userId=" + userId +
                ", designation='" + designation + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}