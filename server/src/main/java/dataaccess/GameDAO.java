package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameDAO {

    private static final Map<Integer, GameData> GAMES = new HashMap<>();
    private static int nextGameId = 1;

    public GameData createGame(String gameName, String username, String playerColor) {
        int gameID = nextGameId++;
        GameData newGame = new GameData(gameID, gameName, null);
        GAMES.put(gameID, newGame);
        return newGame;
    }

    public GameData getGame(int gameID) {
        return GAMES.get(gameID);
    }

    public void clearGames() {
        GAMES.clear();
    }

    public void updateGame(GameData game) {
        GAMES.put(game.getGameID(), game);
    }

    public List<GameData> listGames() {
        return new ArrayList<>(GAMES.values());
    }
}

