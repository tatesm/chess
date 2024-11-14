package client;

import model.AuthData;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public AuthData login(String username, String password) {
        // Send HTTP request to login endpoint
        return null;  // Return auth data if login succeeds
    }

    public AuthData register(String username, String password, String email) {
        // Send HTTP request to register endpoint
        return null;  // Return auth data if registration succeeds
    }

    public void logout() {
        // Send HTTP request to logout endpoint
    }

    public Game createGame(String gameName) {
        // Send HTTP request to create game endpoint
        return null;  // Return game data if create game succeeds
    }

    public List<Game> listGames() {
        // Send HTTP request to list games endpoint
        return new ArrayList<>();  // Return list of games
    }

    public void joinGame(int gameId, String color) {
        // Send HTTP request to join game endpoint
    }

    public void observeGame(int gameId) {
        // Send HTTP request to observe game endpoint
    }
}

