package service;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public GameData createGame(String gameName, String playerColor, String username) {
        // Call GameDAO to create and store the game
        return gameDAO.createGame(gameName, username, playerColor);
    }

    public GameData joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        // Retrieve the game using the gameID
        GameData game = gameDAO.getGame(gameID);

        if (game == null) {
            throw new DataAccessException("Game not found");
        }

        // Check if the requested slot (WHITE/BLACK) is available
        if (playerColor.equalsIgnoreCase("WHITE") && game.getWhiteUsername() == null) {
            game.setWhiteUsername(authToken);  // Assuming authToken maps to the username
        } else if (playerColor.equalsIgnoreCase("BLACK") && game.getBlackUsername() == null) {
            game.setBlackUsername(authToken);
        } else {
            throw new DataAccessException("Player slot is already taken");
        }

        // Update the game in the DAO
        gameDAO.updateGame(game);

        return game;  // Return the updated game data
    }
}

