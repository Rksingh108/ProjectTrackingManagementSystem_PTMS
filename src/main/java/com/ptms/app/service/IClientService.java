package com.ptms.app.service;

import com.ptms.app.dao.ClientDao;
import com.ptms.app.dao.IClientDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Client;
import com.ptms.app.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class IClientService implements ClientService {

    private final ClientDao clientDao;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10}$");

    public IClientService() {
        this.clientDao = new IClientDao();
    }

    public IClientService(ClientDao clientDao) {
        this.clientDao = clientDao;
    }

    @Override
    public Client addClient(Client client, User requestingUser) throws SQLException {
        requireWriteAccess(requestingUser, "add a client");
        validateClient(client);

        int rows = clientDao.insertClient(client);

        if (rows == 0) {
            throw new ValidationException("Client could not be created.");
        }

        return client;
    }

    @Override
    public Client getClientById(int id, User requestingUser) throws SQLException {
        requireViewAccess(requestingUser);
        validateId(id);

        Client client = clientDao.findByClientId(id);

        if (client == null) {
            throw new ResourceNotFoundException(
                    "No client found with id " + id
            );
        }

        return client;
    }

    @Override
    public List<Client> getAllClients(User requestingUser) throws SQLException {
        requireViewAccess(requestingUser);
        return clientDao.findAll();
    }

    @Override
    public List<Client> searchClients(
            String keyword,
            User requestingUser
    ) throws SQLException {
        requireViewAccess(requestingUser);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ValidationException(
                    "Search keyword cannot be empty."
            );
        }

        return clientDao.searchClients(keyword.trim());
    }

    @Override
    public void updateClient(
            Client client,
            User requestingUser
    ) throws SQLException {
        requireWriteAccess(requestingUser, "update a client");

        if (client == null) {
            throw new ValidationException("Client cannot be null.");
        }

        validateId(client.getId());
        validateClient(client);

        Client existingClient = clientDao.findByClientId(client.getId());

        if (existingClient == null) {
            throw new ResourceNotFoundException(
                    "No client found with id " + client.getId()
            );
        }

        int rows = clientDao.updateClient(client);

        if (rows == 0) {
            throw new ResourceNotFoundException(
                    "Client could not be updated."
            );
        }
    }

    @Override
    public void deleteClient(
            int id,
            User requestingUser
    ) throws SQLException {
        requireWriteAccess(requestingUser, "delete a client");
        validateId(id);

        Client existingClient = clientDao.findByClientId(id);

        if (existingClient == null) {
            throw new ResourceNotFoundException(
                    "No client found with id " + id
            );
        }

        int rows = clientDao.deleteClient(id);

        if (rows == 0) {
            throw new ResourceNotFoundException(
                    "Client could not be deleted."
            );
        }
    }

    @Override
    public int getTotalClients(User requestingUser) throws SQLException {
        requireViewAccess(requestingUser);
        return clientDao.countClients();
    }

    @Override
    public int getTotalCompanies(User requestingUser) throws SQLException {
        requireViewAccess(requestingUser);
        return clientDao.countCompanies();
    }

    private void requireViewAccess(User user) {
        if (user == null) {
            throw new UnauthorizedException(
                    "You must be logged in to access client management."
            );
        }

        User.Role role = user.getRole();

        if (role == null) {
            throw new UnauthorizedException(
                    "User role is not defined."
            );
        }
    }

    private void requireWriteAccess(
            User user,
            String action
    ) {
        requireViewAccess(user);

        User.Role role = user.getRole();

        if (role != User.Role.ADMIN
                && role != User.Role.PROJECT_MANAGER) {
            throw new UnauthorizedException(
                    "Only ADMIN or PROJECT_MANAGER can " + action + "."
            );
        }
    }

    private void validateClient(Client client) {
        if (client == null) {
            throw new ValidationException(
                    "Client cannot be null."
            );
        }

        validateName(client.getName());
        validateEmail(client.getEmail());
        validatePhone(client.getPhone());
        validateCompany(client.getCompanyName());
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException(
                    "Client name is required."
            );
        }

        if (name.trim().length() < 2) {
            throw new ValidationException(
                    "Client name must contain at least 2 characters."
            );
        }

        if (name.trim().length() > 100) {
            throw new ValidationException(
                    "Client name cannot exceed 100 characters."
            );
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException(
                    "Email is required."
            );
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException(
                    "Please enter a valid email address."
            );
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException(
                    "Phone number is required."
            );
        }

        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            throw new ValidationException(
                    "Phone number must contain exactly 10 digits."
            );
        }
    }

    private void validateCompany(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new ValidationException(
                    "Company name is required."
            );
        }

        if (companyName.trim().length() < 2) {
            throw new ValidationException(
                    "Company name must contain at least 2 characters."
            );
        }

        if (companyName.trim().length() > 150) {
            throw new ValidationException(
                    "Company name cannot exceed 150 characters."
            );
        }
    }

    private void validateId(int id) {
        if (id <= 0) {
            throw new ValidationException(
                    "Client ID must be greater than zero."
            );
        }
    }
}