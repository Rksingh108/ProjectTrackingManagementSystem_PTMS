package com.ptms.app.service;

import com.ptms.app.dao.ClientDao;
import com.ptms.app.dao.IClientDao;
import com.ptms.app.exception.ResourceNotFoundException;
import com.ptms.app.exception.UnauthorizedException;
import com.ptms.app.exception.ValidationException;
import com.ptms.app.model.Client;
import com.ptms.app.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class IClientService implements ClientService {
    private static final Logger logger=LoggerFactory.getLogger(IClientService.class);
    private final ClientDao clientDao;
    private static final Pattern EMAIL_PATTERN=Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN=Pattern.compile("^[0-9]{10}$");

    public IClientService(){this.clientDao=new IClientDao();}
    public IClientService(ClientDao clientDao){this.clientDao=clientDao;}

    @Override
    public Client addClient(Client client,User requestingUser)throws SQLException{
        requireAdmin(requestingUser,"add a client");
        validateClient(client);
        logger.info("Adding client. User ID: {}, Client: {}",requestingUser.getId(),client.getName());
        try{
            int rows=clientDao.insertClient(client);
            if(rows==0){
                logger.warn("Client creation affected 0 rows. Client: {}",client.getName());
                throw new ValidationException("Client could not be created.");
            }
            logger.info("Client created successfully. Client ID: {}, Client: {}",client.getId(),client.getName());
            return client;
        }catch(SQLException e){
            logger.error("Database error while creating client. Client: {}",client.getName(),e);
            throw e;
        }
    }

    @Override
    public Client getClientById(int id,User requestingUser)throws SQLException{
        requireAdmin(requestingUser,"view client details");
        validateId(id);
        logger.info("Fetching client. Client ID: {}, User ID: {}",id,requestingUser.getId());
        try{
            Client client=clientDao.findByClientId(id);
            if(client==null){
                logger.warn("Client not found. Client ID: {}",id);
                throw new ResourceNotFoundException("No client found with id "+id);
            }
            logger.info("Client retrieved successfully. Client ID: {}",id);
            return client;
        }catch(SQLException e){
            logger.error("Database error while fetching client. Client ID: {}",id,e);
            throw e;
        }
    }

    @Override
    public List<Client> getAllClients(User requestingUser)throws SQLException{
        requireAdmin(requestingUser,"view clients");
        logger.info("Fetching all clients. User ID: {}",requestingUser.getId());
        try{
            List<Client> clients=clientDao.findAll();
            logger.info("All clients retrieved successfully. Result count: {}",clients.size());
            return clients;
        }catch(SQLException e){
            logger.error("Database error while fetching all clients",e);
            throw e;
        }
    }

    @Override
    public List<Client> searchClients(String keyword,User requestingUser)throws SQLException{
        requireAdmin(requestingUser,"search clients");
        if(keyword==null||keyword.trim().isEmpty()){
            logger.warn("Client search failed due to empty keyword. User ID: {}",requestingUser.getId());
            throw new ValidationException("Search keyword cannot be empty.");
        }
        String searchKeyword=keyword.trim();
        logger.info("Searching clients. Keyword: {}, User ID: {}",searchKeyword,requestingUser.getId());
        try{
            List<Client> clients=clientDao.searchClients(searchKeyword);
            logger.info("Client search completed. Keyword: {}, Result count: {}",searchKeyword,clients.size());
            return clients;
        }catch(SQLException e){
            logger.error("Database error while searching clients. Keyword: {}",searchKeyword,e);
            throw e;
        }
    }

    @Override
    public void updateClient(Client client,User requestingUser)throws SQLException{
        requireAdmin(requestingUser,"update a client");
        if(client==null)throw new ValidationException("Client cannot be null.");
        validateId(client.getId());
        validateClient(client);
        logger.info("Updating client. Client ID: {}, User ID: {}",client.getId(),requestingUser.getId());
        try{
            Client existingClient=clientDao.findByClientId(client.getId());
            if(existingClient==null){
                logger.warn("Client update failed. Client not found. Client ID: {}",client.getId());
                throw new ResourceNotFoundException("No client found with id "+client.getId());
            }
            int rows=clientDao.updateClient(client);
            if(rows==0)throw new ResourceNotFoundException("Client could not be updated.");
            logger.info("Client updated successfully. Client ID: {}",client.getId());
        }catch(SQLException e){
            logger.error("Database error while updating client. Client ID: {}",client.getId(),e);
            throw e;
        }
    }

    @Override
    public void deleteClient(int id,User requestingUser)throws SQLException{
        requireAdmin(requestingUser,"delete a client");
        validateId(id);
        logger.info("Deleting client. Client ID: {}, User ID: {}",id,requestingUser.getId());
        try{
            Client existingClient=clientDao.findByClientId(id);
            if(existingClient==null){
                logger.warn("Client deletion failed. Client not found. Client ID: {}",id);
                throw new ResourceNotFoundException("No client found with id "+id);
            }
            int rows=clientDao.deleteClient(id);
            if(rows==0)throw new ResourceNotFoundException("Client could not be deleted.");
            logger.info("Client deleted successfully. Client ID: {}",id);
        }catch(SQLException e){
            logger.error("Database error while deleting client. Client ID: {}",id,e);
            throw e;
        }
    }

    @Override
    public int getTotalClients(User requestingUser)throws SQLException{
        requireAdmin(requestingUser,"view client statistics");
        logger.info("Fetching total client count. User ID: {}",requestingUser.getId());
        try{
            int count=clientDao.countClients();
            logger.info("Total client count retrieved: {}",count);
            return count;
        }catch(SQLException e){
            logger.error("Database error while counting clients",e);
            throw e;
        }
    }

    @Override
    public int getTotalCompanies(User requestingUser)throws SQLException{
        requireAdmin(requestingUser,"view company statistics");
        logger.info("Fetching total company count. User ID: {}",requestingUser.getId());
        try{
            int count=clientDao.countCompanies();
            logger.info("Total company count retrieved: {}",count);
            return count;
        }catch(SQLException e){
            logger.error("Database error while counting companies",e);
            throw e;
        }
    }

    private void requireAdmin(User user,String action){
        if(user==null){
            logger.warn("Unauthorized client operation. User is not logged in. Action: {}",action);
            throw new UnauthorizedException("You must be logged in to access client management.");
        }
        if(user.getRole()!=User.Role.ADMIN){
            logger.warn("Unauthorized client operation. User ID: {}, Role: {}, Action: {}",user.getId(),user.getRole(),action);
            throw new UnauthorizedException("Only ADMIN can "+action+".");
        }
    }

    private void validateClient(Client client){
        if(client==null)throw new ValidationException("Client cannot be null.");
        validateName(client.getName());
        validateEmail(client.getEmail());
        validatePhone(client.getPhone());
        validateCompany(client.getCompanyName());
    }

    private void validateName(String name){
        if(name==null||name.trim().isEmpty())throw new ValidationException("Client name is required.");
        if(name.trim().length()<2)throw new ValidationException("Client name must contain at least 2 characters.");
        if(name.trim().length()>100)throw new ValidationException("Client name cannot exceed 100 characters.");
    }

    private void validateEmail(String email){
        if(email==null||email.trim().isEmpty())throw new ValidationException("Email is required.");
        if(!EMAIL_PATTERN.matcher(email.trim()).matches())throw new ValidationException("Please enter a valid email address.");
    }

    private void validatePhone(String phone){
        if(phone==null||phone.trim().isEmpty())throw new ValidationException("Phone number is required.");
        if(!PHONE_PATTERN.matcher(phone.trim()).matches())throw new ValidationException("Phone number must contain exactly 10 digits.");
    }

    private void validateCompany(String companyName){
        if(companyName==null||companyName.trim().isEmpty())throw new ValidationException("Company name is required.");
        if(companyName.trim().length()<2)throw new ValidationException("Company name must contain at least 2 characters.");
        if(companyName.trim().length()>150)throw new ValidationException("Company name cannot exceed 150 characters.");
    }

    private void validateId(int id){
        if(id<=0)throw new ValidationException("Client ID must be greater than zero.");
    }
}