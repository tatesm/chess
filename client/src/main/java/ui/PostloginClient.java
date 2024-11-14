package ui;

import server.ServerFacade;
import exception.ResponseException;

import java.util.Scanner;

public class PostLoginClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;

    public PostLoginClient(ServerFacade serverFacade, Scanner scanner) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
    }

    public void run() {
        while (true) {
            System.out.print("Enter command (create game, list games, play game, observe game, logout): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "create game" -> createGame();
                    case "list games" -> listGames();
                    case "play game" -> {
                        if (playGame()) return; // Transition to GameClient if playing a game
                    }
                    case "observe game" -> observeGame();
                    case "logout" -> {
                        logout();
                        return; // Return to PreloginClient after logout
                    }
                    default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
                }
            } catch (ResponseException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void createGame() throws ResponseException {
        System.out.print("Enter game name: ");
        String gameName = scanner.nextLine();
        serverFacade.createGame(gameName);
        System.out.println("Game created successfully.");
    }

    private void listGames() throws ResponseException {
        System.out.println("Listing all games:");
        System.out.println(serverFacade.listGames());
    }

    private boolean playGame() throws ResponseException {
        System.out.print("Enter game ID: ");
        int gameId = Integer.parseInt(scanner.nextLine());
        System.out.print("Choose color (white or black): ");
        String color = scanner.nextLine();

        serverFacade.joinGame(gameId, color);
        System.out.println("Joined game. Starting game view...");
        return true; // Transition to GameClient
    }

    private void observeGame() throws ResponseException {
        System.out.print("Enter game ID: ");
        int gameId = Integer.parseInt(scanner.nextLine());

        serverFacade.observeGame(gameId);
        System.out.println("Observing game.");
    }

    private void logout() throws ResponseException {
        serverFacade.logout();
        System.out.println("Logged out.");
    }
}
