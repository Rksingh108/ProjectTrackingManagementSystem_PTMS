package com.ptms.app.dao;

import com.ptms.app.model.Employee;

import java.util.List;

public interface IEmployeeDao {
    void addEmployee(Employee employee);
    void updateEmployee(Employee employee);
    void deleteEmployee(Employee employee);
    Employee getEmployee(int id);
    Employee getEmployeeByUserId(int userId);
    List<Employee> getEmployees();
}
