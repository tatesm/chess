package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameDAO {

    private static final Map<Integer, GameData> games = new HashMap<>();
    private static int nextGameId = 1;

    public GameData createGame(String gameName, String username, String playerColor) {
        int gameID = nextGameId++;
        GameData newGame = new GameData(gameID, gameName, null);
        games.put(gameID, newGame);
        return newGame;
    }

    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    public void clearGames() {
        games.clear();
    }

    public void updateGame(GameData game) {
        games.put(game.getGameID(), game);
    }

    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }
}

