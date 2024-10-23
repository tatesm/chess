package service;

import dataaccess.AuthTokenDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import dataaccess.DataAccessException;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthTokenDAO authTokenDAO;

    // Constructor that takes both UserDAO and AuthTokenDAO
    public UserService(UserDAO userDAO, AuthTokenDAO authTokenDAO) {
        this.userDAO = userDAO;
        this.authTokenDAO = authTokenDAO;
    }

    public AuthData register(UserData user) throws DataAccessException {
        userDAO.insertUser(user);
        String authToken = UUID.randomUUID().toString();
        authTokenDAO.createAuth(new AuthData(authToken, user.username()));  // Save auth token after registering
        return new AuthData(authToken, user.username());
    }

    public UserData getUser(String username) {
        return userDAO.getUser(username);
    }

    public AuthData login(UserData user) throws DataAccessException {
        UserData foundUser = userDAO.getUser(user.username());
        if (foundUser != null && foundUser.password().equals(user.password())) {
            String authToken = UUID.randomUUID().toString();  // Generate a token
            authTokenDAO.createAuth(new AuthData(authToken, user.username()));  // Save auth token
            return new AuthData(authToken, user.username());
        } else {
            throw new DataAccessException("Invalid credentials");
        }
    }
}