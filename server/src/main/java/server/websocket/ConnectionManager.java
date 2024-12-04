package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public void add(String playerName, Session session) {
        var connection = new Connection(playerName, session);
        connections.put(playerName, connection);
    }

    public void remove(String playerName) {
        connections.remove(playerName);
    }

    public void broadcast(String excludePlayer, Notification notification) throws IOException {
        var toRemove = new ArrayList<Connection>();
        for (var connection : connections.values()) {
            if (connection.isOpen() && !connection.getPlayerName().equals(excludePlayer)) {
                connection.send(notification.toString());
            } else if (!connection.isOpen()) {
                toRemove.add(connection);
            }
        }

        // Remove closed connections
        for (var connection : toRemove) {
            connections.remove(connection.getPlayerName());
        }
    }

    public Connection getConnection(String playerName) {
        return connections.get(playerName);
    }
}
