package ui;

import chess.*;
import client.Helper;
import com.google.gson.Gson;

import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;


public class WebSocketFacade extends Endpoint {
    private final Gson gson = new Gson();
    private final String serverUrl;
    private Session session;

    public WebSocketFacade(String serverUrl) throws Exception {
        this.serverUrl = serverUrl.replace("http", "ws") + "/ws";
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = new URI(this.serverUrl);

        // Connect to WebSocket server
        this.session = container.connectToServer(this, uri);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        System.out.println("WebSocket connection established.");
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("WebSocket connection closed: " + closeReason.getReasonPhrase());
    }

    @Override
    public void onError(Session session, Throwable thr) {
        System.err.println("WebSocket error: " + thr.getMessage());
    }

    @OnMessage
    public void onMessage(String message) {
        try {

            ServerMessage baseMessage = gson.fromJson(message, ServerMessage.class);

            // Dispatch based on ServerMessageType
            switch (baseMessage.getServerMessageType()) {
                case LOAD_GAME -> handleLoadGame(gson.fromJson(message, ServerMessage.LoadGameMessage.class));
                case ERROR -> handleError(gson.fromJson(message, ServerMessage.ErrorMessage.class));
                case NOTIFICATION -> handleNotification(gson.fromJson(message, Notification.class));
                default -> System.err.println("Unhandled message type: " + baseMessage.getServerMessageType());
            }
        } catch (Exception e) {
            System.err.println("Failed to process incoming message: " + e.getMessage());
        }
    }


    private void handleLoadGame(ServerMessage serverMessage) {
        try {
            if (serverMessage instanceof ServerMessage.LoadGameMessage loadGameMessage) {
                GameData gameData = loadGameMessage.getGame();
                ChessBoard chessBoard = gameData.getGame().getBoard();

                System.out.println("Game Name: " + gameData.getGameName());
                System.out.println("White Player: " + gameData.getWhiteUsername());
                System.out.println("Black Player: " + gameData.getBlackUsername());
                System.out.println("Is Game Over: " + gameData.getGame().isGameOver());
                System.out.println("Current Turn: " + gameData.getGame().getTeamTurn());
                System.out.println("Current Board:");
                System.out.println(Helper.formatBoard(Helper.convertBoardToDisplay(chessBoard)));
            } else {
                System.err.println("Unexpected message type for handleLoadGame.");
            }
        } catch (Exception e) {
            System.err.println("Failed to handle load game message: " + e.getMessage());
        }
    }


    public String highlightLegalMoves(ChessBoard chessBoard, String selectedSquare) {
        try {
            ChessPosition position = parseSquare(selectedSquare); // Convert square to ChessPosition
            ChessPiece piece = chessBoard.getPiece(position);

            if (piece == null) {
                return "No piece at the selected square.";
            }

            // Set up a temporary ChessGame instance
            ChessGame tempGame = new ChessGame();
            tempGame.setBoard(chessBoard); // Use the provided ChessBoard

            Collection<ChessMove> legalMoves = tempGame.validMoves(position);

            if (legalMoves == null || legalMoves.isEmpty()) {
                return "No legal moves available for the selected piece.";
            }

            // Extract end positions for highlighting
            String[] highlightedMoves = legalMoves.stream()
                    .map(move -> move.getEndPosition().toString())
                    .toArray(String[]::new);

            // Highlight the board
            return Helper.formatBoardWithHighlight(chessBoard, selectedSquare, highlightedMoves);
        } catch (Exception e) {
            return "Error highlighting moves: " + e.getMessage();
        }
    }


