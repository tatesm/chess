package service;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthTokenDAO;

public class ClearService {

    private UserDAO userDAO = new UserDAO();
    private GameDAO gameDAO = new GameDAO();
    private AuthTokenDAO authTokenDAO = AuthTokenDAO.getInstance();

    public void clearDatabase() throws DataAccessException {
        userDAO.clearUsers();
        gameDAO.clearGames();
        authTokenDAO.clearAuthTokens();
    }
}
