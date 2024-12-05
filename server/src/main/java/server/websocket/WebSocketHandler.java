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
            connections.add(authToken, session);
            GameData gameData = gameDAO.getGame(gameID);

            ServerMessage.LoadGameMessage loadGameMessage = new ServerMessage.LoadGameMessage(gameData);
            connections.broadcast(authToken, loadGameMessage);
        } catch (Exception e) {
            System.err.println("Error connecting player: " + e.getMessage());
        }
    }
}
