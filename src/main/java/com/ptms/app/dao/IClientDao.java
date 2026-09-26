package com.ptms.app.dao;

import com.ptms.app.model.Client;
import com.ptms.app.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class IClientDao implements ClientDao {

    private static final Logger logger = Logger.getLogger(IClientDao.class.getName());

    private final String insertClient =
            "INSERT INTO clients (name, email, phone, company_name) VALUES (?, ?, ?, ?)";

    private final String findClientById =
            "SELECT * FROM clients WHERE id = ?";

    private final String findAllClients =
            "SELECT * FROM clients ORDER BY id";

    private final String searchClientByName =
            "SELECT * FROM clients WHERE name LIKE ? OR company_name LIKE ? ORDER BY id";

    private final String updateClient =
            "UPDATE clients SET name = ?, email = ?, phone = ?, company_name = ? WHERE id = ?";

    private final String deleteClient =
            "DELETE FROM clients WHERE id = ?";

    @Override
    public int insert(Client client) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertClient, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, client.getName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getCompanyName());

            int count = ps.executeUpdate();
            if (count > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        client.setId(keys.getInt(1));
                    }
                }
                logger.info("Client inserted successfully, id=" + client.getId());
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to insert client name=" + client.getName() + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Client findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findClientById)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }

        } catch (SQLException e) {
            logger.severe("Failed to fetch client id=" + id + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Client> findAll() throws SQLException {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(findAllClients);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clients.add(mapRow(rs));
            }
            return clients;

        } catch (SQLException e) {
            logger.severe("Failed to fetch all clients : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Client> searchByName(String keyword) throws SQLException {
        List<Client> clients = new ArrayList<>();
        String likePattern = "%" + keyword + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(searchClientByName)) {

            ps.setString(1, likePattern);
            ps.setString(2, likePattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapRow(rs));
                }
            }
            return clients;

        } catch (SQLException e) {
            logger.severe("Failed to search clients keyword=" + keyword + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int update(Client client) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateClient)) {

            ps.setString(1, client.getName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getCompanyName());
            ps.setInt(5, client.getId());

            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("Client updated successfully, id=" + client.getId());
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to update client id=" + client.getId() + " : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public int delete(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteClient)) {

            ps.setInt(1, id);
            int count = ps.executeUpdate();
            if (count > 0) {
                logger.info("Client deleted successfully, id=" + id);
            }
            return count;

        } catch (SQLException e) {
            logger.severe("Failed to delete client id=" + id + " : " + e.getMessage());
            throw e;
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setId(rs.getInt("id"));
        client.setName(rs.getString("name"));
        client.setEmail(rs.getString("email"));
        client.setPhone(rs.getString("phone"));
        client.setCompanyName(rs.getString("company_name"));
        return client;
    }
}
