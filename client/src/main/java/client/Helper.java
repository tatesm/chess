package client;

import ui.EscapeSequences;

public class Helper {
    static String formatBoard(String[][] board) {
        StringBuilder boardRepresentation = new StringBuilder();

        // Add column labels (A-H)
        boardRepresentation.append("   "); // Indentation for row numbers
        for (char column = 'A'; column <= 'H'; column++) {
            boardRepresentation.append(" ").append(column).append(" ");
        }
        boardRepresentation.append("\n");

        // Add board rows with row numbers (1-8)
        for (int row = 0; row < board.length; row++) {
            boardRepresentation.append(8 - row).append(" "); // Row number on the left
            for (int col = 0; col < board[row].length; col++) {
                String bgColor = (row + col) % 2 == 0 ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                boardRepresentation.append(bgColor).append(board[row][col]).append(EscapeSequences.RESET_BG_COLOR);
            }
            boardRepresentation.append(" ").append(8 - row).append("\n"); // Row number on the right
        }

        // Add column labels again (A-H)
        boardRepresentation.append("   ");
        for (char column = 'A'; column <= 'H'; column++) {
            boardRepresentation.append(" ").append(column).append(" ");
        }
        boardRepresentation.append("\n");

        return boardRepresentation.toString();
    }
    
}
