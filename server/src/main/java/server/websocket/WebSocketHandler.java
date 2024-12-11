package server.websocket;

import chess.*;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import dataaccess.AuthTokenDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMove;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.util.List;

@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final GameDAO gameDAO = new GameDAO();
    private final AuthTokenDAO authDAO = new AuthTokenDAO();// Ensure this is properly initialized
    private final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("New connection established");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws DataAccessException {
        var command = gson.fromJson(message, websocket.commands.UserGameCommand.class);

        switch (command.getCommandType()) {
            case CONNECT -> connectPlayer(command.getAuthToken(), command.getGameID(), session);
            case MAKE_MOVE ->
                    handleMove(command.getAuthToken(), command.getGameID(), gson.fromJson(message, MakeMove.class).getChessMove(), session);
            case RESIGN -> handleResign(command.getAuthToken(), command.getGameID(), session);
            case LEAVE -> handleLeave(command.getAuthToken(), command.getGameID(), session);
            default -> System.out.println("Unhandled command type: " + command.getCommandType());
        }
    }


    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String authToken = connections.getAuthTokenBySession(session);
        if (authToken != null) {
            // Retrieve the game ID associated with the auth token
            Integer gameID = connections.getGameIDByAuthToken(authToken);
            if (gameID != null) {
                connections.remove(authToken, gameID); // Pass both authToken and gameID
                System.out.println("Connection closed for player: " + authToken + " in game ID: " + gameID + ". Reason: " + reason);
            } else {
                System.out.println("No game ID found for player: " + authToken + ". Reason: " + reason);
            }
        }
    }


    private void connectPlayer(String authToken, Integer gameID, Session session) {
        try {
            // Retrieve the game data for the given game ID
            GameData gameData = gameDAO.getGame(gameID);
            AuthData authData = authDAO.getAuth(authToken);

            // Handle invalid game ID
            if (gameData == null) {
                System.out.println("Invalid game ID: " + gameID);
                ErrorMessage errorMessage = new ErrorMessage("Invalid game ID.");
                connections.sendToRoot(session, errorMessage);
                return; // Stop further processing
            }

            // Validate authToken
            if (authData == null || !authToken.equals(authData.authToken())) {
                System.out.println("Invalid auth token: " + authToken);
                ErrorMessage errorMessage = new ErrorMessage("Invalid auth token.");
                connections.sendToRoot(session, errorMessage);
                return;
            }

            // Add connection with game context
            connections.add(authToken, session, gameID);

            // Determine the player's color and update the GameData
            String playerColor = authData.username().equals(gameData.getWhiteUsername()) ? "white"
                    : authData.username().equals(gameData.getBlackUsername()) ? "black" : null;

            if (playerColor == null) {
                if (gameData.getWhiteUsername() == null) {
                    gameData.setWhiteUsername(authData.username());
                    playerColor = "white";
                } else if (gameData.getBlackUsername() == null) {
                    gameData.setBlackUsername(authData.username());
                    playerColor = "black";
                } else {
                    // If both slots are taken, treat as an observer
                    playerColor = "observer";
                }
            }

            // Persist the updated game data to the database
            gameDAO.updateGame(gameData);

            // Notify the player of the game load
            LoadGameMessage loadGameMessage = new LoadGameMessage(gameData);
            connections.sendToRoot(session, loadGameMessage);

            // Notify other players in the game
            String notificationMessage = authData.username() + " joined as " + playerColor;
            Notification notification = new Notification(notificationMessage);
            connections.broadcastToGame(gameID, authToken, notification);

            System.out.println(authData.username() + " connected to game ID: " + gameID + " as " + playerColor);
        } catch (Exception e) {
            System.err.println("Error connecting player: " + e.getMessage());
            ErrorMessage errorMessage = new ErrorMessage("Server error: " + e.getMessage());
            connections.sendToRoot(session, errorMessage);
        }
    }


    private void handleLeave(String authToken, Integer gameID, Session session) throws DataAccessException {
        if (!validateAuthAndGame(authToken, gameID, session, (authData, gameData) -> {
            String username = authData.username();
            boolean isWhitePlayer = username.equals(gameData.getWhiteUsername());
            boolean isBlackPlayer = username.equals(gameData.getBlackUsername());

            if (isWhitePlayer || isBlackPlayer) {
                // Remove the player from the game
                if (isWhitePlayer) {
                    gameData.setWhiteUsername(null);
                    System.out.println(username + " left the game and is no longer the white player.");
                } else {
                    gameData.setBlackUsername(null);
                    System.out.println(username + " left the game and is no longer the black player.");
                }
            } else {
                // Handle observer disconnection
                connections.removeObserver(session);
                System.out.println("An observer has left the game.");
            }

            // Persist the updated game state in the database
            gameDAO.updateGame(gameData);

            // Notify other clients in the game
            String leaveNotification = isWhitePlayer || isBlackPlayer
                    ? username + " has left the game."
                    : "An observer has left the game.";
            Notification notification = new Notification(leaveNotification);
            connections.broadcastToGame(gameID, authToken, notification);

            // Remove the connection for the leaving client
            connections.remove(authToken, gameID);
        })) {
            return; // Validation failed, exit
        }
    }


    private void handleMove(String authToken, Integer gameID, ChessMove move, Session session) throws DataAccessException {
        if (!validateAuthAndGame(authToken, gameID, session, (authData, gameData) -> {
            // Ensure the game is not over
            if (gameData.getGame().isGameOver()) {
                connections.sendToRoot(session, new ErrorMessage("The game is over. No further moves are allowed."));
                return;
            }

            // Ensure player is part of the game
            String username = authData.username();
            boolean isWhitePlayer = username.equals(gameData.getWhiteUsername());
            boolean isBlackPlayer = username.equals(gameData.getBlackUsername());
            if (!isWhitePlayer && !isBlackPlayer) {
                connections.sendToRoot(session, new ErrorMessage("Player not part of the game."));
                return;
            }

            // Check if the player is attempting to move their own piece
            ChessGame game = gameData.getGame();
            ChessPiece piece = game.getBoard().getPiece(move.getStartPosition());
            if (piece == null || (isWhitePlayer && piece.getTeamColor() != ChessGame.TeamColor.WHITE) ||
                    (isBlackPlayer && piece.getTeamColor() != ChessGame.TeamColor.BLACK)) {
                connections.sendToRoot(session, new ErrorMessage("You can only move your own pieces."));
                return;
            }

            // Attempt to make the move
            try {
                game.makeMove(move); // Apply the move
            } catch (InvalidMoveException e) {
                connections.sendToRoot(session, new ErrorMessage("Invalid move: " + e.getMessage()));
                return;
            }

            // Update the game in the database
            gameDAO.updateGame(gameData);

            // Broadcast updated game state to all clients
            LoadGameMessage loadGameMessage = new LoadGameMessage(gameData);
            connections.broadcastToGame(gameID, null, loadGameMessage);

            // Notify other clients about the move
            String moveNotification = username + " moved from " + move.getStartPosition() + " to " + move.getEndPosition();
            if (move.getPromotionPiece() != null) {
                moveNotification += ", promoting to " + move.getPromotionPiece();
            }
            Notification notification = new Notification(moveNotification);
            connections.broadcastToGame(gameID, authToken, notification);
        })) {
            return; // Validation failed, exit
        }
    }


    private boolean validateAuthAndGame(String authToken, Integer gameID, Session session, SessionConsumer consumer) throws DataAccessException {
        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null) {
            connections.sendToRoot(session, new ErrorMessage("Invalid auth token."));
            return false;
        }

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) {
            connections.sendToRoot(session, new ErrorMessage("Invalid game ID."));
            return false;
        }

        consumer.accept(authData, gameData);
        return true;
    }

    @FunctionalInterface
    public interface SessionConsumer {
        void accept(AuthData authData, GameData gameData) throws DataAccessException;
    }


    private void handleResign(String authToken, Integer gameID, Session session) {
        try {
            // Validate the auth token
            AuthData authData = authDAO.getAuth(authToken);
            if (authData == null) {
                System.out.println("Invalid auth token: " + authToken);
                connections.sendToRoot(session, new ErrorMessage("Invalid auth token."));
                return;
            }

            // Retrieve game data
            GameData gameData = gameDAO.getGame(gameID);
            if (gameData == null) {
                System.out.println("Invalid game ID: " + gameID);
                connections.sendToRoot(session, new ErrorMessage("Invalid game ID."));
                return;
            }

            // Ensure the game is not already over
            if (gameData.getGame().isGameOver()) {
                System.out.println("Attempted to resign after the game is already over.");
                connections.sendToRoot(session, new ErrorMessage("The game is already over. You cannot resign."));
                return;
            }

            // Ensure the player is part of the game
            String username = authData.username();
            if (!username.equals(gameData.getWhiteUsername()) && !username.equals(gameData.getBlackUsername())) {
                System.out.println("Player not part of the game: " + username);
                connections.sendToRoot(session, new ErrorMessage("Player not part of the game."));
                return;
            }

            // Prevent double resignation
            if (gameData.isResigned()) {
                System.out.println("Double resignation detected for game ID " + gameID);
                connections.sendToRoot(session, new ErrorMessage("Resignation already occurred. The game is over."));
                return;
            }

            // Mark the game as over
            ChessGame game = gameData.getGame();
            gameData.setResigned(true); // Mark the game as resigned
            game.setGameOver(true);    // Mark the game as over
            gameDAO.updateGame(gameData);

            // Notify all players and observers, including the resigning player
            String resignationMessage = username + " has resigned. The game is over.";
            Notification notification = new Notification(resignationMessage);
            connections.broadcastToGame(gameID, null, notification);

            System.out.println("Game ID " + gameID + " marked as over due to resignation by: " + username);

        } catch (Exception e) {
            System.err.println("Error processing resignation: " + e.getMessage());
            connections.sendToRoot(session, new ErrorMessage("Server error: " + e.getMessage()));
        }
    }


}