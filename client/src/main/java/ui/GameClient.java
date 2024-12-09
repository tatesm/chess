package ui;

import websocket.commands.UserGameCommand;

import java.util.Scanner;

public class GameClient {
    private final WebSocketFacade webSocketFacade;
    private final Scanner scanner;
    private final String authToken;
    private final int gameId;

    public GameClient(WebSocketFacade webSocketFacade, Scanner scanner, int gameId, String authToken) {
        this.webSocketFacade = webSocketFacade;
        this.scanner = scanner;
        this.gameId = gameId;
        this.authToken = authToken;
    }

    public void run() {
        while (true) {
            System.out.print("Enter command (help, redraw, leave, move, resign, highlight): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                processCommand(command);
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private void processCommand(String command) throws Exception {
        switch (command) {
            case "help" -> displayHelp();
            case "redraw" -> redrawBoard();
            case "leave" -> leaveGame();
            case "move" -> makeMove();
            case "resign" -> resignGame();
            case "highlight" -> highlightLegalMoves();
            default -> System.out.println("Invalid command. Type 'help' to see the list of available commands.");
        }
    }

    private void displayHelp() {
        System.out.println("""
                Available commands:
                - help: Displays the list of available commands.
                - redraw: Redraws the current state of the chess board.
                - leave: Leaves the game and transitions to the post-login UI.
                - move: Makes a move in the game.
                - resign: Resigns from the game, forfeiting it.
                - highlight: Highlights the legal moves for a piece on the board.
                """);
    }

    private void redrawBoard() throws Exception {
        webSocketFacade.redrawBoard(gameId, authToken);
        System.out.println("Board redraw request sent.");
    }

    private void leaveGame() throws Exception {
        webSocketFacade.leaveGameObserver(gameId, authToken);
        System.out.println("Leave request sent. Transitioning to post-login UI...");
    }

    private void makeMove() throws Exception {
        System.out.print("Enter your move (e.g., e2e4): ");
        String move = scanner.nextLine().trim();

        if (!isValidMove(move)) {
            System.out.println("Invalid move format. Please use standard chess notation (e.g., e2e4).");
            return;
        }

        webSocketFacade.makeMove(gameId, move, authToken);
        System.out.println("Move request sent: " + move);
    }

    private void resignGame() throws Exception {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(confirmation)) {
            webSocketFacade.resignGamePlayer(gameId, authToken);
            System.out.println("You have resigned from the game.");
        } else {
            System.out.println("Resignation canceled.");
        }
    }

    private void highlightLegalMoves() throws Exception {
        System.out.print("Enter the position of the piece to highlight (e.g., e2): ");
        String position = scanner.nextLine().trim();

        if (!isValidPosition(position)) {
            System.out.println("Invalid position format. Use standard chess notation (e.g., e2).");
            return;
        }

        webSocketFacade.highlightLegalMoves(gameId, position, authToken);
        System.out.println("Highlight legal moves request sent for: " + position);
    }

    private boolean isValidMove(String move) {
        return move.matches("^[a-h][1-8][a-h][1-8]$");
    }

    private boolean isValidPosition(String position) {
        return position.matches("^[a-h][1-8]$");
    }
}
