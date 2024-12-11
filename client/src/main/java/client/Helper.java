package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import ui.EscapeSequences;
import ui.WebSocketFacade;

public class Helper {
    public static String formatBoard(String[][] board) {
        StringBuilder boardRepresentation = new StringBuilder();

        // Add column labels (A-H) with proper alignment
        boardRepresentation.append("    "); // Indentation for row numbers
        for (char column = 'A'; column <= 'H'; column++) {
            boardRepresentation.append("  ").append(column).append("  ");
        }
        boardRepresentation.append("\n");

        // Add board rows with row numbers (1-8)
        for (int row = 0; row < board.length; row++) {
            boardRepresentation.append(" ").append(8 - row).append("  "); // Row number on the left
            for (int col = 0; col < board[row].length; col++) {
                String bgColor = (row + col) % 2 == 0 ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                String cellContent = board[row][col] == null || board[row][col].isBlank() ? "     " : "  " + board[row][col] + "  ";
                boardRepresentation.append(bgColor).append(cellContent).append(EscapeSequences.RESET_BG_COLOR);
            }
            boardRepresentation.append("  ").append(8 - row).append("\n"); // Row number on the right
        }

        // Add column labels again (A-H) with proper alignment
        boardRepresentation.append("    ");
        for (char column = 'A'; column <= 'H'; column++) {
            boardRepresentation.append("  ").append(column).append("  ");
        }
        boardRepresentation.append("\n");

        return boardRepresentation.toString();
    }


    public static String formatBoardWithHighlight(ChessBoard chessBoard, String square, String[] legalMoves) {
        String[][] boardDisplay = new String[8][8];

        // Populate board with pieces
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row, col));
                boardDisplay[row - 1][col - 1] = piece != null ? pieceToDisplay(piece) : EscapeSequences.EMPTY;
            }
        }

        // Highlight the selected square
        int selectedRow = 8 - (square.charAt(1) - '1'); // Convert square to 0-indexed row
        int selectedCol = square.charAt(0) - 'a';       // Convert square to 0-indexed column
        boardDisplay[selectedRow][selectedCol] = EscapeSequences.SET_BG_COLOR_BLUE
                + boardDisplay[selectedRow][selectedCol] + EscapeSequences.RESET_BG_COLOR;

        // Highlight the legal moves
        for (String move : legalMoves) {
            int moveRow = 8 - (move.charAt(1) - '1');
            int moveCol = move.charAt(0) - 'a';
            boardDisplay[moveRow][moveCol] = EscapeSequences.SET_BG_COLOR_GREEN
                    + boardDisplay[moveRow][moveCol] + EscapeSequences.RESET_BG_COLOR;
        }

        // Format the board for display
        return formatBoard(boardDisplay);
    }

    private static String pieceToDisplay(ChessPiece piece) {
        switch (piece.getPieceType()) {
            case PAWN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "\u2659" : "\u265F";
            case ROOK:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "\u2656" : "\u265C";
            case KNIGHT:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "\u2658" : "\u265E";
            case BISHOP:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "\u2657" : "\u265D";
            case QUEEN:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "\u2655" : "\u265B";
            case KING:
                return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? "\u2654" : "\u265A";
            default:
                return " ";
        }
    }

    public static String[][] convertBoardToDisplay(ChessBoard chessBoard) {
        String[][] boardDisplay = new String[8][8];

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = chessBoard.getPiece(position);

                // If there's a piece at the position, display it; otherwise, leave it empty
                boardDisplay[row - 1][col - 1] = piece != null ? pieceToDisplay(piece) : EscapeSequences.EMPTY;
            }
        }

        return boardDisplay;
    }

}
