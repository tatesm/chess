package ui;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebSocketFacade {
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
}
