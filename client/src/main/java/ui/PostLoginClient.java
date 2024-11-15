package ui;

import client.ServerFacade;

import java.util.Scanner;

public class PostLoginClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private String authToken;
    private int joinedGameId = -1;

    public PostLoginClient(ServerFacade serverFacade, Scanner scanner) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void run() {
        while (true) {
            System.out.print("Enter command (create game, list games, play game, observe game, logout, help): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "create game" -> createGame();
                    case "list games" -> listGames();
                    case "play game" -> {
                        if (playGame()) return;
                    }
                    case "observe game" -> observeGame();
                    case "logout" -> {
                        logout();
                        return;
                    }
                    case "help" -> displayHelp();
                    default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred. Please try again.");
            }
        }
    }

    public int getJoinedGameId() {
        return joinedGameId;
    }

    private boolean playGame() {
        try {
            System.out.print("Enter game number: ");
            int gameId = Integer.parseInt(scanner.nextLine());
            System.out.print("Choose color (white or black): ");
            String color = scanner.nextLine().trim().toLowerCase();

            if (!color.equals("white") && !color.equals("black")) {
                System.out.println("Invalid color. Please choose 'white' or 'black'.");
                return false;
            }

            // if need an authorization token to join a game
            System.out.print("Enter authorization token: ");
            String authToken = scanner.nextLine().trim();

            serverFacade.joinGame(authToken, gameId, color);
            joinedGameId = gameId;
            System.out.println("Joined game #" + gameId + " as " + color + ". Transitioning to game view...");
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
            System.out.print("Choose color (white or black): ");
            String playerColor = scanner.nextLine().trim().toLowerCase();

            if (!playerColor.equals("white") && !playerColor.equals("black")) {
                System.out.println("Invalid color. Please choose 'white' or 'black'.");
                return;
            }

            // if need an authorization token to create a game
            System.out.print("Enter authorization token: ");
            String authToken = scanner.nextLine().trim();

            serverFacade.createGame(authToken, gameName, playerColor);
            System.out.println("Game '" + gameName + "' created successfully.");
        } catch (Exception e) {
            System.out.println("Failed to create game. Please try again.");
        }
    }

    private void listGames() {
        try {
            // if need an authorization token to list games
            System.out.print("Enter authorization token: ");
            String authToken = scanner.nextLine().trim();

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


            System.out.print("Enter authorization token: ");
            String authToken = scanner.nextLine().trim();

            // serverFacade.observeGame(authToken, gameId);
            //System.out.println("Observing game #" + gameId);
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID. Please enter a valid number.");
        } catch (Exception e) {
            System.out.println("Failed to observe game. Please try again.");
        }
    }

    private void logout() {
        try {

            System.out.print("Enter authorization token: ");
            String authToken = scanner.nextLine().trim();

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