    private ChessPosition parseSquare(String selectedSquare) {
        if (selectedSquare == null || selectedSquare.length() != 2) {
            throw new IllegalArgumentException("Invalid square format. Must be a letter (a-h) followed by a number (1-8).");
        }

        char columnChar = selectedSquare.charAt(0);
        char rowChar = selectedSquare.charAt(1);

        if (columnChar < 'a' || columnChar > 'h' || rowChar < '1' || rowChar > '8') {
            throw new IllegalArgumentException("Invalid square coordinates. Must be within 'a1' to 'h8'.");
        }

        int row = rowChar - '1' + 1; // Convert '1'-'8' to 1-8
        int column = columnChar - 'a' + 1; // Convert 'a'-'h' to 1-8

        return new ChessPosition(row, column);
    }


    private void handleNotification(ServerMessage serverMessage) {
        if (serverMessage instanceof Notification) {
            Notification notification = (Notification) serverMessage;
            System.out.println("Notification: " + notification.getMessage());
        } else {
            System.err.println("Invalid server message type for notification: " + serverMessage.getServerMessageType());
        }
    }


    private void handleError(ServerMessage serverMessage) {
        if (serverMessage instanceof ServerMessage.ErrorMessage) {
            ServerMessage.ErrorMessage errorMessage = (ServerMessage.ErrorMessage) serverMessage;
            System.err.println("Error message received: " + errorMessage.getErrorMessage());

        } else {
            System.err.println("Invalid server message type for error: " + serverMessage.getServerMessageType());
        }
    }


    public void makeMove(int gameId, String move, String authToken) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId, move));
        System.out.println("Move command sent successfully.");
    }

    public boolean leaveGame(int gameId, String authToken) {
        try {
            sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId));
            System.out.println("Leave command sent successfully.");
            return true;
        } catch (IOException e) {
            System.out.println("Failed to send leave game command: " + e.getMessage());
            return false;
        }
    }

    public boolean resignGame(int gameId, String authToken) {
        try {
            sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId));
            System.out.println("Resign command sent successfully.");
            return true;
        } catch (IOException e) {
            System.out.println("Failed to send resign game command: " + e.getMessage());
            return false;
        }
    }

    private void sendCommand(UserGameCommand command) throws IOException {
        String message = gson.toJson(command);
        this.session.getBasicRemote().sendText(message);
    }

    public void close() {
        try {
            if (this.session != null && this.session.isOpen()) {
                this.session.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing WebSocket: " + e.getMessage());
        }
    }


    public String getBoard(int gameId, String authToken, String playerColor, ChessBoard chessBoard) {
        String[][] boardDisplay = new String[8][8];

        // Populate board representation
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row, col));
                if (piece != null) {
                    boardDisplay[row - 1][col - 1] = pieceToDisplay(piece);
                } else {
                    boardDisplay[row - 1][col - 1] = EscapeSequences.EMPTY;
                }
            }
        }

        // Adjust for player perspective
        if (playerColor.equalsIgnoreCase("black")) {
            boardDisplay = reverseBoard(boardDisplay);
        }

        return Helper.formatBoard(boardDisplay);
    }

    private String[][] reverseBoard(String[][] board) {
        String[][] reversedBoard = new String[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            reversedBoard[i] = board[board.length - 1 - i];
        }
        return reversedBoard;
    }

    public void observeGame(String authToken, int gameId, String perspective) throws Exception {
        // Validate perspective input
        if (!perspective.equalsIgnoreCase("white") && !perspective.equalsIgnoreCase("black")) {
            perspective = "white"; // Default perspective
        }

        System.out.println("Observing Game #" + gameId + " as " + perspective);
    }


    String pieceToDisplay(ChessPiece piece) {
        String display = "";
        switch (piece.getPieceType()) {
            case PAWN:
                display = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
                break;
            case ROOK:
                display = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
                break;
            case KNIGHT:
                display = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
                break;
            case BISHOP:
                display = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
                break;
            case QUEEN:
                display = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
                break;
            case KING:
                display = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                        ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
                break;
        }
        return display;
    }


    public void connectToGame(String authToken, int gameId) throws Exception {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken,
                gameId
        );
        sendCommand(command);
        System.out.println("Connected to game via WebSocket.");
    }
}
