package ui;

import chess.ChessBoard;

import java.util.Scanner;

/**
 * GameUI class to handle game-specific commands and actions.
 */
public class GameClient {
    private final Scanner scanner;
    private final WebSocketFacade webSocketFacade;
    private final int gameId;
    private final String authToken;
    private ChessBoard chessBoard;

    public GameClient(Scanner scanner, WebSocketFacade webSocketFacade, int gameId, String authToken) {
        this.scanner = scanner;
        this.webSocketFacade = webSocketFacade;
        this.gameId = gameId;
        this.authToken = authToken;
        this.chessBoard = new ChessBoard();
    }

    public String run() {
        while (true) {
            System.out.print("Enter command (help, redraw board, leave, make move, resign, highlight moves): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                String result = String.valueOf(processCommand(command));
                if ("quit".equals(result)) {
                    return "quit"; // Exit the game
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private boolean processCommand(String command) throws Exception {
        switch (command) {
            case "help" -> displayHelp();
            case "redraw board" -> redrawBoard();
            case "leave" -> {
                leaveGame();
                return false; // Exit the loop
            }
            case "make move" -> makeMove();
            case "resign" -> resignGame();
            case "highlight moves" -> highlightLegalMoves();
            default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
        return true;
    }

    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("- help: Display this help message.");
        System.out.println("- redraw board: Redraw the current chess board.");
        System.out.println("- leave: Leave the current game.");
        System.out.println("- make move: Enter and execute a chess move.");
        System.out.println("- resign: Resign from the game.");
        System.out.println("- highlight moves: Highlight all legal moves for a selected piece.");
    }

    private void redrawBoard() throws Exception { //
        String board = webSocketFacade.getBoard(gameId, authToken, "white", chessBoard); // Default to "white"
        System.out.println("Current board:");
        System.out.println(board);
    }

    private void leaveGame() throws Exception {
        if (webSocketFacade.leaveGame(gameId, authToken)) {
            System.out.println("You have left the game.");
        } else {
            System.out.println("Failed to leave the game.");
        }
    }

    private void makeMove() throws Exception {
        System.out.print("Enter your move (e.g., e2e4): ");
        String move = scanner.nextLine().trim();
        webSocketFacade.makeMove(gameId, move, authToken);
        System.out.println("Move executed.");
    }

    private void resignGame() throws Exception {
        if (webSocketFacade.resignGame(gameId, authToken)) {
            System.out.println("You have resigned. The game is over.");
        } else {
            System.out.println("Failed to resign.");
        }
    }

    private void highlightLegalMoves() throws Exception {
        System.out.print("Enter the square of the piece (e.g., e2): ");
        String square = scanner.nextLine().trim();
        // format square, get the legal moves for hte peice oat the postion, using the board taht i already have. Then format board same as in redraw, but highlight moves, ex in specs.

    }

}
