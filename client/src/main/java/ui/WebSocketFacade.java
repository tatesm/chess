package ui;

import client.ServerFacade;
import com.google.gson.Gson;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebSocketFacade {
    private final String serverUrl;
    private final Gson gson;
    private final ServerFacade serverFacade;

    public WebSocketFacade(String serverUrl, ServerFacade serverFacade) {
        this.serverUrl = serverUrl;
        this.serverFacade = serverFacade;
        this.gson = new Gson();

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
    }


    public boolean leaveGameObserver(int gameId, String authToken) throws Exception {

    }

    public boolean leaveGamePlayer(int gameId, String authToken) throws Exception {

    }

    public boolean resignGamePlayer(int gameId, String authToken) throws Exception {

    }


    public void observeGame(String authToken, int gameId, String perspective) throws Exception {
        // Validate perspective input
        if (!perspective.equalsIgnoreCase("white") && !perspective.equalsIgnoreCase("black")) {
            perspective = "white"; // Default perspective
        }

        // Fetch the board

        String board = serverFacade.getBoard(gameId, authToken, perspective);

        System.out.println("Observing Game #" + gameId + " as " + perspective);
        System.out.println(board);
    }
}
