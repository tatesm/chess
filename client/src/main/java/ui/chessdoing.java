package ui;

public class chessdoing {
    public void drawChessBoard(boolean isWhiteBottom) {
        System.out.print(EscapeSequences.ERASE_SCREEN);  // Clears the screen for a fresh draw

        String[][] board = {
                {EscapeSequences.BLACK_ROOK, EscapeSequences.BLACK_KNIGHT, EscapeSequences.BLACK_BISHOP, EscapeSequences.BLACK_QUEEN,
                        EscapeSequences.BLACK_KING, EscapeSequences.BLACK_BISHOP, EscapeSequences.BLACK_KNIGHT, EscapeSequences.BLACK_ROOK},
                {EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN,
                        EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN, EscapeSequences.BLACK_PAWN},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY,
                        EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY, EscapeSequences.EMPTY},
                {EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN,
                        EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN, EscapeSequences.WHITE_PAWN},
                {EscapeSequences.WHITE_ROOK, EscapeSequences.WHITE_KNIGHT, EscapeSequences.WHITE_BISHOP, EscapeSequences.WHITE_QUEEN,
                        EscapeSequences.WHITE_KING, EscapeSequences.WHITE_BISHOP, EscapeSequences.WHITE_KNIGHT, EscapeSequences.WHITE_ROOK}
        };

        // Row and column headers
        String[] columns = isWhiteBottom ? new String[]{"a", "b", "c", "d", "e", "f", "g", "h"} : new String[]{"h", "g", "f", "e", "d", "c", "b", "a"};
        System.out.print("   ");
        for (String col : columns) {
            System.out.print(" " + col + " ");
        }
        System.out.println();

        for (int i = 0; i < 8; i++) {
            int rowNum = isWhiteBottom ? 8 - i : i + 1;
            System.out.print(rowNum + " ");

            for (int j = 0; j < 8; j++) {
                boolean isLightSquare = (i + j) % 2 == 0;
                String bgColor = isLightSquare ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY : EscapeSequences.SET_BG_COLOR_DARK_GREY;
                System.out.print(bgColor + board[i][j] + EscapeSequences.RESET_BG_COLOR);
            }
            System.out.print(" " + rowNum);
            System.out.println();
        }

        System.out.print("   ");
        for (String col : columns) {
            System.out.print(" " + col + " ");
        }
        System.out.println(EscapeSequences.RESET_BG_COLOR);
    }

    public void displayPreloginMenu() {
        System.out.println(EscapeSequences.SET_TEXT_BOLD + "Welcome to Chess Client!" + EscapeSequences.RESET_TEXT_BOLD_FAINT);
        System.out.println("Available Commands:");
        System.out.println("- " + EscapeSequences.SET_TEXT_COLOR_YELLOW + "Help" + EscapeSequences.RESET_TEXT_COLOR + ": Displays help text.");
        System.out.println("- " + EscapeSequences.SET_TEXT_COLOR_YELLOW + "Login" + EscapeSequences.RESET_TEXT_COLOR + ": Login to your account.");
        System.out.println("- " + EscapeSequences.SET_TEXT_COLOR_YELLOW + "Register" + EscapeSequences.RESET_TEXT_COLOR + ": Create a new account.");
        System.out.println("- " + EscapeSequences.SET_TEXT_COLOR_YELLOW + "Quit" + EscapeSequences.RESET_TEXT_COLOR + ": Exit the application.");
    }

}
