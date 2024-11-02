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


    public GameData createGame(String gameName, String username, String playerColor) throws DataAccessException {
        return gameDAO.createGame(gameName, username, playerColor);
    }

    public GameData joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {


        AuthData authData = authTokenDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid auth token");
        }


        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }


        if (playerColor == null || (!playerColor.equalsIgnoreCase("WHITE") && !playerColor.equalsIgnoreCase("BLACK"))) {
            throw new DataAccessException("Invalid player color");
        }


        if (playerColor.equalsIgnoreCase("WHITE")) {
            if (game.getWhiteUsername() != null) {
                throw new DataAccessException("White player slot is already taken");
            }
            game.setWhiteUsername(authData.username());
        } else if (playerColor.equalsIgnoreCase("BLACK")) {
            if (game.getBlackUsername() != null) {
                throw new DataAccessException("Black player slot is already taken");
            }
            game.setBlackUsername(authData.username());
        }


        gameDAO.updateGame(game);
        return game;
    }

    public List<GameData> listGames() throws DataAccessException {
        return gameDAO.listGames();
    }
}

