package server.websocket;

import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import chess.ChessGame;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import dataaccess.AuthTokenDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;


import java.io.IOException;

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
    public void onMessage(Session session, String message) {
        var command = gson.fromJson(message, websocket.commands.UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connectPlayer(command.getAuthToken(), command.getGameID(), session);
            case MAKE_MOVE -> {
                ChessMove move = command.getMove(gson, message);
                handleMove(command.getAuthToken(), command.getGameID(), move, session);
            }
            case RESIGN -> handleResign(command.getAuthToken(), command.getGameID(), session);
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

            // Retrieve the game data for the given game ID
            GameData gameData = gameDAO.getGame(gameID);
            AuthData authData = authDAO.getAuth(authToken);

            // Handle invalid game ID
            if (gameData == null) {
                System.out.println("Invalid game ID: " + gameID);
                ServerMessage.ErrorMessage errorMessage = new ServerMessage.ErrorMessage("Invalid game ID.");
                connections.sendToRoot(session, errorMessage);
                return; // Stop further processing
            }

            // Validate authToken
            if (!authToken.equals(authData.authToken()) && !authToken.equals(gameData.getBlackUsername())) {
                System.out.println("Invalid auth token: " + authToken);
                ServerMessage.ErrorMessage errorMessage = new ServerMessage.ErrorMessage("Invalid auth token.");
                connections.sendToRoot(session, errorMessage);
                return;
            }
            connections.add(authToken, session);

            // Determine the player's color
            String playerColor = authData.username().equals(gameData.getWhiteUsername()) ? "white" :
                    authData.username().equals(gameData.getBlackUsername()) ? "black" : "observer";

            // Send a LOAD_GAME message to the root client
            ServerMessage.LoadGameMessage loadGameMessage = new ServerMessage.LoadGameMessage(gameData);
            System.out.println("Sending LOAD_GAME to root client: " + authToken);
            connections.sendToRoot(session, loadGameMessage);

            // Notify other clients (broadcast)
            String notificationMessage = gameData.getGameName() + " | " + authToken + " joined as " + playerColor;
            Notification notification = new Notification(notificationMessage);
            System.out.println("Broadcasting NOTIFICATION to other players.");
            connections.broadcast(authToken, notification);

        } catch (Exception e) {
            System.err.println("Error connecting player: " + e.getMessage());
            ServerMessage.ErrorMessage errorMessage = new ServerMessage.ErrorMessage("Server error: " + e.getMessage());
            connections.sendToRoot(session, errorMessage);
        }
    }


    private void handleMove(String authToken, Integer gameID, ChessMove move, Session session) {
        try {
            // Validate auth token
            AuthData authData = authDAO.getAuth(authToken);
            if (authData == null) {
                System.out.println("Invalid auth token: " + authToken);
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("Invalid auth token."));
                return;
            }

            // Retrieve game data
            GameData gameData = gameDAO.getGame(gameID);
            if (gameData == null) {
                System.out.println("Invalid game ID: " + gameID);
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("Invalid game ID."));
                return;
            }

            // Ensure player is part of the game
            String username = authData.username();
            boolean isWhitePlayer = username.equals(gameData.getWhiteUsername());
            boolean isBlackPlayer = username.equals(gameData.getBlackUsername());
            if (!isWhitePlayer && !isBlackPlayer) {
                System.out.println("Player not part of the game: " + username);
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("Player not part of the game."));
                return;
            }

            // Check if the player is attempting to move their own piece
            ChessGame game = gameData.getGame();
            ChessPiece piece = game.getBoard().getPiece(move.getStartPosition());
            if (piece == null || (isWhitePlayer && piece.getTeamColor() != ChessGame.TeamColor.WHITE) ||
                    (isBlackPlayer && piece.getTeamColor() != ChessGame.TeamColor.BLACK)) {
                System.out.println("Player attempted to move an opponent's piece: " + username);
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("You can only move your own pieces."));
                return;
            }

            // Attempt to make the move
            try {
                game.makeMove(move); // Apply the move
            } catch (InvalidMoveException e) {
                System.out.println("Invalid move: " + e.getMessage());
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("Invalid move: " + e.getMessage()));
                return;
            }

            // Update the game in the database
            gameDAO.updateGame(gameData);

            // Broadcast updated game state to all clients
            ServerMessage.LoadGameMessage loadGameMessage = new ServerMessage.LoadGameMessage(gameData);
            connections.broadcast(null, loadGameMessage);

            // Notify other clients about the move
            String moveNotification = username + " moved from " + move.getStartPosition() + " to " + move.getEndPosition();
            if (move.getPromotionPiece() != null) {
                moveNotification += ", promoting to " + move.getPromotionPiece();
            }
            Notification notification = new Notification(moveNotification);
            connections.broadcast(authToken, notification);

            // Check for endgame scenarios
            if (game.isInCheckmate(game.getTeamTurn())) {
                Notification checkmateNotification = new Notification("Checkmate! " + game.getTeamTurn() + " loses.");
                connections.broadcast(null, checkmateNotification);
            } else if (game.isInStalemate(game.getTeamTurn())) {
                Notification stalemateNotification = new Notification("Stalemate! The game is a draw.");
                connections.broadcast(null, stalemateNotification);
            } else if (game.isInCheck(game.getTeamTurn())) {
                Notification checkNotification = new Notification(game.getTeamTurn() + " is in check.");
                connections.broadcast(null, checkNotification);
            }

        } catch (Exception e) {
            System.err.println("Error processing move: " + e.getMessage());
            connections.sendToRoot(session, new ServerMessage.ErrorMessage("Server error: " + e.getMessage()));
        }
    }

    private void handleResign(String authToken, Integer gameID, Session session) {
        try {
            // Validate auth token
            AuthData authData = authDAO.getAuth(authToken);
            if (authData == null) {
                System.out.println("Invalid auth token: " + authToken);
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("Invalid auth token."));
                return;
            }

            // Retrieve game data
            GameData gameData = gameDAO.getGame(gameID);
            if (gameData == null) {
                System.out.println("Invalid game ID: " + gameID);
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("Invalid game ID."));
                return;
            }

            // Ensure player is part of the game
            String username = authData.username();
            boolean isWhitePlayer = username.equals(gameData.getWhiteUsername());
            boolean isBlackPlayer = username.equals(gameData.getBlackUsername());
            if (!isWhitePlayer && !isBlackPlayer) {
                System.out.println("Player not part of the game: " + username);
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("Player not part of the game."));
                return;
            }

            // Mark game as over in the database
            gameData.getGame().setBoard(null); // Clear the board to signify game over
            gameDAO.updateGame(gameData);

            // Notify all players about the resignation
            String resignMessage = username + " has resigned. " +
                    (isWhitePlayer ? "Black wins!" : "White wins!");
            Notification notification = new Notification(resignMessage);
            connections.broadcast(authToken, notification);

        } catch (Exception e) {
            System.err.println("Error processing resign: " + e.getMessage());
            connections.sendToRoot(session, new ServerMessage.ErrorMessage("Server error: " + e.getMessage()));
        }
    }


}