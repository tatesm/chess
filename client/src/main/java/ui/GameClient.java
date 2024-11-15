package ui;

import client.ServerFacade;

import java.util.Scanner;

public class GameClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private final int gameId;
    private final String authToken;

    public GameClient(ServerFacade serverFacade, Scanner scanner, int gameId, String authToken) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
        this.gameId = gameId;
        this.authToken = authToken;
    }

    public void run() {
        while (true) {
            System.out.print("Enter command (move, display board, quit game): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "move" -> makeMove();
                    case "display board" -> displayInitialBoard();
                    case "quit game" -> {
                        quitGame();
                        return; // Exit GameClient and go back to PostLoginClient
                    }
                    default -> System.out.println("Invalid command. Type 'move', 'display board', or 'quit game'.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred. Please try again.");
            }
        }
    }

    private void makeMove() {
        System.out.print("Enter your move (e.g., e2 e4): ");
        String move = scanner.nextLine();
        try {
            serverFacade.makeMove(gameId, move, authToken);
            System.out.println("Move made successfully.");
            displayInitialBoard(); // Refresh board after move
        } catch (Exception e) {
            System.out.println("Invalid move or server error. Please ensure move format is correct and try again.");
        }
    }

    private void displayInitialBoard() {
        System.out.println("Current board:");
        try {
            String board = serverFacade.getBoard(gameId, authToken);
            System.out.println(board);
        } catch (Exception e) {
            System.out.println("Could not retrieve board. Please try again.");
        }
    }

    private void quitGame() {
        try {
            serverFacade.quitGame(gameId, authToken);
            System.out.println("Exited game #" + gameId);
        } catch (Exception e) {
            System.out.println("Error exiting game. Please try again.");
        }
    }
}



