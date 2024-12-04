package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    private final String playerName; // Player's name or identifier
    private final Session session;

    public Connection(String playerName, Session session) {
        this.playerName = playerName;
        this.session = session;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isOpen() {
        return session.isOpen();
    }

    public void send(String message) throws IOException {
        session.getRemote().sendString(message);
    }

    public Session getSession() {
        return this.session;
    }

}
