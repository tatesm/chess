package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import client.Helper;
import com.google.gson.Gson;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WebSocketFacade extends Endpoint {
    private final Gson gson = new Gson();
    private final String serverUrl;
    private Session session;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>(); // To handle synchronous responses

    public WebSocketFacade(String serverUrl) throws Exception {
        this.serverUrl = serverUrl.replace("http", "ws") + "/ws";
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = new URI(this.serverUrl);

        // Connect to WebSocket server
        this.session = container.connectToServer(this, uri);

        // Set message handler
        this.session.addMessageHandler((MessageHandler.Whole<String>) message -> {
            try {
                messageQueue.put(message); // Add received messages to the queue
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
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

    public void makeMove(int gameId, String move, String authToken) throws IOException {
        sendCommand(new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId, move));
        System.out.println("Move command sent successfully.");
    }

    public boolean leaveGame(int gameId, String authToken) {
        try {
            sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId));
            System.out.println("Leave command sent successfully.");
            return true; // Indicate success
        } catch (IOException e) {
            System.out.println("Failed to send leave game command: " + e.getMessage());
            return false; // Indicate failure
        }
    }


    public boolean resignGame(int gameId, String authToken) {
        try {
            sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId));
            System.out.println("Quit command sent successfully.");
            return true;
        } catch (IOException e) {
            System.out.println("Failed to send quit game command: " + e.getMessage());
            return false;
        }
    }

    public String[] highlightLegalMoves(int gameId, String square, String authToken) {
        try {
            // Send LEGAL_MOVES command
            sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEGAL_MOVES, authToken, gameId, square));
            System.out.println("Highlight legal moves command sent successfully.");

            // Wait for server response
            String response = messageQueue.take();
            ServerMessage.LegalMovesMessage legalMovesMessage = gson.fromJson(response, ServerMessage.LegalMovesMessage.class);

            // Return the array of legal moves
            return legalMovesMessage.getLegalMoves().toArray(new String[0]);
        } catch (Exception e) {
            System.out.println("Failed to retrieve legal moves: " + e.getMessage());
            return null;
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


}
