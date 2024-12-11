package ui;

import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;
import client.Helper;

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
                String result = processCommand(command);
                if (result != null) {
                    return result; // Return the appropriate state based on the command
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }


    private String processCommand(String command) throws Exception {
        if (command.isBlank()) {
            System.out.println("Invalid command. Type 'help' for a list of commands.");
            return null;
        }

        switch (command) {
            case "help" -> displayHelp();
            case "redraw board" -> redrawBoard();
            case "leave" -> {
                leaveGame();
                return "postlogin"; // Signal to return to post-login
            }
            case "make move" -> makeMove();
            case "resign" -> {
                resignGame();
                return "postlogin"; // Signal to return to post-login
            }
            case "highlight moves" -> highlightLegalMoves();
            default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
        return null; // Continue loop for other commands
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

    private void redrawBoard() throws Exception {
        System.out.print("Choose perspective (white/black): ");
        String perspective = scanner.nextLine().trim().toLowerCase();

        if (!perspective.equals("white") && !perspective.equals("black")) {
            System.out.println("Invalid perspective. Defaulting to white.");
            perspective = "white";
        }

        String board = webSocketFacade.getBoard(gameId, authToken, perspective, chessBoard);
        System.out.println("Current board (" + perspective + " perspective):");
        System.out.println(board);
    }

    private void highlightLegalMoves() throws Exception {
        System.out.print("Enter the square of the piece (e.g., e2): ");
        String square = scanner.nextLine().trim().toLowerCase();

        // Use the `highlightLegalMoves` method from `WebSocketFacade` or local logic
        String highlightedBoard = webSocketFacade.highlightLegalMoves(chessBoard, square);

        if (highlightedBoard != null) {
            System.out.println("Highlighted Board:");
            System.out.println(highlightedBoard);
        } else {
            System.out.println("No legal moves available or an error occurred.");
        }
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


}


