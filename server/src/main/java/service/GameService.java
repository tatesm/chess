package service;

import chess.ChessGame;
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
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Game name cannot be null or empty");
        }
        ChessGame newGame = new ChessGame(); // Initialize a new chess game
        return gameDAO.createGame(gameName, username, playerColor, newGame);
    }


    public GameData joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        // Validate auth token
        AuthData authData = authTokenDAO.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Invalid auth token");
        }

        // Retrieve game from DAO
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Game with ID " + gameID + " not found");
        }

        // Validate playerColor
        if (playerColor == null || (!playerColor.equalsIgnoreCase("WHITE") && !playerColor.equalsIgnoreCase("BLACK"))) {
            throw new DataAccessException("Player color must be 'WHITE' or 'BLACK'");
        }

        // Assign player to the game
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

        // Update the game in DAO
        gameDAO.updateGame(game);
        return game;
    }


    public List<GameData> listGames() throws DataAccessException {
        return gameDAO.listGames();
    }
}
