package com.ptms.app.dao;

import com.ptms.app.model.Client;
import com.ptms.app.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class IClientDao implements ClientDao {
    private static final Logger logger=LoggerFactory.getLogger(IClientDao.class);
    private static final String INSERT_CLIENT="INSERT INTO clients (name,email,phone,company_name) VALUES (?,?,?,?)";
    private static final String FIND_BY_ID="SELECT id,name,email,phone,company_name FROM clients WHERE id=?";
    private static final String FIND_ALL="SELECT id,name,email,phone,company_name FROM clients ORDER BY id DESC";
    private static final String SEARCH_CLIENTS="SELECT id,name,email,phone,company_name FROM clients WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? OR company_name LIKE ? ORDER BY id DESC";
    private static final String UPDATE_CLIENT="UPDATE clients SET name=?,email=?,phone=?,company_name=? WHERE id=?";
    private static final String DELETE_CLIENT="DELETE FROM clients WHERE id=?";
    private static final String COUNT_CLIENTS="SELECT COUNT(*) FROM clients";
    private static final String COUNT_COMPANIES="SELECT COUNT(DISTINCT company_name) FROM clients WHERE company_name IS NOT NULL AND TRIM(company_name)<>''";

    @Override
    public int insertClient(Client client)throws SQLException{
        try(Connection connection=DBConnection.getConnection();
            PreparedStatement ps=connection.prepareStatement(INSERT_CLIENT,Statement.RETURN_GENERATED_KEYS)){
            ps.setString(1,client.getName());
            ps.setString(2,client.getEmail());
            ps.setString(3,client.getPhone());
            ps.setString(4,client.getCompanyName());
            int rows=ps.executeUpdate();
            if(rows>0){
                try(ResultSet rs=ps.getGeneratedKeys()){
                    if(rs.next())client.setId(rs.getInt(1));
                }
                logger.info("Client inserted successfully. Client ID: {}",client.getId());
            }else{
                logger.warn("Client insert affected 0 rows. Client: {}",client.getName());
            }
            return rows;
        }catch(SQLException e){
            logger.error("Database error while inserting client: {}",client.getName(),e);
            throw e;
        }
    }

    @Override
    public Client findByClientId(int id)throws SQLException{
        try(Connection connection=DBConnection.getConnection();
            PreparedStatement ps=connection.prepareStatement(FIND_BY_ID)){
            ps.setInt(1,id);
            try(ResultSet rs=ps.executeQuery()){
                if(rs.next()){
                    Client client=mapRow(rs);
                    logger.info("Client retrieved successfully. Client ID: {}",id);
                    return client;
                }
            }
            logger.warn("Client not found. Client ID: {}",id);
        }catch(SQLException e){
            logger.error("Database error while retrieving client ID: {}",id,e);
            throw e;
        }
        return null;
    }

    @Override
    public List<Client> findAll()throws SQLException{
        List<Client> clients=new ArrayList<>();
        try(Connection connection=DBConnection.getConnection();
            PreparedStatement ps=connection.prepareStatement(FIND_ALL);
            ResultSet rs=ps.executeQuery()){
            while(rs.next())clients.add(mapRow(rs));
            logger.info("Retrieved all clients successfully. Result count: {}",clients.size());
        }catch(SQLException e){
            logger.error("Database error while retrieving all clients",e);
            throw e;
        }
        return clients;
    }

    @Override
    public List<Client> searchClients(String keyword)throws SQLException{
        List<Client> clients=new ArrayList<>();
        String pattern="%"+keyword+"%";
        try(Connection connection=DBConnection.getConnection();
            PreparedStatement ps=connection.prepareStatement(SEARCH_CLIENTS)){
            ps.setString(1,pattern);
            ps.setString(2,pattern);
            ps.setString(3,pattern);
            ps.setString(4,pattern);
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next())clients.add(mapRow(rs));
            }
            logger.info("Client search completed. Keyword: '{}', Result count: {}",keyword,clients.size());
        }catch(SQLException e){
            logger.error("Database error while searching clients. Keyword: {}",keyword,e);
            throw e;
        }
        return clients;
    }

    @Override
    public int updateClient(Client client)throws SQLException{
        try(Connection connection=DBConnection.getConnection();
            PreparedStatement ps=connection.prepareStatement(UPDATE_CLIENT)){
            ps.setString(1,client.getName());
            ps.setString(2,client.getEmail());
            ps.setString(3,client.getPhone());
            ps.setString(4,client.getCompanyName());
            ps.setInt(5,client.getId());
            int rows=ps.executeUpdate();
            if(rows>0)logger.info("Client updated successfully. Client ID: {}",client.getId());
            else logger.warn("Client update affected 0 rows. Client ID: {}",client.getId());
            return rows;
        }catch(SQLException e){
            logger.error("Database error while updating client ID: {}",client.getId(),e);
            throw e;
        }
    }

    @Override
    public int deleteClient(int id)throws SQLException{
        try(Connection connection=DBConnection.getConnection();
            PreparedStatement ps=connection.prepareStatement(DELETE_CLIENT)){
            ps.setInt(1,id);
            int rows=ps.executeUpdate();
            if(rows>0)logger.info("Client deleted successfully. Client ID: {}",id);
            else logger.warn("Client deletion affected 0 rows. Client ID: {}",id);
            return rows;
        }catch(SQLException e){
            logger.error("Database error while deleting client ID: {}",id,e);
            throw e;
        }
    }

    @Override
    public int countClients()throws SQLException{
        try(Connection connection=DBConnection.getConnection();
            PreparedStatement ps=connection.prepareStatement(COUNT_CLIENTS);
            ResultSet rs=ps.executeQuery()){
            if(rs.next()){
                int count=rs.getInt(1);
                logger.info("Client count retrieved successfully: {}",count);
                return count;
            }
        }catch(SQLException e){
            logger.error("Database error while counting clients",e);
            throw e;
        }
        return 0;
    }

    @Override
    public int countCompanies()throws SQLException{
        try(Connection connection=DBConnection.getConnection();
            PreparedStatement ps=connection.prepareStatement(COUNT_COMPANIES);
            ResultSet rs=ps.executeQuery()){
            if(rs.next()){
                int count=rs.getInt(1);
                logger.info("Company count retrieved successfully: {}",count);
                return count;
            }
        }catch(SQLException e){
            logger.error("Database error while counting companies",e);
            throw e;
        }
        return 0;
    }

    private Client mapRow(ResultSet rs)throws SQLException{
        Client client=new Client();
        client.setId(rs.getInt("id"));
        client.setName(rs.getString("name"));
        client.setEmail(rs.getString("email"));
        client.setPhone(rs.getString("phone"));
        client.setCompanyName(rs.getString("company_name"));
        return client;
    }
}