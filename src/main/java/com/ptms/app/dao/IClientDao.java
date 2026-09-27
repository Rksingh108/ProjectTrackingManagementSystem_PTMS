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

    private static final String INSERT_CLIENT = "INSERT INTO clients (name, email, phone, company_name) VALUES (?, ?, ?, ?)";

    private static final String FIND_BY_ID = "SELECT id, name, email, phone, company_name " +
                    "FROM clients WHERE id = ?";

    private static final String FIND_ALL = "SELECT id, name, email, phone, company_name " +
                    "FROM clients ORDER BY id DESC";

    private static final String SEARCH_CLIENTS = "SELECT id, name, email, phone, company_name " +
                    "FROM clients " +
                    "WHERE name LIKE ? " +
                    "OR email LIKE ? " +
                    "OR phone LIKE ? " +
                    "OR company_name LIKE ? " +
                    "ORDER BY id DESC";

    private static final String UPDATE_CLIENT = "UPDATE clients " +
                    "SET name = ?, email = ?, phone = ?, company_name = ? " +
                    "WHERE id = ?";

    private static final String DELETE_CLIENT = "DELETE FROM clients WHERE id = ?";

    private static final String COUNT_CLIENTS = "SELECT COUNT(*) FROM clients";

    private static final String COUNT_COMPANIES = "SELECT COUNT(DISTINCT company_name) " +
                    "FROM clients " +
                    "WHERE company_name IS NOT NULL " +
                    "AND TRIM(company_name) <> ''";

    @Override
    public int insertClient(Client client) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     INSERT_CLIENT,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, client.getName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getCompanyName());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        client.setId(rs.getInt(1));
                    }
                }
            }

            return rows;
        }
    }

    @Override
    public Client findByClientId(int id) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<Client> findAll() throws SQLException {

        List<Client> clients = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clients.add(mapRow(rs));
            }
        }

        return clients;
    }

    @Override
    public List<Client> searchClients(String keyword) throws SQLException {

        List<Client> clients = new ArrayList<>();

        String pattern = "%" + keyword + "%";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(SEARCH_CLIENTS)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    clients.add(mapRow(rs));
                }
            }
        }

        return clients;
    }

    @Override
    public int updateClient(Client client) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_CLIENT)) {

            ps.setString(1, client.getName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getCompanyName());
            ps.setInt(5, client.getId());

            return ps.executeUpdate();
        }
    }

    @Override
    public int deleteClient(int id) throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE_CLIENT)) {

            ps.setInt(1, id);

            return ps.executeUpdate();
        }
    }

    @Override
    public int countClients() throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(COUNT_CLIENTS);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
    }

    @Override
    public int countCompanies() throws SQLException {

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(COUNT_COMPANIES);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 0;
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