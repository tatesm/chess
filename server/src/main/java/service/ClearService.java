package service;

import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthTokenDAO;

public class ClearService {

    private UserDAO userDAO = new UserDAO();
    private GameDAO gameDAO = new GameDAO();
    private AuthTokenDAO authTokenDAO = AuthTokenDAO.getInstance();  // Use Singleton instance

    public void clearDatabase() {
        userDAO.clearUsers();
        gameDAO.clearGames();
        authTokenDAO.clearAuthTokens();  // Use the correct instance
    }
}
