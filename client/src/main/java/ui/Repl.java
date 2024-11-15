package ui;

import client.ServerFacade;
import model.AuthData;
import model.GameData;

import java.util.Scanner;

/**
 * Repl class provides a command-line interface to interact with the game server.
 * Allows users to login, register, manage games, and play.
 */
public class Repl {
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade serverFacade;
    private boolean loggedIn = false;
    private boolean inGame = false;
    private String authToken;  // Stores auth token after login
    private int currentGameId = -1;

    /**
     * Initializes the REPL with the given server URL.
     */
    public Repl(String serverUrl) {
        this.serverFacade = new ServerFacade(serverUrl);
    }

    /**
     * Main loop to control user interaction flow: pre-login, post-login, and gameplay.
     */
    public void run() {
        while (true) {
            if (!loggedIn) {
                preLoginLoop();
            } else if (!inGame) {
                postLoginLoop();
            } else {
                gameplayLoop();
            }
        }
    }

    /**
     * Handles commands available before logging in.
     */
    private void preLoginLoop() {
        while (!loggedIn) {
            System.out.println("Pre-login commands: help, login, register, quit");
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "help" -> displayHelp();
                case "login" -> promptLogin();
                case "register" -> promptRegister();
                case "quit" -> exitProgram();
                default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
            }
        }
    }

    /**
     * Handles commands available after login but before joining a game.
     */
    private void postLoginLoop() {
        while (loggedIn && !inGame) {
            System.out.println("Post-login commands: help, logout, create game, list games, play game, observe game");
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "help" -> displayHelp();
                case "logout" -> logout();
                case "create game" -> createGame();
                case "list games" -> listGames();
                case "play game" -> playGame();
                // case "observe game" -> observeGame();
                default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
            }
        }
    }

    /**
     * Handles gameplay commands once the user has joined or started a game.
     */

    private void gameplayLoop() {
        while (inGame) {
            System.out.println("Gameplay commands: move, exit game");
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                //case "move" -> makeMove();
                //case "exit game" -> exitGame();
                default -> System.out.println("Invalid command. Type 'move' or 'exit game'.");
            }
        }
    }

    /**
     * Displays a summary of available commands based on the user's current state.
     */
    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("Pre-login: help, login, register, quit");
        System.out.println("Post-login: help, logout, create game, list games, play game, observe game");
        System.out.println("In-game: move, exit game");
    }

    private void promptLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            AuthData authData = serverFacade.login(username, password);
            if (authData != null) {
                loggedIn = true;
                authToken = authData.authToken();
                System.out.println("Login successful.");
            } else {
                System.out.println("Login failed. Please check your credentials.");
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }
    }

    private void promptRegister() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        try {
            AuthData authData = serverFacade.register(username, password, email);
            if (authData != null) {
                loggedIn = true;
                authToken = authData.authToken();
                System.out.println("Registration successful.");
            } else {
                System.out.println("Registration failed. Try a different username.");
            }
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
        }
    }

    private void logout() {
        try {
            serverFacade.logout(authToken);
            loggedIn = false;
            authToken = null;
            System.out.println("Logged out.");
        } catch (Exception e) {
            System.out.println("Logout error: " + e.getMessage());
        }
    }

    private void createGame() {
        System.out.print("Game name: ");
        String gameName = scanner.nextLine();
        System.out.print("Choose color (white or black): ");
        String playerColor = scanner.nextLine().trim().toLowerCase();

        if (!playerColor.equals("white") && !playerColor.equals("black")) {
            System.out.println("Invalid color choice. Please choose 'white' or 'black'.");
            return;
        }

        try {
            GameData gameData = serverFacade.createGame(authToken, gameName, playerColor);
            currentGameId = gameData.getGameID();
            inGame = true;
            System.out.println("Game created. You are now in the game.");
        } catch (Exception e) {
            System.out.println("Game creation error: " + e.getMessage());
        }
    }

    private void listGames() {
        try {
            var games = serverFacade.listGames(authToken);
            if (games.isEmpty()) {
                System.out.println("No available games.");
            } else {
                System.out.println("Available games:");
                for (int i = 0; i < games.size(); i++) {
                    System.out.println((i + 1) + ": " + games.get(i));
                }
            }
        } catch (Exception e) {
            System.out.println("Error listing games: " + e.getMessage());
        }
    }

    private void playGame() {
        System.out.print("Enter game number to play: ");
        int gameId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Choose color (white or black): ");
        String playerColor = scanner.nextLine().trim().toLowerCase();

        if (!playerColor.equals("white") && !playerColor.equals("black")) {
            System.out.println("Invalid color. Please choose 'white' or 'black'.");
            return;
        }

        try {
            serverFacade.joinGame(authToken, gameId, playerColor);
            currentGameId = gameId;
            inGame = true;
            System.out.println("Joined game. Ready to play.");
        } catch (Exception e) {
            System.out.println("Game join error: " + e.getMessage());
        }
    }
/*
    private void observeGame() {
        System.out.print("Enter game number to observe: ");
        int gameId = Integer.parseInt(scanner.nextLine().trim());

        try {
            serverFacade.observeGame(authToken, gameId);
            inGame = true;
            System.out.println("Observing game.");
        } catch (Exception e) {
            System.out.println("Game observe error: " + e.getMessage());
        }
    }

    private void makeMove() {
        System.out.print("Enter your move (e.g., e2 e4): ");
        String move = scanner.nextLine();

        try {
            serverFacade.makeMove(currentGameId, move, authToken);
            System.out.println("Move successful.");
        } catch (Exception e) {
            System.out.println("Move error: " + e.getMessage());
        }
    }

    private void exitGame() {
        inGame = false;
        System.out.println("You have exited the game.");
    }*/

    private void exitProgram() {
        System.out.println("Goodbye!");
        System.exit(0);
    }
}
