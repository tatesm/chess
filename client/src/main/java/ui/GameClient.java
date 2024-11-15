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
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private void makeMove() throws Exception {
        System.out.print("Enter your move (e.g., e2 e4): ");
        String move = scanner.nextLine();
        serverFacade.makeMove(gameId, move, authToken);  // Pass authToken as the third argument
        System.out.println("Move made.");
        displayInitialBoard(); // Refresh board after move
    }

    private void displayInitialBoard() throws Exception {
        System.out.println("Current board:");
        // Fetch the board's state from the server and display it
        String board = serverFacade.getBoard(gameId, authToken);  // Ensure authToken is passed
        System.out.println(board);
    }

    private void quitGame() throws Exception {
        serverFacade.quitGame(gameId, authToken);  // Pass authToken as the second argument
        System.out.println("Exited game #" + gameId);
    }
}


