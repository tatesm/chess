package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    private static Map<String, UserData> users = new HashMap<>();
    private static UserDAO instance = new UserDAO();

    public static UserDAO getInstance() {
        return instance;
    }

    public void clearUsers() {
        users.clear();
    }

    public void clearGames() {
    }

    public void createGame(String testGame, String testUser, Object o) {
    }
}
