package com.ptms.app.service;

import com.ptms.app.model.Client;
import com.ptms.app.model.User;
import java.sql.SQLException;
import java.util.List;

public interface ClientService {
    Client addClient(Client client, User requestingUser) throws SQLException;
    Client getClientById(int id, User requestingUser) throws SQLException;
    List<Client> getAllClients(User requestingUser) throws SQLException;
    List<Client> searchClients(String keyword, User requestingUser) throws SQLException;
    void updateClient(Client client, User requestingUser) throws SQLException;
    void deleteClient(int id, User requestingUser) throws SQLException;
    int getTotalClients(User requestingUser) throws SQLException;
    int getTotalCompanies(User requestingUser) throws SQLException;
}