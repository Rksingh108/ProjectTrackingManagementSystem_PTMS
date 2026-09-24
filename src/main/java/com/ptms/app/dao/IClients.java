package com.ptms.app.dao;

import com.ptms.app.model.Clients;

import java.util.List;

public interface IClients {
    void addClient(Clients client);
    void updateClient(Clients client);
    void deleteClient(int id);

    Clients getClientById(int id);
    Clients getClientByEmail(String email);
    List<Clients> getAllClients();
}
