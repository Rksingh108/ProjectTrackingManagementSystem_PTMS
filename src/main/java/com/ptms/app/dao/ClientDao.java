package com.ptms.app.dao;
import com.ptms.app.model.Client;
import java.sql.SQLException;
import java.util.List;
public interface ClientDao {

    int insertClient(Client client) throws SQLException;
    Client findByClientId(int id) throws SQLException;
    List<Client> findAll() throws SQLException;
    List<Client> searchByClientName(String keyword) throws SQLException;
    int updateClient(Client client) throws SQLException;
    int deleteClient(int id) throws SQLException;

}