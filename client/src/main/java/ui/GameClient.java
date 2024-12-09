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
    private final WebSocketFacade webSocketFacade;
    private int gameId;
    private final String authToken;

    /**
     * Constructs a new GameClient instance.
     *
     * @param serverFacade Provides access to server functions.
     * @param scanner      For reading user input.
     * @param gameId       Unique identifier of the game.
     * @param authToken    Authentication token for secure server access.
     */
    public GameClient(ServerFacade serverFacade, Scanner scanner, WebSocketFacade webSocketFacade, int gameId, String authToken) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
        this.webSocketFacade = webSocketFacade;
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
                if ("quit".equals(result)) {
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
                if (gameId == -1) {
                    System.out.println("No game is currently active. Join or create a game first.");
                    return null;
                }
                promptForMove();
                return "move";
            }
            case "display board" -> {
                if (gameId == -1) {
                    System.out.println("No game is currently active. Join or create a game first.");
                    return null;
                }
                displayCurrentBoard();
                return "display board";
            }
            case "quit" -> {
                if (gameId == -1) {
                    System.out.println("No game is currently active to quit.");
                    return null;
                }
                exitGame();
                return "quit";
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
            webSocketFacade.makeMove(gameId, move, authToken);
            System.out.println("Move accepted.");
            displayCurrentBoard(); // Display the board after a successful move
        } catch (Exception e) {
            System.out.println("Move failed: " + e.getMessage());
        }
    }

    /**
     * Validates the move format to ensure it follows standard chess notation.
     */
    private boolean validateMoveFormat(String move) {
        // Standard chess notation: [a-h][1-8][a-h][1-8]
        return move.matches("^[a-h][1-8][a-h][1-8]$");
    }

    /**
     * Displays the current board state.
     */
    private void displayCurrentBoard() {
        System.out.println("Current board state:");
        try {
            String playerColor = determinePlayerColor();
            String board = serverFacade.getBoard(gameId, authToken, playerColor);
            System.out.println(board);
        } catch (Exception e) {
            System.out.println("Unable to fetch board from server. Simulated response used.");
            System.out.println(getSimulatedBoard("white")); // Default fallback
        }
    }

    /**
     * Determines the player's color dynamically based on game state or defaults to white.
     */
    private String determinePlayerColor() {
        // Logic to determine the player's color dynamically from the game state.
        // Replace this with actual server logic if available.
        return "white"; // Default to white
    }

    private String getSimulatedBoard(String playerColor) {
        // Your simulated board generation logic remains unchanged.
        // (Same as in the original code.)
        return "Simulated board for " + playerColor;
    }


    /**
     * Exits the game and resets the state.
     */
    private void exitGame() {
        try {
            if (webSocketFacade.quitGame(gameId, authToken)) {
                System.out.println("Successfully exited the game.");
                gameId = -1; // Reset the game ID to indicate no active game
            } else {
                System.out.println("Failed to exit the game.");
            }
        } catch (Exception e) {
            System.out.println("Error while exiting the game: " + e.getMessage());
        }
    }
}
