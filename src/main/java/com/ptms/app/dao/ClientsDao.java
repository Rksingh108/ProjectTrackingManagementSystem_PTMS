package com.ptms.app.dao;

import com.ptms.app.model.Clients;
import com.ptms.app.util.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientsDao implements IClients {

    @Override
    public void addClient(Clients client) {

        final String sql = "INSERT INTO clients (name, email, phone, company_name) VALUES (?, ?, ?, ?)";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, client.getName());
            statement.setString(2, client.getEmail());
            statement.setString(3, client.getPhone());
            statement.setString(4, client.getCompanyName());

            int rows = statement.executeUpdate();

            if (rows > 0) {

                try (ResultSet resultSet = statement.getGeneratedKeys()) {

                    if (resultSet.next()) {
                        client.setId(resultSet.getInt(1));
                    }
                }

                System.out.println("Client added successfully.");

            } else {
                System.out.println("Client was not added.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Clients getClientById(int id) {

        String sql = "SELECT id, name, email, phone, company_name FROM clients WHERE id = ? ";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapClient(resultSet);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Clients getClientByEmail(String email) {

        String sql = "SELECT id, name, email, phone, company_name FROM clients WHERE email = ? ";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapClient(resultSet);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Clients> getAllClients() {

        List<Clients> clients = new ArrayList<>();

        String sql = "SELECT id, name, email, phone, company_name FROM clients ORDER BY id ";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                clients.add(mapClient(resultSet));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clients;
    }

    @Override
    public void updateClient(Clients client) {

        String sql = "UPDATE clients SET name = ?, email = ?, phone = ?, company_name = ? WHERE id = ? ";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, client.getName());
            statement.setString(2, client.getEmail());
            statement.setString(3, client.getPhone());
            statement.setString(4, client.getCompanyName());
            statement.setInt(5, client.getId());

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Client updated successfully.");
            } else {
                System.out.println("Client not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteClient(int id) {

        String sql = "DELETE FROM clients WHERE id = ? ";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                System.out.println("Client deleted successfully.");
            } else {
                System.out.println("Client not found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Clients mapClient(ResultSet resultSet) throws SQLException {

        Clients client = new Clients();

        client.setId(resultSet.getInt("id"));
        client.setName(resultSet.getString("name"));
        client.setEmail(resultSet.getString("email"));
        client.setPhone(resultSet.getString("phone"));
        client.setCompanyName(resultSet.getString("company_name"));

        return client;
    }
}