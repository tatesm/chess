package client;

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

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl; // Base URL for the server
        this.gson = new Gson();
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
        System.out.println("Response code: " + responseCode);
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


    public String getBoard(int gameId, String authToken, String playerColor) throws Exception {
        // Simulated board representation
        String[][] board = new String[][]{
                {EscapeSequences.BLACK_ROOK, EscapeSequences.BLACK_KNIGHT, EscapeSequences.BLACK_BISHOP,
                        EscapeSequences.BLACK_QUEEN, EscapeSequences.BLACK_KING, EscapeSequences.BLACK_BISHOP,
                        EscapeSequences.BLACK_KNIGHT, EscapeSequences.BLACK_ROOK},
                {EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN,
                        EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN,
                        EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN,
                        EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN,
                        EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN},
                {EscapeSequences.WHITE_ROOK, EscapeSequences.WHITE_KNIGHT, EscapeSequences.WHITE_BISHOP,
                        EscapeSequences.WHITE_QUEEN, EscapeSequences.WHITE_KING, EscapeSequences.WHITE_BISHOP,
                        EscapeSequences.WHITE_KNIGHT, EscapeSequences.WHITE_ROOK}
        };

        // Adjust board orientation based on the player's color
        if (playerColor.equalsIgnoreCase("black")) {
            board = reverseBoard(board);
        }
        return Helper.formatBoard(board); // Use formatBoard to build the display

        //NUKE this, use chessboard object
    }


    private String[][] reverseBoard(String[][] board) {
        String[][] reversedBoard = new String[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            reversedBoard[i] = board[board.length - 1 - i];
        }
        return reversedBoard;
    }


    public boolean quitGame(int gameId, String authToken) throws Exception {
        URL url = new URL(serverUrl + "/game/" + gameId + "/quit");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Authorization", authToken);  // Pass authToken in header

        try {
            handleError(connection); // Throws an exception if there's an error response
            return true; // Return true if no errors were encountered
        } catch (Exception e) {
            System.out.println("Failed to quit game: " + e.getMessage());
            return false; // Return false if an error occurs
        } finally {
            connection.disconnect(); // Ensure the connection is closed
        }
    }

    public void clearDatabase() throws Exception {
        URL url = new URL(serverUrl + "/db");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        handleError(connection);
        connection.disconnect();
    }


    public void observeGame(String authToken, int gameId, String perspective) throws Exception {
        // Validate perspective input
        if (!perspective.equalsIgnoreCase("white") && !perspective.equalsIgnoreCase("black")) {
            perspective = "white"; // Default perspective
        }

        // Fetch the board
        String board = getBoard(gameId, authToken, perspective);

        System.out.println("Observing Game #" + gameId + " as " + perspective);
        System.out.println(board);
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

