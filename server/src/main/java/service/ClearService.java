package service;

import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthTokenDAO;

public class ClearService {

    private UserDAO userDAO = new UserDAO();
    private GameDAO gameDAO = new GameDAO();
    private AuthTokenDAO authTokenDAO = AuthTokenDAO.getInstance();

    public void clearDatabase() {
        userDAO.clearUsers();
        gameDAO.clearGames();
        authTokenDAO.clearAuthTokens();
    }
}
