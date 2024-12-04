package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("New connection established: " + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> connectPlayer(command.getAuthToken(), command.getGameID(), session);
            case MAKE_MOVE -> handleMove(command.getAuthToken(), command.getGameID());
            case LEAVE -> handleLeave(command.getAuthToken());
            case RESIGN -> handleResign(command.getAuthToken(), command.getGameID());
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String authToken = connections.getAuthTokenBySession(session);
        if (authToken != null) {
            connections.remove(authToken);
            System.out.println("Connection closed: " + authToken + " due to " + reason);
        }
    }

    private void connectPlayer(String authToken, Integer gameID, Session session) {
        try {
            connections.add(authToken, session);
            Notification notification = new Notification(Notification.Type.CONNECT, "Player connected to game " + gameID);
            connections.broadcast(authToken, notification);
        } catch (IOException e) {
            System.err.println("Failed to connect player: " + e.getMessage());
        }
    }

    private void handleMove(String authToken, Integer gameID) {
        try {
            Notification notification = new Notification(Notification.Type.MOVE, "Move made in game " + gameID + " by " + authToken);
            connections.broadcast(authToken, notification);
        } catch (IOException e) {
            System.err.println("Failed to handle move: " + e.getMessage());
        }
    }

    private void handleLeave(String authToken) {
        try {
            connections.remove(authToken);
            Notification notification = new Notification(Notification.Type.DISCONNECT, "Player " + authToken + " left the game.");
            connections.broadcast(authToken, notification);
            System.out.println("Player " + authToken + " has left the game.");
        } catch (IOException e) {
            System.err.println("Failed to handle leave for player: " + authToken + ". Error: " + e.getMessage());
        }
    }

    private void handleResign(String authToken, Integer gameID) {
        try {
            Notification notification = new Notification(Notification.Type.RESIGN, "Player " + authToken + " resigned from game " + gameID);
            connections.broadcast(authToken, notification);
            System.out.println("Player " + authToken + " has resigned from game " + gameID + ".");
        } catch (IOException e) {
            System.err.println("Failed to handle resign for player: " + authToken + ". Error: " + e.getMessage());
        }
    }
}
