package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import created.CreatedStuff;
import model.AuthData;
import model.GameData;
import model.UserData;
import com.google.gson.Gson;
import ui.EscapeSequences;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson;

    private ChessBoard chessBoard; // ChessBoard instance

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
        this.gson = new Gson();
        this.chessBoard = new ChessBoard();
        this.chessBoard.resetBoard(); // Initialize board with default pieces
    }


    public AuthData register(String username, String password, String email) throws Exception {
        URL url = new URL(serverUrl + "/user");  // Ensure the correct server endpoint
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Create user data from inputs
        UserData userData = new UserData(username, password, email);
        String requestBody = gson.toJson(userData);

        // Send request to the server
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(requestBody);
            writer.flush();
        }

        // Check for specific error responses
        if (connection.getResponseCode() == 403) {  // Username already taken
            try (InputStreamReader errorReader = new InputStreamReader(connection.getErrorStream())) {
                ErrorResponse errorResponse = gson.fromJson(errorReader, ErrorResponse.class);
                throw new Exception(errorResponse.getMessage());
            }
        }

        // Handle general errors
        handleError(connection);

        // Read the response body for successful registration
        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            return gson.fromJson(reader, AuthData.class);
        }
    }

    public AuthData login(String username, String password) throws Exception {
        URL url = new URL(serverUrl + "/session");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        UserData loginData = new UserData(username, password, null);
        String requestBody = gson.toJson(loginData);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(requestBody);
            writer.flush();
        }

        if (connection.getResponseCode() >= 400) {
            try (InputStreamReader errorReader = new InputStreamReader(connection.getErrorStream())) {

                Map<String, Object> errorResponse = gson.fromJson(errorReader, Map.class);
                System.out.println("Login failed: " + errorResponse.get("message"));
            }
            return null;
        }

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            return gson.fromJson(reader, AuthData.class);
        }
    }


    public List<GameData> listGames(String authToken) throws Exception {
        URL url = new URL(serverUrl + "/game");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", authToken);  // Set the auth token header

        handleError(connection);

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            JsonObject responseObject = gson.fromJson(reader, JsonObject.class);
            JsonArray gamesArray = responseObject.getAsJsonArray("games");
            return gson.fromJson(gamesArray, new TypeToken<List<GameData>>() {
            }.getType());
        }
    }


    public GameData createGame(String authToken, String gameName, String playerColor) throws Exception {
        URL url = new URL(serverUrl + "/game");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", authToken);
        connection.setDoOutput(true);

        String requestBody = gson.toJson(new CreatedStuff.CreateGameRequest(gameName, playerColor, "username"));
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(requestBody);
            writer.flush();
        }

        handleError(connection);

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            return gson.fromJson(reader, GameData.class);
        }
    }


    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        URL url = new URL(serverUrl + "/game");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Authorization", authToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        CreatedStuff.JoinGameRequest request = new CreatedStuff.JoinGameRequest(gameID, playerColor);
        String requestBody = gson.toJson(request);
        System.out.println("Sending request: " + requestBody);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(requestBody);
            writer.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode >= 400) {
            try (InputStreamReader errorReader = new InputStreamReader(connection.getErrorStream())) {
                ErrorResponse errorResponse = gson.fromJson(errorReader, ErrorResponse.class);
                throw new Exception("Server error: " + errorResponse.getMessage());
            }
        } else {
            System.out.println("Join game request successful.");
        }
    }


    // Define the ErrorResponse class in ServerFacade (or a shared package)
    private static class ErrorResponse {
        private String message;

        public String getMessage() {
            return message;
        }
    }


    public void clearDatabase() throws Exception {
        URL url = new URL(serverUrl + "/db");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        handleError(connection);
        connection.disconnect();
    }


    public void logout(String authToken) throws Exception {
        URL url = new URL(serverUrl + "/session");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Authorization", authToken);
        handleError(connection);
    }

    private void handleError(HttpURLConnection connection) throws Exception {
        if (connection.getResponseCode() >= 400) {
            try (InputStreamReader reader = new InputStreamReader(connection.getErrorStream())) {
                String errorResponse = connection.getResponseMessage();
                throw new Exception("Error: " + errorResponse);
            }
        }
    }
}

