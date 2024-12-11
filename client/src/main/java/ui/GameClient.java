package ui;

import chess.ChessBoard;

import java.util.Scanner;

public class GameClient {
    private final WebSocketFacade webSocketFacade;
    private final int gameId;
    private final String authToken;
    private final Scanner scanner;
    private final ChessBoard chessBoard;

    public GameClient(WebSocketFacade webSocketFacade, int gameId, String authToken, Scanner scanner) {
        this.webSocketFacade = webSocketFacade;
        this.gameId = gameId;
        this.authToken = authToken;
        this.scanner = scanner;
        this.chessBoard = new ChessBoard();
    }

    public String run() {
        while (true) {
            System.out.print("Enter command (help, move, leave, resign, quit): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "help" -> displayHelp();
                    case "move" -> makeMove();
                    case "leave" -> {
                        leaveGame();
                        return "postlogin";
                    }
                    case "resign" -> {
                        resignGame();
                        return "postlogin";
                    }
                    case "quit" -> {
                        System.out.println("Quitting game.");
                        return "quit";
                    }
                    default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("- help: Display this help message.");
        System.out.println("- make move: Make a chess move.");
        System.out.println("- resign: Resign from the game.");
        System.out.println("- leave: Leave the current game.");
        System.out.println("- highlight moves: Highlight legal moves for a selected piece.");
        System.out.println("- redraw board: Redraw the current chess board.");
    }

    private void makeMove() throws Exception {
        System.out.print("Enter your move (e.g., e2e4): ");
        String move = scanner.nextLine();
        webSocketFacade.makeMove(gameId, move, authToken);
        System.out.println("Move executed.");
    }

    private boolean resignGame() throws Exception {
        if (webSocketFacade.resignGame(gameId, authToken)) {
            System.out.println("You have resigned from the game.");
            return true;
        } else {
            System.out.println("Failed to resign.");
            return false;
        }
    }

    private boolean leaveGame() throws Exception {
        if (webSocketFacade.leaveGame(gameId, authToken)) {
            System.out.println("You have left the game.");
            return true;
        } else {
            System.out.println("Failed to leave the game.");
            return false;
        }
    }

    private void highlightMoves() throws Exception {
        System.out.print("Enter the square of the piece (e.g., e2): ");
        String square = scanner.nextLine().trim().toLowerCase();
        String highlightedBoard = webSocketFacade.highlightLegalMoves(chessBoard, square);

        if (highlightedBoard != null) {
            System.out.println("Highlighted Board:");
            System.out.println(highlightedBoard);
        } else {
            System.out.println("No legal moves available.");
        }
    }

    private void redrawBoard() throws Exception {
        System.out.print("Enter perspective (white/black): ");
        String perspective = scanner.nextLine().trim().toLowerCase();
        System.out.println(webSocketFacade.getBoard(gameId, authToken, perspective, chessBoard));
    }
}
