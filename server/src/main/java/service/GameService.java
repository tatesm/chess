package service;

import dataaccess.AuthTokenDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

import java.util.List;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthTokenDAO authTokenDAO;

    public GameService(GameDAO gameDAO, AuthTokenDAO authTokenDAO) {
        this.gameDAO = gameDAO;
        this.authTokenDAO = authTokenDAO;
    }

    public GameData createGame(String gameName, String playerColor, String username) {
        // Call GameDAO to create and store the game
        return gameDAO.createGame(gameName, username, playerColor);
    }

    public GameData joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        // Validate authToken and retrieve the associated username
        AuthData authData = authTokenDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid auth token");
        }
        String username = authData.username();  // Retrieve the username from the auth token

        // Retrieve the game using the gameID
        GameData game = gameDAO.getGame(gameID);

        if (game == null) {
            throw new DataAccessException("Game not found");
        }

        // Check if the requested slot (WHITE/BLACK) is available
        if (playerColor.equalsIgnoreCase("WHITE") && game.getWhiteUsername() == null) {
            game.setWhiteUsername(username);
        } else if (playerColor.equalsIgnoreCase("BLACK") && game.getBlackUsername() == null) {
            game.setBlackUsername(username);
        } else {
            throw new DataAccessException("Player slot is already taken");
        }

        // Update the game in the DAO
        gameDAO.updateGame(game);

        return game;  // Return the updated game data
    }

    public List<GameData> listGames() {
        return gameDAO.listGames();
    }
}

