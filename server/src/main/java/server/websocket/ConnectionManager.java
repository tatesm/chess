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
        sessionToAuthToken.put(session, playerName); // Populate session map
        System.out.println("Connection added for player: " + playerName);
    }

    public void remove(String playerName) {
        Connection connection = connections.remove(playerName);
        if (connection != null) {
            sessionToAuthToken.remove(connection.getSession()); // Clean up session mapping
        }
        System.out.println("Connection removed for player: " + playerName);
    }

    public String getAuthTokenBySession(Session session) {
        return sessionToAuthToken.get(session);
    }

    public void broadcast(String excludePlayer, ServerMessage message) {
        for (Connection connection : connections.values()) {
            if (connection.isOpen() && !connection.getPlayerName().equals(excludePlayer)) {
                try {
                    String json = gson.toJson(message);
                    connection.send(json);
                    System.out.println("Broadcasting to: " + connection.getPlayerName());
                } catch (IOException e) {
                    System.err.println("Failed to send message to: " + connection.getPlayerName());
                }
            } else {
                System.out.println("Excluding player: " + connection.getPlayerName() + " from broadcast.");
            }
        }
    }

    public boolean sendToRoot(String rootPlayer, ServerMessage message) { // takes in a session, sends to the session
        Connection rootConnection = connections.get(rootPlayer);
        if (rootConnection != null && rootConnection.isOpen()) {
            try {
                String json = gson.toJson(message); // Serialize ServerMessage to JSON
                System.out.println("Sending to root player: " + rootPlayer);
                System.out.println("Message content: " + json);
                rootConnection.send(json);
                return true;
            } catch (IOException e) {
                System.err.println("Failed to send message to root client: " + rootPlayer + ". Message: " + gson.toJson(message));
                e.printStackTrace();
            }
        } else {
            System.err.println("No open connection found for root player: " + rootPlayer);
        }
        return false; // Indicate failure
    }

    public Connection getConnection(String playerName) {
        return connections.get(playerName);
    }
}
