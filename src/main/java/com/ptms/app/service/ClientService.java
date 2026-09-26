package com.ptms.app.service;

import com.ptms.app.model.Client;
import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;

public interface ClientService {

    Client addClient(Client client, User requestingUser) throws SQLException;

    Client getClientById(int id) throws SQLException;

    List<Client> getAllClients() throws SQLException;

    List<Client> searchClients(String keyword) throws SQLException;

    void updateClient(Client client, User requestingUser) throws SQLException;

    void deleteClient(int id, User requestingUser) throws SQLException;
}
