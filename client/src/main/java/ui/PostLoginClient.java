package ui;

import client.ServerFacade;
import model.GameData;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PostLoginClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private final String authToken; // The unique authentication token for the logged-in user
    private int joinedGameId = -1; // Tracks the game the user has joined, if any

    public PostLoginClient(ServerFacade serverFacade, Scanner scanner, String authToken) {
        this.serverFacade = serverFacade; // Dependency injection for server communication
        this.scanner = scanner; // User input handler
        this.authToken = authToken; // Authentication token for API requests
    }

    public String getAuthToken() {
        return authToken; // Returns the current user's auth token
    }

    public String run() {
        // Main application loop, processes user commands until an exit condition is met
        return Base.run(
                this::getUserCommand,
                command -> {
                    try {
                        return processCommand(command); // Handle the user command
                    } catch (Exception e) {
                        throw new RuntimeException(e); // Ensure exceptions don't crash the loop
                    }
                }
        );
    }

    private String getUserCommand() {
        // Prompts the user for their next action
        System.out.print("Enter command (create game, list games, play game, observe game, logout, help): ");
        return scanner.nextLine().trim().toLowerCase(); // Normalize input for consistency
    }

    private String processCommand(String command) throws Exception {
        // Maps user commands to their respective methods
        switch (command) {
            case "create game" -> {
                createGame();
                return "create game";
            }
            case "list games" -> {
                listGames();
                return "list games";
            }
            case "play game" -> {
                if (playGame()) {
                    return "play game";
                }
            }
            case "observe game" -> {
                observeGame();
                return "observe game";
            }
            case "logout" -> {
                logout();
                return "logout";
            }
            case "help" -> {
                displayHelp();
                return "help";
            }
            default -> {
                System.out.println("Invalid command. Type 'help' for a list of commands.");
            }
        }
        return null; // No valid command was processed
    }

    private void handleError(Exception e) {
        // Unified error handling for unexpected issues
        System.err.println("An error occurred. Please try again.");
        e.printStackTrace();
    }

    public int getJoinedGameId() {
        return joinedGameId; // Retrieve the ID of the currently joined game
    }

    private boolean playGame() {
        try {
            // Fetch the list of games directly from the server
            List<GameData> gamesList = serverFacade.listGames(authToken);
            displayGames(gamesList); // Use the helper method

            if (gamesList.isEmpty()) {
                return false;
            }

            System.out.print("Enter game number (index as displayed above): ");
            int gameIndex = Integer.parseInt(scanner.nextLine()); // User inputs game index

            // Validate the game index
            if (gameIndex < 1 || gameIndex > gamesList.size()) {
                System.out.println("Invalid game index. Please enter a number between 1 and " + gamesList.size() + ".");
                return false;
            }

            // Translate game index to game ID
            GameData selectedGame = gamesList.get(gameIndex - 1); // Convert 1-based index to 0-based
            int gameId = selectedGame.getGameID(); // Retrieve the actual game ID from the selected game

            String playerColor = chooseColor(); // User chooses their desired color
            if (playerColor == null) {
                System.out.println("Action canceled.");
                return false; // User opted out
            }

            // Attempt to join the game
            serverFacade.joinGame(authToken, gameId, playerColor);
            System.out.printf("Successfully joined game '%s' as %s.%n", selectedGame.getGameName(), playerColor);

            // Display the board after joining
            String board = serverFacade.getBoard(gameId, authToken, playerColor); // Fetch the board
            System.out.println("Current Board:");
            System.out.println(board);

            return true;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Failed to join game: " + e.getMessage()); // Display error from the server
        }
        return false; // Joining game failed
    }


    private void createGame() {
        // Handles creating a new game
        try {
            System.out.print("Enter game name: ");
            String gameName = scanner.nextLine().trim();

            serverFacade.createGame(authToken, gameName, null); // Create game without specifying color
            System.out.println("Game '" + gameName + "' created successfully.");
        } catch (Exception e) {
            System.out.println("Failed to create game. Please try again."); // Inform user about the failure
        }
    }

    private void displayGames(List<GameData> gamesList) {
        if (gamesList.isEmpty()) {
            System.out.println("No games currently available."); // no games to display
        } else {
            System.out.println("Available games:");
            int index = 1;
            for (GameData game : gamesList) {
                String white = (game.getWhiteUsername() == null) ? "Empty" : game.getWhiteUsername();
                String black = (game.getBlackUsername() == null) ? "Empty" : game.getBlackUsername();
                System.out.printf("%d. Game Name: %s, White: %s, Black: %s%n", index++, game.getGameName(), white, black);
            }
        }
    }

    private void listGames() {
        try {
            var gamesList = serverFacade.listGames(authToken); // Fetch the game list from the server
            displayGames(gamesList); // Use the helper method
        } catch (Exception e) {
            System.out.println("Failed to retrieve game list. Please try again."); // Handle API errors gracefully
        }
    }


    private void observeGame() {
        // Allows the user to view a game's board without participating
        try {
            System.out.print("Enter game number to observe: ");
            int gameId = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter perspective (white or black, default is white): ");
            String perspective = scanner.nextLine().trim().toLowerCase();

            if (!perspective.equals("white") && !perspective.equals("black")) {
                perspective = "white"; // Default perspective
            }

            serverFacade.observeGame(authToken, gameId, perspective); // Pass the perspective
        } catch (NumberFormatException e) {
            System.out.println("Invalid game number. Please enter a valid number."); // Inform user about invalid input
        } catch (Exception e) {
            System.out.println("Failed to retrieve game board. Please try again."); // Handle errors fetching the board
        }
    }


    private void logout() {
        // Logs the user out and invalidates their session
        try {
            serverFacade.logout(authToken); // Call server to log out
            System.out.println("Successfully logged out.");
        } catch (Exception e) {
            System.out.println("Failed to log out. Please try again."); // Notify user if logout fails
        }
    }

    private void displayHelp() {
        // Provides a list of all available commands to the user
        System.out.println("Available commands:");
        System.out.println("- create game: Create a new game");
        System.out.println("- list games: List all existing games");
        System.out.println("- play game: Join a game and play");
        System.out.println("- observe game: Observe an ongoing game");
        System.out.println("- logout: Log out of your account");
        System.out.println("- help: Display this help message");
    }

    private String chooseColor() {
        // Allows the user to select a color for their game
        System.out.print("Choose color (white or black) or type 'cancel' to go back: ");
        String color = scanner.nextLine().trim().toLowerCase();
        if (color.equals("white") || color.equals("black")) {
            return color; // Valid color chosen
        } else if (color.equals("cancel")) {
            System.out.println("Canceled color selection."); // User canceled the action
            return null;
        } else {
            System.out.println("Invalid color choice. Please choose 'white' or 'black'.");
            return chooseColor(); // Retry color selection
        }
    }
}




