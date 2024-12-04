package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Session, String> sessionToAuthToken = new ConcurrentHashMap<>();

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


    public void broadcast(String excludePlayer, Notification notification) {
        for (Connection connection : connections.values()) {
            if (connection.isOpen() && !connection.getPlayerName().equals(excludePlayer)) {
                try {
                    connection.send(notification.toString());
                } catch (IOException e) {
                    System.err.println("Failed to send notification to: " + connection.getPlayerName());
                }
            }
        }
    }


    public Connection getConnection(String playerName) {
        return connections.get(playerName);
    }


}
