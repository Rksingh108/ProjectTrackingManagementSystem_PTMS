package com.ptms.app.service;

import com.ptms.app.dao.ClientDao;
import com.ptms.app.dao.IClientDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.model.Client;
import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class IClientService implements ClientService {

    private static final Logger logger = Logger.getLogger(IClientService.class.getName());

    private final ClientDao clientDao;

    public IClientService() {
        this.clientDao = new IClientDao();
    }

    public IClientService(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    @Override
    public Client addClient(Client client, User requestingUser) throws SQLException {
        requireAdminOrManager(requestingUser, "add a client");
        clientDao.insert(client);
        logger.info("Client added: " + client.getName() + " by user id=" + requestingUser.getId());
        return client;
    }

    @Override
    public Client getClientById(int id) throws SQLException {
        Client client = clientDao.findById(id);
        if (client == null) {
            throw new ResourceNotFoundException("No client found with id " + id);
        }
        return client;
    }

    @Override
    public List<Client> getAllClients() throws SQLException {
        return clientDao.findAll();
    }

    @Override
    public List<Client> searchClients(String keyword) throws SQLException {
        return clientDao.searchByName(keyword);
    }

    @Override
    public void updateClient(Client client, User requestingUser) throws SQLException {
        requireAdminOrManager(requestingUser, "update a client");
        int rows = clientDao.update(client);
        if (rows == 0) {
            throw new ResourceNotFoundException("No client found with id " + client.getId() + " to update.");
        }
        logger.info("Client updated id=" + client.getId() + " by user id=" + requestingUser.getId());
    }

    @Override
    public void deleteClient(int id, User requestingUser) throws SQLException {
        requireAdminOrManager(requestingUser, "delete a client");
        int rows = clientDao.delete(id);
        if (rows == 0) {
            throw new ResourceNotFoundException("No client found with id " + id + " to delete.");
        }
        logger.info("Client deleted id=" + id + " by user id=" + requestingUser.getId());
    }

    private void requireAdminOrManager(User requestingUser, String action) {
        if (requestingUser.getRole() != User.Role.ADMIN && requestingUser.getRole() != User.Role.PROJECT_MANAGER) {
            throw new UnauthorizedException("Only an Admin or Project Manager can " + action + ".");
        }
    }
}
