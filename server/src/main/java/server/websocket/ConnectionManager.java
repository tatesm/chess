package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Session, String> sessionToAuthToken = new ConcurrentHashMap<>();
    private final Gson gson = new Gson(); // Create a Gson instance for JSON serialization

    public void add(String playerName, Session session) {
        var connection = new Connection(playerName, session);
        connections.put(playerName, connection);
        System.out.println("Connection added for player: " + playerName);
    }

    public void remove(String playerName) {
        connections.remove(playerName);
        System.out.println("Connection removed for player: " + playerName);
    }

    public String getAuthTokenBySession(Session session) {
        return sessionToAuthToken.get(session);
    }

    public void broadcast(String excludePlayer, ServerMessage message) {
        for (Connection connection : connections.values()) {
            if (connection.isOpen() && !connection.getPlayerName().equals(excludePlayer)) {
                try {
                    String json = new Gson().toJson(message); // Serialize ServerMessage to JSON
                    connection.send(json);
                } catch (IOException e) {
                    System.err.println("Failed to send message to: " + connection.getPlayerName());
                }
            }
        }
    }

    public void sendToRoot(String rootPlayer, ServerMessage message) {
        Connection rootConnection = connections.get(rootPlayer);
        if (rootConnection != null && rootConnection.isOpen()) {
            try {
                String json = gson.toJson(message); // Serialize ServerMessage to JSON
                rootConnection.send(json);
            } catch (IOException e) {
                System.err.println("Failed to send message to root client: " + rootPlayer);
            }
        }
    }


    public Connection getConnection(String playerName) {
        return connections.get(playerName);
    }
}
