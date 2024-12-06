package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> gameConnections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Session, String> sessionToAuthToken = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    /**
     * Adds a new connection for a player in a specific game.
     *
     * @param playerName the player's name or auth token
     * @param session    the WebSocket session
     * @param gameID     the ID of the game the player is joining
     */
    public void add(String playerName, Session session, int gameID) {
        var connection = new Connection(playerName, session);
        connections.put(playerName, connection);
        sessionToAuthToken.put(session, playerName);

        gameConnections.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>())
                .put(playerName, connection);

        System.out.println("Connection added for player: " + playerName + " in game: " + gameID);
    }

    /**
     * Removes a connection by player name and game ID.
     *
     * @param playerName the player's name or auth token
     * @param gameID     the ID of the game
     */
    public void remove(String playerName, int gameID) {
        Connection connection = connections.remove(playerName);
        if (connection != null) {
            sessionToAuthToken.remove(connection.getSession());
        }

        ConcurrentHashMap<String, Connection> gamePlayers = gameConnections.get(gameID);
        if (gamePlayers != null) {
            gamePlayers.remove(playerName);
            if (gamePlayers.isEmpty()) {
                gameConnections.remove(gameID); // Clean up empty games
            }
        }

        System.out.println("Connection removed for player: " + playerName + " from game: " + gameID);
    }

    /**
     * Removes an observer by session.
     *
     * @param session the WebSocket session to remove
     */
    public void removeObserver(Session session) {
        String authToken = sessionToAuthToken.remove(session);
        if (authToken != null) {
            connections.remove(authToken);
            gameConnections.values().forEach(game -> game.remove(authToken));
            System.out.println("Observer removed for token: " + authToken);
        }
    }

    public Integer getGameIDByAuthToken(String authToken) {
        for (var entry : gameConnections.entrySet()) {
            Integer gameID = entry.getKey();
            ConcurrentHashMap<String, Connection> gamePlayers = entry.getValue();
            if (gamePlayers.containsKey(authToken)) {
                return gameID; // Return the game ID if the player is found
            }
        }
        return null; // Return null if no game ID is associated with the auth token
    }

    /**
     * Broadcasts a message to all connected clients in a specific game,
     * excluding a specific player.
     *
     * @param gameID        the ID of the game
     * @param excludePlayer the player to exclude from the broadcast
     * @param message       the message to broadcast
     */
    public void broadcastToGame(int gameID, String excludePlayer, ServerMessage message) {
        ConcurrentHashMap<String, Connection> gamePlayers = gameConnections.get(gameID);
        if (gamePlayers == null) {
            System.out.println("No players found for game ID: " + gameID);
            return;
        }

        for (Connection connection : gamePlayers.values()) {
            if (connection.isOpen() && !connection.getPlayerName().equals(excludePlayer)) {
                try {
                    String json = gson.toJson(message);
                    connection.send(json);
                    System.out.println("Broadcasting to: " + connection.getPlayerName() + " in game: " + gameID);
                } catch (IOException e) {
                    System.err.println("Failed to send message to: " + connection.getPlayerName());
                }
            } else {
                System.out.println("Excluding player: " + connection.getPlayerName() + " from broadcast.");
            }
        }
    }


    /**
     * Retrieves the auth token associated with a session.
     *
     * @param session the WebSocket session
     * @return the auth token or null if not found
     */
    public String getAuthTokenBySession(Session session) {
        return sessionToAuthToken.get(session);
    }

    /**
     * Broadcasts a message to all connected clients, excluding a specific player.
     *
     * @param excludePlayer the player to exclude from the broadcast
     * @param message       the message to broadcast
     */


    /**
     * Sends a message to a specific client using their session.
     *
     * @param session the WebSocket session to send the message to
     * @param message the message to send
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendToRoot(Session session, ServerMessage message) {
        if (session != null && session.isOpen()) {
            try {
                String json = gson.toJson(message);
                System.out.println("Sending message to session: " + session);
                session.getRemote().sendString(json); // Send the message
                return true;
            } catch (IOException e) {
                System.err.println("Failed to send message to session. Message: " + gson.toJson(message));
                e.printStackTrace();
            }
        } else {
            System.err.println("Session is null or closed: " + session);
        }
        return false;
    }

    /**
     * Checks if a connection exists for the given auth token.
     *
     * @param authToken the auth token
     * @return true if a connection exists, false otherwise
     */
    public boolean containsToken(String authToken) {
        return connections.containsKey(authToken);
    }

    /**
     * Checks if the client is an observer in the game.
     *
     * @param authToken     the auth token of the client
     * @param whiteUsername the username of the white player
     * @param blackUsername the username of the black player
     * @return true if the client is an observer, false otherwise
     */
    public boolean isObserver(String authToken, String whiteUsername, String blackUsername) {
        if (!containsToken(authToken)) {
            return false; // Not part of the game
        }
        // If not one of the players, consider them an observer
        return !authToken.equals(whiteUsername) && !authToken.equals(blackUsername);
    }

    /**
     * Retrieves the connection associated with an auth token.
     *
     * @param authToken the auth token
     * @return the connection or null if not found
     */
    public Connection get(String authToken) {
        return connections.get(authToken);
    }
}
