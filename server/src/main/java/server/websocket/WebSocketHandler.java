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
            case LEAVE -> handleLeave(command.getAuthToken(), command.getGameID(), session);
            default -> System.out.println("Unhandled command type");
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
                ServerMessage.ErrorMessage errorMessage = new ServerMessage.ErrorMessage("Invalid game ID.");
                connections.sendToRoot(session, errorMessage);
                return; // Stop further processing
            }

            // Validate authToken
            if (authData == null || !authToken.equals(authData.authToken())) {
                System.out.println("Invalid auth token: " + authToken);
                ServerMessage.ErrorMessage errorMessage = new ServerMessage.ErrorMessage("Invalid auth token.");
                connections.sendToRoot(session, errorMessage);
                return;
            }

            // Add connection with game context
            connections.add(authToken, session, gameID);

            // Determine the player's color
            String playerColor = authData.username().equals(gameData.getWhiteUsername()) ? "white"
                    : authData.username().equals(gameData.getBlackUsername()) ? "black" : "observer";

            // Send a LOAD_GAME message to the root client
            ServerMessage.LoadGameMessage loadGameMessage = new ServerMessage.LoadGameMessage(gameData);
            connections.sendToRoot(session, loadGameMessage);

            // Notify other clients in the same game
            String notificationMessage = authData.username() + " joined as " + playerColor;
            Notification notification = new Notification(notificationMessage);
            connections.broadcastToGame(gameID, authToken, notification);

            System.out.println(authData.username() + " connected to game ID: " + gameID);
        } catch (Exception e) {
            System.err.println("Error connecting player: " + e.getMessage());
            ServerMessage.ErrorMessage errorMessage = new ServerMessage.ErrorMessage("Server error: " + e.getMessage());
            connections.sendToRoot(session, errorMessage);
        }
    }


    private void handleLeave(String authToken, Integer gameID, Session session) {
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

            // Check if the client is a player
            String username = authData.username();
            boolean isWhitePlayer = username.equals(gameData.getWhiteUsername());
            boolean isBlackPlayer = username.equals(gameData.getBlackUsername());

            if (isWhitePlayer || isBlackPlayer) {
                // Update the game data to remove the player
                if (isWhitePlayer) {
                    gameData.setWhiteUsername(null);
                } else {
                    gameData.setBlackUsername(null);
                }

                // Update the game in the database
                gameDAO.updateGame(gameData);

                // Notify other clients
                String leaveNotification = username + " has left the game.";
                Notification notification = new Notification(leaveNotification);

                // Broadcast notification to all players in the game (except the leaving player)
                connections.broadcastToGame(gameID, authToken, notification);

            } else {
                // If the leaving client is an observer
                connections.removeObserver(session);
                String leaveNotification = "An observer has left the game.";
                Notification notification = new Notification(leaveNotification);
                connections.broadcastToGame(gameID, authToken, notification);
            }

            // Remove the connection
            connections.remove(authToken, gameID);

        } catch (Exception e) {
            System.err.println("Error processing leave: " + e.getMessage());
            connections.sendToRoot(session, new ServerMessage.ErrorMessage("Server error: " + e.getMessage()));
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

            // Ensure the game is not over
            if (gameData.getGame().getBoard() == null) { // Null board indicates the game is over
                System.out.println("Attempted move after game is over.");
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("The game is over. No further moves are allowed."));
                return;
            }//add column or vairable to gameData or chessboard, boolean if game is resigned, end game for everyone

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
            connections.broadcastToGame(gameID, null, loadGameMessage);

            // Notify other clients about the move
            String moveNotification = username + " moved from " + move.getStartPosition() + " to " + move.getEndPosition();
            if (move.getPromotionPiece() != null) {
                moveNotification += ", promoting to " + move.getPromotionPiece();
            }
            Notification notification = new Notification(moveNotification);
            connections.broadcastToGame(gameID, authToken, notification);

            // Check for endgame scenarios
            if (game.isInCheckmate(game.getTeamTurn())) {
                Notification checkmateNotification = new Notification("Checkmate! " + game.getTeamTurn() + " loses.");
                connections.broadcastToGame(gameID, null, checkmateNotification);
            } else if (game.isInStalemate(game.getTeamTurn())) {
                Notification stalemateNotification = new Notification("Stalemate! The game is a draw.");
                connections.broadcastToGame(gameID, null, stalemateNotification);
            } else if (game.isInCheck(game.getTeamTurn())) {
                Notification checkNotification = new Notification(game.getTeamTurn() + " is in check.");
                connections.broadcastToGame(gameID, null, checkNotification);
            }

        } catch (Exception e) {
            System.err.println("Error processing move: " + e.getMessage());
            connections.sendToRoot(session, new ServerMessage.ErrorMessage("Server error: " + e.getMessage()));
        }
    }


    private void handleResign(String authToken, Integer gameID, Session session) {
        try {
            // Validate the auth token
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

            // Ensure the game is not already over
            // do new varible, is game over, if true, end game
            if (gameData.getGame().getBoard() == null) { // Null board indicates the game is over
                System.out.println("Attempted to resign after the game is already over.");
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("The game is already over. You cannot resign."));
                return;
            }

            // Ensure the player is part of the game
            String username = authData.username();
            if (!username.equals(gameData.getWhiteUsername()) && !username.equals(gameData.getBlackUsername())) {
                System.out.println("Player not part of the game: " + username);
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("Player not part of the game."));
                return;
            }

            // Prevent double resignation
            if (gameData.isResigned()) {
                System.out.println("Double resignation detected for game ID " + gameID);
                connections.sendToRoot(session, new ServerMessage.ErrorMessage("Resignation already occurred. The game is over."));
                return;
            }

            // Mark the game as over
            ChessGame game = gameData.getGame();
            gameData.setResigned(true); // Mark the game as resigned
            game.setBoard(null); // Clear the board to indicate game over
            gameDAO.updateGame(gameData);

            // Notify all players and observers, including the resigning player
            String resignationMessage = username + " has resigned. The game is over.";
            Notification notification = new Notification(resignationMessage);
            connections.broadcastToGame(gameID, null, notification);

            System.out.println("Game ID " + gameID + " marked as over due to resignation by: " + username);

        } catch (Exception e) {
            System.err.println("Error processing resignation: " + e.getMessage());
            connections.sendToRoot(session, new ServerMessage.ErrorMessage("Server error: " + e.getMessage()));
        }
    }


}