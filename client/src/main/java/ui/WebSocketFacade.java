package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import client.Helper;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebSocketFacade {
    private final String serverUrl;
    private final Gson gson;

    public WebSocketFacade(String serverUrl) {
        this.serverUrl = serverUrl;
        this.gson = new Gson();
    }

    public void makeMove(int gameId, String move, String authToken) throws Exception { // needs to be websocket

    }


    public boolean quitGame(int gameId, String authToken) throws Exception { //needs to be websocket
        URL url = new URL(serverUrl + "/game/" + gameId + "/quit");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        connection.setRequestProperty("Authorization", authToken);  // Pass authToken in header

        try {
            handleError(connection); // Throws an exception if there's an error response
            return true; // Return true if no errors were encountered
        } catch (Exception e) {
            System.out.println("Failed to quit game: " + e.getMessage());
            return false; // Return false if an error occurs
        } finally {
            connection.disconnect(); // Ensure the connection is closed
        }
    }

    public void observeGame(String authToken, int gameId, String perspective) throws Exception {
        // Validate perspective input
        if (!perspective.equalsIgnoreCase("white") && !perspective.equalsIgnoreCase("black")) {
            perspective = "white"; // Default perspective
        }

        System.out.println("Observing Game #" + gameId + " as " + perspective);
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

    public boolean resignGame(int gameId, String authToken) {
        return false;
    }

    private String[][] reverseBoard(String[][] board) {
        String[][] reversedBoard = new String[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            reversedBoard[i] = board[board.length - 1 - i];
        }
        return reversedBoard;
    }

    public boolean leaveGame(int gameId, String authToken) {
        //send command to server side websockethandler
    }

    private String pieceToDisplay(ChessPiece piece) {
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
