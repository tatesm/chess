package dataaccess;

import model.GameData;

import java.util.HashMap;
import java.util.Map;

public class GameDAO {

    private static Map<Integer, GameData> games = new HashMap<>();
    private static int nextGameId = 1;  // To assign unique IDs to each new game

    // Method to create a new game and store it in the in-memory map
    public GameData createGame(String gameName, String username, String playerColor) {
        int gameID = nextGameId++;  // Generate a new unique game ID
        GameData newGame = new GameData(gameID, username, playerColor, gameName, null);  // Game object with a placeholder for the chess game
        games.put(gameID, newGame);  // Store the new game in the map
        return newGame;
    }

    // Method to retrieve a game by its ID
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    // Method to clear all games (useful for testing or resetting the system)
    public void clearGames() {
        games.clear();
    }

    public void updateGame(GameData game) {
        games.put(game.getGameID(), game);  // Update the game in the in-memory map
    }
}
