package ui;

import client.ServerFacade;
import model.GameData;

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
        // Handles the process of joining a game as a player
        try {
            System.out.print("Enter game number: ");
            int gameId = Integer.parseInt(scanner.nextLine());
            String playerColor = chooseColor(); // User chooses their desired color
            if (playerColor == null) {
                System.out.println("Action canceled.");
                return false; // User opted out
            }

            serverFacade.joinGame(authToken, gameId, playerColor); // Attempt to join the game
            joinedGameId = gameId; // Save the joined game's ID
            System.out.println("Successfully joined game #" + gameId + " as " + playerColor + ".");
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID. Please enter a valid number.");
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

    private void listGames() {
        // Displays all games available on the server
        try {
            System.out.println("Available games:");
            var gamesList = serverFacade.listGames(authToken); // Fetch the game list from the server
            if (gamesList.isEmpty()) {
                System.out.println("No games currently available."); // No games to display
            } else {
                int index = 1; // Start numbering games from 1
                for (GameData game : gamesList) {
                    String white = (game.getWhiteUsername() == null) ? "Empty" : game.getWhiteUsername();
                    String black = (game.getBlackUsername() == null) ? "Empty" : game.getBlackUsername();
                    System.out.printf("%d. Game Name: %s, White: %s, Black: %s%n", index++, game.getGameName(), white, black);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to retrieve game list. Please try again."); // Handle API errors gracefully
        }
    }

    private void observeGame() {
        // Allows the user to observe a game without participating
        try {
            System.out.print("Enter game number to observe: ");
            int gameId = Integer.parseInt(scanner.nextLine());

            serverFacade.observeGame(authToken, gameId); // API call to observe the game
            System.out.println("Observing game #" + gameId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID. Please enter a valid number."); // User input error
        } catch (Exception e) {
            System.out.println("Failed to observe game. Please try again."); // Handle API errors
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




