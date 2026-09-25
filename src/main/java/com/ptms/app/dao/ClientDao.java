package com.ptms.app.dao;

import com.ptms.app.model.Client;

import java.sql.SQLException;
import java.util.List;

public interface ClientDao {

    int insert(Client client) throws SQLException;

    Client findById(int id) throws SQLException;

    List<Client> findAll() throws SQLException;

    List<Client> searchByName(String keyword) throws SQLException;

    boolean update(Client client) throws SQLException;

    boolean delete(int id) throws SQLException;
}