package com.ptms.app.dao;

import com.ptms.app.model.Employee;
import com.ptms.app.util.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDao implements IEmployeeDao {

    @Override
    public void addEmployee(Employee employee) {
        String sql = """
                INSERT INTO employees (user_id, designation, department)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, employee.getUserId());
            statement.setString(2, employee.getDesignation());
            statement.setString(3, employee.getDepartment());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateEmployee(Employee employee) {
        String sql = """
                UPDATE employees
                SET user_id = ?, designation = ?, department = ?
                WHERE id = ?
                """;

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, employee.getUserId());
            statement.setString(2, employee.getDesignation());
            statement.setString(3, employee.getDepartment());
            statement.setInt(4, employee.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteEmployee(Employee employee) {
        String sql = "DELETE FROM employees WHERE id = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, employee.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Employee getEmployee(int id) {
        String sql = """
                SELECT id, user_id, designation, department
                FROM employees
                WHERE id = ?
                """;

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return mapEmployee(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Employee getEmployeeByUserId(int userId) {
        String sql = """
                SELECT id, user_id, designation, department
                FROM employees
                WHERE user_id = ?
                """;

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return mapEmployee(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Employee> getEmployees() {
        List<Employee> employees = new ArrayList<>();

        String sql = """
                SELECT id, user_id, designation, department
                FROM employees
                ORDER BY id
                """;

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                employees.add(mapEmployee(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("designation"),
                rs.getString("department")
        );
    }

    public static void main(String[] args) {
        EmployeeDao dao = new EmployeeDao();

        Employee employee = new Employee(
                101,
                "Developer",
                "IT"
        );

        dao.addEmployee(employee);
        System.out.println("Employee added successfully");
    }
}