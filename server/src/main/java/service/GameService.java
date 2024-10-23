package service;

import dataaccess.GameDAO;
import model.GameData;

public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public GameData createGame(String gameName, String username, String playerColor) {
        return gameDAO.createGame(gameName, username, playerColor);  // Pass all arguments to GameDAO
    }
}
