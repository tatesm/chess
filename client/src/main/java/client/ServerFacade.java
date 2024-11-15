package client;

import com.google.gson.reflect.TypeToken;
import created.CreatedStuff;
import model.AuthData;
import model.GameData;
import model.UserData;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ServerFacade {

    private final String serverUrl;
    private final Gson gson;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl; // Base URL for the server
        this.gson = new Gson();
    }

    public AuthData register(String username, String password, String email) throws Exception {
        URL url = new URL(serverUrl + "/user");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        UserData userData = new UserData(username, password, email);
        String requestBody = gson.toJson(userData);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(requestBody);
            writer.flush();
        }

        handleError(connection);

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            return gson.fromJson(reader, AuthData.class); // Return AuthData object on success
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

        handleError(connection);

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            return gson.fromJson(reader, AuthData.class);
        }
    }

    public List<GameData> listGames(String authToken) throws Exception {
        URL url = new URL(serverUrl + "/game");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", authToken);
        handleError(connection);

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            return gson.fromJson(reader, new TypeToken<List<GameData>>() {
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

    public void joinGame(String authToken, int gameId, String playerColor) throws Exception {
        URL url = new URL(serverUrl + "/game");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", authToken);
        connection.setDoOutput(true);

        String requestBody = gson.toJson(new CreatedStuff.JoinGameRequest(gameId, playerColor));
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(requestBody);
            writer.flush();
        }

        handleError(connection);

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            reader.read(); // Consume response to ensure proper resource release
        }
    }

    public void makeMove(int gameId, String move, String authToken) throws Exception {
        URL url = new URL(serverUrl + "/game/" + gameId + "/move");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", authToken);  // Include auth token for security
        connection.setDoOutput(true);

        // Create JSON request with move details
        String requestBody = gson.toJson(move);
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(requestBody);
            writer.flush();
        }

        handleError(connection);

        // Optional: read the server's response (even if empty) to release resources
        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            reader.read();  // Just consume response to ensure connection resources are released
        }
    }


    public String getBoard(int gameId, String authToken) throws Exception {
        URL url = new URL(serverUrl + "/game/" + gameId + "/board");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", authToken);  // Pass authToken in header

        handleError(connection); // Method to handle errors

        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            return gson.fromJson(reader, String.class);  // Assuming board is returned as JSON string
        }
    }

    public void quitGame(int gameId, String authToken) throws Exception {
        URL url = new URL(serverUrl + "/game/" + gameId + "/quit");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Authorization", authToken);  // Pass authToken in header

        handleError(connection); // Method to handle errors
    }


    public void observeGame(String authToken, int gameId) throws Exception {
        URL url = new URL(serverUrl + "/game/" + gameId + "/observe");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", authToken); // Set the authorization token
        handleError(connection);

        // Optional: Read the response (if any) to ensure the connection closes properly
        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            reader.read(); // Simply consume the response
        }
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
                String errorResponse = gson.fromJson(reader, String.class);
                throw new Exception("Error: " + errorResponse);
            }
        }
    }
}

