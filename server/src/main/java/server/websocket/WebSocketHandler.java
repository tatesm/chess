package server.websocket;

import com.google.gson.Gson;
import chess.ChessGame;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import dataaccess.AuthTokenDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final GameDAO gameDAO = new GameDAO();
    private final AuthTokenDAO authDAO = new AuthTokenDAO();// Ensure this is properly initialized
    private final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("New connection established");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        var command = gson.fromJson(message, websocket.commands.UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connectPlayer(command.getAuthToken(), command.getGameID(), session);
            default -> System.out.println("Unhandled command type");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String authToken = connections.getAuthTokenBySession(session);
        if (authToken != null) {
            connections.remove(authToken);
            System.out.println("Connection closed: " + authToken + " Reason: " + reason);
        }
    }

    private void connectPlayer(String authToken, Integer gameID, Session session) {
        try {

            // Retrieve the game data for the given game ID
            GameData gameData = gameDAO.getGame(gameID);
            AuthData authData = authDAO.getAuth(authToken);

            // Handle invalid game ID
            if (gameData == null) {
                System.out.println("Invalid game ID: " + gameID);
                ServerMessage.ErrorMessage errorMessage = new ServerMessage.ErrorMessage("Invalid game ID.");
                connections.sendToRoot(authToken, errorMessage);
                return; // Stop further processing
            }

            // Validate authToken
            if (!authToken.equals(authData.authToken()) && !authToken.equals(gameData.getBlackUsername())) {
                System.out.println("Invalid auth token: " + authToken);
                ServerMessage.ErrorMessage errorMessage = new ServerMessage.ErrorMessage("Invalid auth token.");
                connections.sendToRoot(authToken, errorMessage);
                return;
            }
            connections.add(authToken, session);

            // Determine the player's color
            String playerColor = authData.username().equals(gameData.getWhiteUsername()) ? "white" :
                    authData.username().equals(gameData.getBlackUsername()) ? "black" : "observer";

            // Send a LOAD_GAME message to the root client
            ServerMessage.LoadGameMessage loadGameMessage = new ServerMessage.LoadGameMessage(gameData);
            System.out.println("Sending LOAD_GAME to root client: " + authToken);
            connections.sendToRoot(authToken, loadGameMessage);

            // Notify other clients (broadcast)
            String notificationMessage = gameData.getGameName() + " | " + authToken + " joined as " + playerColor;
            Notification notification = new Notification(notificationMessage);
            System.out.println("Broadcasting NOTIFICATION to other players.");
            connections.broadcast(authToken, notification);

        } catch (Exception e) {
            System.err.println("Error connecting player: " + e.getMessage());
            ServerMessage.ErrorMessage errorMessage = new ServerMessage.ErrorMessage("Server error: " + e.getMessage());
            connections.sendToRoot(authToken, errorMessage);
        }
    }


}