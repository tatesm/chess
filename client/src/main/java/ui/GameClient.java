package ui;

import client.ServerFacade;

import java.util.Scanner;

public class GameClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private final int gameId;
    private final String authToken;  // Add authToken

    public GameClient(ServerFacade serverFacade, Scanner scanner, int gameId, String authToken) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
        this.gameId = gameId;
        this.authToken = authToken;  // Store authToken
    }

    public void run() {
        while (true) {
            System.out.print("Enter command (move, display board, quit game): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "move" -> makeMove();
                    case "display board" -> displayBoard();
                    case "quit game" -> {
                        quitGame();
                        return; // Exit GameClient and go back to PostLoginClient
                    }
                    default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private void makeMove() throws Exception {
        System.out.print("Enter your move (e.g., e2 e4): ");
        String move = scanner.nextLine();
        serverFacade.makeMove(gameId, move, authToken);  // Pass authToken as the third argument
        System.out.println("Move made.");
        displayBoard(); // Refresh board after move
    }

    private void displayBoard() throws Exception {
        System.out.println("Current board:");
        System.out.println(serverFacade.getBoard(gameId));
    }

    private void quitGame() throws Exception {
        serverFacade.quitGame(gameId);
        System.out.println("Exited game #" + gameId);
    }
}

