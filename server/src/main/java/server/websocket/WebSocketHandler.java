package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import server.websocketmessages.Action;
import server.websocketmessages.Notification;

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
        Action action = gson.fromJson(message, Action.class);

        switch (action.type()) {
            case CONNECT -> connectPlayer(action.getPlayerName(), session);
            case MOVE -> handleMove(action.getPlayerName(), action.getMove());
            case DISCONNECT -> disconnectPlayer(action.getPlayerName());
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        connections.remove(session.toString());
        System.out.println("Connection closed: " + session + " due to " + reason);
    }

    private void connectPlayer(String playerName, Session session) {
        try {
            connections.add(playerName, session);
            Notification notification = new Notification(Notification.Type.CONNECT, playerName + " joined the game.");
            connections.broadcast(playerName, notification);
        } catch (IOException e) {
            System.err.println("Failed to connect player: " + e.getMessage());
        }
    }

    private void handleMove(String playerName, String move) throws IOException {
        Notification notification = new Notification(Notification.Type.MOVE, playerName + " made move: " + move);
        connections.broadcast(playerName, notification);
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

