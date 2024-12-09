package ui;

import client.ServerFacade;

import java.util.Scanner;

public class GameClient {
    private final WebSocketFacade webSocketFacade;
    private final Scanner scanner;
    private final int gameId;
    private final String authToken;

    public GameClient(WebSocketFacade webSocketFacade, Scanner scanner, int gameId, String authToken) {
        this.webSocketFacade = webSocketFacade;
        this.scanner = scanner;
        this.gameId = gameId;
        this.authToken = authToken;
    }

    public String run() {
        while (true) {
            System.out.print("Enter command (help, redraw, move, resign, leave): ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "help" -> displayHelp();
                case "redraw" -> redrawBoard();
                case "move" -> makeMove();
                case "resign" -> {
                    if (resignGame()) {
                        return "postlogin"; // Return to post-login after resigning
                    }
                }
                case "leave" -> {
                    if (leaveGame()) {
                        return "postlogin"; // Return to post-login after leaving the game
                    }
                }
                case "logout" -> {
                    return "logout"; // Return to pre-login
                }
                case "quit" -> {
                    return "quit"; // Exit the application
                }
                default -> System.out.println("Unknown command. Type 'help' for available commands.");
            }
        }
    }

    private void displayHelp() {
        System.out.println("""
                Available commands:
                - help: Displays this help message.
                - redraw: Redraws the chess board.
                - move: Make a move in the game.
                - resign: Resign from the game and return to post-login.
                - leave: Leave the game and return to post-login.
                - logout: Logout and return to pre-login.
                - quit: Quit the application.
                """);
    }

    private void redrawBoard() {
        try {
            webSocketFacade.redrawBoard(gameId, authToken);
            System.out.println("Board redrawn successfully.");
        } catch (Exception e) {
            System.err.println("Failed to redraw board: " + e.getMessage());
        }
    }

    private void makeMove() {
        System.out.print("Enter your move (e.g., e2e4): ");
        String move = scanner.nextLine().trim();

        try {
            webSocketFacade.makeMove(gameId, move, authToken);
            System.out.println("Move executed successfully.");
        } catch (Exception e) {
            System.err.println("Failed to make move: " + e.getMessage());
        }
    }

    private boolean resignGame() {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(confirmation)) {
            try {
                webSocketFacade.resignGamePlayer(gameId, authToken);
                System.out.println("You have resigned from the game.");
                return true;
            } catch (Exception e) {
                System.err.println("Failed to resign: " + e.getMessage());
            }
        } else {
            System.out.println("Resignation canceled.");
        }
        return false;
    }

    private boolean leaveGame() {
        System.out.print("Are you sure you want to leave the game? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(confirmation)) {
            try {
                webSocketFacade.leaveGameObserver(gameId, authToken);
                System.out.println("You have left the game.");
                return true;
            } catch (Exception e) {
                System.err.println("Failed to leave game: " + e.getMessage());
            }
        } else {
            System.out.println("Leave game canceled.");
        }
        return false;
    }
}
