package ui;

import client.ServerFacade;
import model.GameData;

import java.util.List;
import java.util.Scanner;

public class PostLoginClient {
    private final ServerFacade serverFacade;
    private WebSocketFacade webSocketFacade;
    private final Scanner scanner;
    private final String authToken;
    private int joinedGameId = -1; // Store the ID of the joined game
    private final String serverUrl;

    public PostLoginClient(ServerFacade serverFacade, WebSocketFacade webSocketFacade, Scanner scanner, String authToken, String serverUrl) {
        this.serverFacade = serverFacade;
        this.webSocketFacade = webSocketFacade;
        this.scanner = scanner;
        this.authToken = authToken;
        this.serverUrl = serverUrl;
    }

    public String run() {
        while (true) {
            System.out.print("Enter command (create game, list games, play game, observe game, logout, help): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "create game" -> createGame();
                    case "list games" -> listGames();
                    case "play game" -> {
                        if (playGame()) {
                            return "play game"; // Switch to in-game context
                        }
                    }
                    case "observe game" -> observeGame();
                    case "logout" -> {
                        logout();
                        return "logout";
                    }
                    case "help" -> displayHelp();
                    default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private void createGame() {
        try {
            System.out.print("Enter game name: ");
            String gameName = scanner.nextLine().trim();

            serverFacade.createGame(authToken, gameName, null);
            System.out.println("Game '" + gameName + "' created successfully.");
        } catch (Exception e) {
            System.out.println("Failed to create game: " + e.getMessage());
        }
    }

    private void listGames() {
        try {
            List<GameData> gamesList = serverFacade.listGames(authToken);
            displayGames(gamesList);
        } catch (Exception e) {
            System.out.println("Failed to retrieve games: " + e.getMessage());
        }
    }

    private boolean playGame() {
        try {
            List<GameData> gamesList = serverFacade.listGames(authToken);
            if (gamesList.isEmpty()) {
                System.out.println("No games available to join.");
                return false;
            }

            displayGames(gamesList);
            System.out.print("Enter game number to play: ");
            int gameIndex = Integer.parseInt(scanner.nextLine()) - 1;

            if (gameIndex < 0 || gameIndex >= gamesList.size()) {
                System.out.println("Invalid game number.");
                return false;
            }

            GameData selectedGame = gamesList.get(gameIndex);
            System.out.print("Choose color (white or black): ");
            String playerColor = scanner.nextLine().trim().toLowerCase();

            if (!playerColor.equals("white") && !playerColor.equals("black")) {
                System.out.println("Invalid color choice.");
                return false;
            }

            serverFacade.joinGame(authToken, selectedGame.getGameID(), playerColor);
            System.out.printf("Joined game '%s' as %s.%n", selectedGame.getGameName(), playerColor);

            // Store the game ID and connect to the WebSocket
            joinedGameId = selectedGame.getGameID();

            webSocketFacade = new WebSocketFacade(serverUrl);
            webSocketFacade.connectToGame(authToken, joinedGameId);
            return true; // Successfully joined the game
        } catch (Exception e) {
            System.out.println("Failed to join game: " + e.getMessage());
        }
        return false;
    }

    private void observeGame() {
        try {
            System.out.print("Enter game number to observe: ");
            int gameId = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter perspective (white/black, default is white): ");
            String perspective = scanner.nextLine().trim().toLowerCase();

            if (!perspective.equals("white") && !perspective.equals("black")) {
                perspective = "white";
            }

            webSocketFacade.observeGame(authToken, gameId, perspective);
            System.out.printf("Observing game #%d as %s.%n", gameId, perspective);
        } catch (Exception e) {
            System.out.println("Failed to observe game: " + e.getMessage());
        }
    }

    private void logout() {
        try {
            webSocketFacade.close();
            serverFacade.logout(authToken);
            System.out.println("Logged out successfully.");
        } catch (Exception e) {
            System.out.println("Failed to log out: " + e.getMessage());
        }
    }

    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("- create game: Create a new game");
        System.out.println("- list games: List all available games");
        System.out.println("- play game: Join and play a game");
        System.out.println("- observe game: Observe an ongoing game");
        System.out.println("- logout: Log out of your account");
        System.out.println("- help: Display this help message");
    }

    private void displayGames(List<GameData> gamesList) {
        if (gamesList.isEmpty()) {
            System.out.println("No games available.");
        } else {
            System.out.println("Available games:");
            int index = 1;
            for (GameData game : gamesList) {
                String white = game.getWhiteUsername() == null ? "Empty" : game.getWhiteUsername();
                String black = game.getBlackUsername() == null ? "Empty" : game.getBlackUsername();
                System.out.printf("%d. %s (White: %s, Black: %s)%n", index++, game.getGameName(), white, black);
            }
        }
    }

    public int getJoinedGameId() {
        return joinedGameId;
    }
}
