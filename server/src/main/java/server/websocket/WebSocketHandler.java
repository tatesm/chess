package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
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
        System.out.println("New connection established: " + session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> connectPlayer(command.getAuthToken(), command.getGameID(), session);
            case MAKE_MOVE -> handleMove(command.getAuthToken(), command.getGameID(), session);
            case LEAVE -> handleLeave(command.getAuthToken(), command.getGameID(), session);
            case RESIGN -> handleResign(command.getAuthToken(), command.getGameID(), session);
        }
    }


    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        connections.remove(session.toString());
        System.out.println("Connection closed: " + session + " due to " + reason);
    }

    private void connectPlayer(String authToken, Integer gameID, Session session) {
        try {
            connections.add(authToken, session); // Use authToken as the unique identifier
            Notification notification = new Notification(Notification.Type.CONNECT, "Player connected to game " + gameID);
            connections.broadcast(authToken, notification);
        } catch (IOException e) {
            System.err.println("Failed to connect player: " + e.getMessage());
        }
    }


    private void handleMove(String authToken, Integer gameID, Session session) {
        try {
            Notification notification = new Notification(Notification.Type.MOVE, "Move made in game " + gameID + " by " + authToken);
            connections.broadcast(authToken, notification);
        } catch (IOException e) {
            System.err.println("Failed to handle move: " + e.getMessage());
        }
    }


    private void disconnectPlayer(String playerName) throws IOException {
        connections.remove(playerName);
        Notification notification = new Notification(Notification.Type.DISCONNECT, playerName + " left the game.");
        connections.broadcast(playerName, notification);
    }

    private void notifyPlayer(Session session, String message) {
        try {
            session.getRemote().sendString(message);
        } catch (IOException e) {
            System.err.println("Failed to notify player: " + e.getMessage());
        }
    }

}

