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
        return gameDAO.createGame(gameName, username, playerColor);
    }

    public GameData joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        AuthData authData = authTokenDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid auth token");
        }
        String username = authData.username();

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }

        if (playerColor.equalsIgnoreCase("WHITE") && game.getWhiteUsername() == null) {
            game.setWhiteUsername(username);
        } else if (playerColor.equalsIgnoreCase("BLACK") && game.getBlackUsername() == null) {
            game.setBlackUsername(username);
        } else {
            throw new DataAccessException("Player slot is already taken");
        }

        gameDAO.updateGame(game);

        return game;
    }

    public List<GameData> listGames() {
        return gameDAO.listGames();
    }
}


