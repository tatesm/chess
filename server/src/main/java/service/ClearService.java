package service;

import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;

public class ClearService {

    private UserDAO userDAO = new UserDAO();
    private GameDAO gameDAO = new GameDAO();
    private AuthDAO authDAO = new AuthDAO();

    public void clearDatabase() {
        userDAO.clearUsers();
        gameDAO.clearGames();
        authDAO.clearAuthTokens();
    }
}