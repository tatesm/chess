package ui;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.Notification;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

public class WebSocketCommunicator {
    private static WebSocketCommunicator instance;
    private WebSocket webSocket;
    private final Gson gson;

    // Private constructor for singleton
    private WebSocketCommunicator() {
        this.gson = new Gson();
    }

    // Singleton pattern to get the instance
    public static WebSocketCommunicator getInstance() {
        if (instance == null) {
            instance = new WebSocketCommunicator();
        }
        return instance;
    }


    // Connect to the WebSocket server
    public void connect(String uri) throws Exception {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            throw new IllegalStateException("WebSocket already connected!");
        }

        webSocket = HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(new URI(uri), new WebSocket.Listener() {
                    @Override
                    public CompletableFuture<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        handleServerMessage(data.toString());
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        System.err.println("WebSocket error: " + error.getMessage());
                    }
                }).join();
    }


    // Send a command to the WebSocket server
    public void sendCommand(UserGameCommand command) throws Exception {
        if (webSocket == null || webSocket.isOutputClosed()) {
            throw new IllegalStateException("WebSocket is not connected!");
        }

        String commandJson = gson.toJson(command);
        webSocket.sendText(commandJson, true);
    }

    // Handle incoming messages from the server
    private void handleServerMessage(String message) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

            if (serverMessage instanceof Notification notification) {
                System.out.println("Notification: " + notification.getMessage());
            } else if (serverMessage instanceof ServerMessage.ErrorMessage errorMessage) {
                System.out.println("Error: " + errorMessage.getErrorMessage());
            } else if (serverMessage instanceof ServerMessage.LegalMovesMessage legalMovesMessage) {
                System.out.println("Legal Moves:");
                legalMovesMessage.getLegalMoves().forEach(move ->
                        System.out.println(move.getStartPosition() + " -> " + move.getEndPosition())
                );
            } else if (serverMessage instanceof ServerMessage.LoadGameMessage loadGameMessage) {
                System.out.println("Game Loaded: " + loadGameMessage.getGame());
            }
        } catch (Exception e) {
            System.err.println("Failed to handle server message: " + e.getMessage());
        }
    }

    // Disconnect the WebSocket
    public void disconnect() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client disconnecting").join();
            webSocket = null;
        }
    }
}
