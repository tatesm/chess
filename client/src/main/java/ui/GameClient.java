package ui;

import client.ServerFacade;

import java.util.Scanner;

/**
 * GameClient provides an interface for users to interact with the game server.
 * Users can make moves, display the current board, or quit the game.
 */
public class GameClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private final int gameId;
    private final String authToken;

    /**
     * Constructs a new GameClient instance.
     *
     * @param serverFacade Provides access to server functions.
     * @param scanner      For reading user input.
     * @param gameId       Unique identifier of the game.
     * @param authToken    Authentication token for secure server access.
     */
    public GameClient(ServerFacade serverFacade, Scanner scanner, int gameId, String authToken) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
        this.gameId = gameId;
        this.authToken = authToken;
    }

    /**
     * Runs the main command loop for the game client.
     */
    public String run() {
        while (true) {
            System.out.print("Enter command (move, display board, quit): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                String result = processCommand(command);
                if (result != null && result.equals("quit")) {
                    return "quit"; // Exit the game
                }
            } catch (Exception e) {
                System.out.println("An unexpected error occurred. Please try again.");
            }
        }
    }

    private String processCommand(String command) throws Exception {
        switch (command) {
            case "move" -> {
                promptForMove();
                return "move";
            }
            case "display board" -> {
                displayCurrentBoard();
                return "display board";
            }
            case "quit" -> {
                exitGame();
                return "quit"; // Exit the loop
            }
            default -> {
                System.out.println("Unknown command. Please type 'move', 'display board', or 'quit'.");
            }
        }
        return null; // Continue the loop
    }


    /**
     * Prompts the user for a move and attempts to make it on the server.
     */
    private void promptForMove() {
        System.out.print("Enter your move (e.g., e2e4): ");
        String move = scanner.nextLine().trim();

        if (!validateMoveFormat(move)) {
            System.out.println("Invalid move format. Moves should follow standard chess notation.");
            return;
        }

        try {
            serverFacade.makeMove(gameId, move, authToken);
            System.out.println("Move accepted.");
            displayCurrentBoard();
        } catch (Exception e) {
            System.out.println("Move failed. Check the move format and try again.");
        }
    }


    /**
     * Validates that the move follows a basic format.
     *
     * @param move The move string entered by the user.
     * @return true if the move format is valid; false otherwise.
     */
    private boolean validateMoveFormat(String move) {
        // Simple validation for chess move format (e.g., e2e4)
        return move.matches("^[a-h][1-8][a-h][1-8]$");
    }

    /**
     * Displays the current game board by fetching it from the server.
     */
    private void displayCurrentBoard() {
        // Display the current state of the simulated board
        System.out.println("Current board state:");
        try {
            String board = serverFacade.getBoard(gameId, authToken); // Fetch the simulated board
            System.out.println(board);
        } catch (Exception e) {
            System.out.println("Unable to fetch board from server. Simulated response used.");
        }
    }


    /**
     * Quits the game and exits gracefully.
     */
    private void exitGame() {
        try {
            serverFacade.quitGame(gameId, authToken);
            System.out.println("You have exited game #" + gameId);
        } catch (Exception e) {
            System.out.println("Failed to exit the game properly.");
        }
    }
}

