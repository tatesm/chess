package service;

import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import dataaccess.DataAccessException;

import java.util.UUID;

public class UserService {
    private UserDAO userDAO = new UserDAO();

    public AuthData register(UserData user) throws DataAccessException {
        userDAO.insertUser(user);
        String authToken = UUID.randomUUID().toString();  // Generate a token
        return new AuthData(authToken, user.username());
    }

    public UserData getUser(String username) {
        return userDAO.getUser(username);
    }
}