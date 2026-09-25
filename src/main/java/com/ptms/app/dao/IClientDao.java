package com.ptms.app.dao;

import com.ptms.app.model.Client;
import com.ptms.app.util.DataBaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IClientDao implements ClientDao {
    private final String insertClient = "INSERT INTO clients (name, email, phone, company_name) VALUES (?, ?, ?, ?)";
    private final String findByClientId = "SELECT * FROM clients WHERE id = ?";
    private final String findAllClientById = "SELECT * FROM clients ORDER BY id";
    private final String searchClientByName = "SELECT * FROM clients WHERE name LIKE ? OR company_name LIKE ? ORDER BY id";
    private final String updateClient = "UPDATE clients SET name = ?, email = ?, phone = ?, company_name = ? WHERE id = ?";
    private final String deleteClient = "DELETE FROM clients WHERE id = ?";


    @Override
    public int insert(Client client) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertClient, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, client.getName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getCompanyName());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    @Override
    public Client findById(int id) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findByClientId)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Client> findAll() throws SQLException {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findAllClientById);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clients.add(mapRow(rs));
            }
        }
        return clients;
    }

    @Override
    public List<Client> searchByName(String keyword) throws SQLException {

        List<Client> clients = new ArrayList<>();
        String pattern = "%" + keyword + "%";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(searchClientByName)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapRow(rs));
                }
            }
        }
        return clients;
    }

    @Override
    public boolean update(Client client) throws SQLException {


        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateClient)) {

            ps.setString(1, client.getName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getCompanyName());
            ps.setInt(5, client.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteClient)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("company_name")
        );
    }
}