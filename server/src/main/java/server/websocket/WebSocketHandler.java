package server.websocket;

import com.google.gson.Gson;
import chess.ChessGame;
import dataaccess.GameDAO;
import model.GameData;
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
    private final GameDAO gameDAO = new GameDAO(); // Ensure this is properly initialized
    private final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        String authToken = "some_token"; // Replace with actual extraction logic
        connections.add(authToken, session);
        System.out.println("New connection established: " + authToken);
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
            // Add root client to connections
            connections.add(authToken, session);

            // Retrieve the game data for the given game ID
            GameData gameData = gameDAO.getGame(gameID);

            // If game data doesn't exist, send an error message to the root client
            if (gameData == null) {
                ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
                connections.sendToRoot(authToken, errorMessage);
                return;
            }

            // Determine the player's color
            String playerColor;
            if (authToken.equals(gameData.getWhiteUsername())) {
                playerColor = "white";
            } else if (authToken.equals(gameData.getBlackUsername())) {
                playerColor = "black";
            } else {
                playerColor = "observer"; // Default to observer if not a player
            }

            // Send a LOAD_GAME message to the root client
            ServerMessage.LoadGameMessage loadGameMessage = new ServerMessage.LoadGameMessage(gameData);
            connections.sendToRoot(authToken, loadGameMessage);

            // Create and send a Notification message to all other clients
            String notificationMessage = authToken + " joined as " + playerColor;
            Notification notification = new Notification(notificationMessage);

            // Ensure root client is excluded from broadcast
            connections.broadcast(authToken, notification);

        } catch (Exception e) {
            System.err.println("Error connecting player: " + e.getMessage());
        }
    }


}
