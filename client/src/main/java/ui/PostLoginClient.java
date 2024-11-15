package ui;

import client.ServerFacade;

import java.util.Scanner;

public class PostLoginClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private final String authToken; // Use the passed authToken directly
    private int joinedGameId = -1;

    public PostLoginClient(ServerFacade serverFacade, Scanner scanner, String authToken) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String run() {
        while (true) {
            String command = getUserCommand();

            try {
                String result = processCommand(command);
                if (result != null) {
                    return result;
                }
            } catch (Exception e) {
                handleError(e);
            }
        }
    }

    private String getUserCommand() {
        System.out.print("Enter command (create game, list games, play game, observe game, logout, help): ");
        return scanner.nextLine().trim().toLowerCase();
    }

    private String processCommand(String command) throws Exception {
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
        return null;
    }

    private void handleError(Exception e) {
        System.err.println("An error occurred. Please try again.");
        e.printStackTrace();
    }

    public int getJoinedGameId() {
        return joinedGameId;
    }

    private boolean playGame() {
        try {
            System.out.print("Enter game number: ");
            int gameId = Integer.parseInt(scanner.nextLine());
            String playerColor = chooseColor();
            if (playerColor == null) {
                return false; // User canceled the action
            }

            serverFacade.joinGame(authToken, gameId, playerColor);
            joinedGameId = gameId;
            System.out.println("Joined game #" + gameId + " as " + playerColor + ". Transitioning to game view...");
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID. Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Failed to join game. Please try again.");
        }
        return false;
    }

    private void createGame() {
        try {
            System.out.print("Enter game name: ");
            String gameName = scanner.nextLine().trim();
            String playerColor = chooseColor();
            if (playerColor == null) {
                return; // User canceled the action
            }

            serverFacade.createGame(authToken, gameName, playerColor);
            System.out.println("Game '" + gameName + "' created successfully.");
        } catch (Exception e) {
            System.out.println("Failed to create game. Please try again.");
        }
    }

    private void listGames() {
        try {
            System.out.println("Available games:");
            var gamesList = serverFacade.listGames(authToken);
            if (gamesList.isEmpty()) {
                System.out.println("No games currently available.");
            } else {
                gamesList.forEach(game -> System.out.println("- " + game));
            }
        } catch (Exception e) {
            System.out.println("Failed to retrieve game list. Please try again.");
        }
    }

    private void observeGame() {
        try {
            System.out.print("Enter game number to observe: ");
            int gameId = Integer.parseInt(scanner.nextLine());

            serverFacade.observeGame(authToken, gameId);
            System.out.println("Observing game #" + gameId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID. Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Failed to observe game. Please try again.");
        }
    }

    private void logout() {
        try {
            serverFacade.logout(authToken);
            System.out.println("Successfully logged out.");
        } catch (Exception e) {
            System.out.println("Failed to log out. Please try again.");
        }
    }

    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("- create game: Create a new game");
        System.out.println("- list games: List all existing games");
        System.out.println("- play game: Join a game and play");
        System.out.println("- observe game: Observe an ongoing game");
        System.out.println("- logout: Log out of your account");
        System.out.println("- help: Display this help message");
    }

    private String chooseColor() {
        System.out.print("Choose color (white or black) or type 'cancel' to go back: ");
        String color = scanner.nextLine().trim().toLowerCase();
        if (color.equals("white") || color.equals("black")) {
            return color;
        } else if (color.equals("cancel")) {
            System.out.println("Canceled color selection.");
            return null;
        } else {
            System.out.println("Invalid color choice. Please choose 'white' or 'black'.");
            return chooseColor(); // Recursive call for retry
        }
    }
}



