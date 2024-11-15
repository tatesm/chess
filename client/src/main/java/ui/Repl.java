package ui;

import client.ServerFacade;
import model.AuthData;
import model.GameData;

import java.util.Scanner;

public class Repl {
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade serverFacade;
    private boolean loggedIn = false;
    private boolean inGame = false;
    private String authToken;  // Store authToken after login
    private int currentGameId = -1;

    public Repl(String serverUrl) {
        this.serverFacade = new ServerFacade(serverUrl);
    }

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

    private void preLoginLoop() {
        while (!loggedIn) {
            System.out.println("Pre-login commands: help, login, register, quit");
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "help" -> displayHelp();
                case "login" -> promptLogin();
                case "register" -> promptRegister();
                case "quit" -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
            }
        }
    }

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
                case "observe game" -> observeGame();
                default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
            }
        }
    }

    private void gameplayLoop() {
        while (inGame) {
            System.out.println("Gameplay commands: move, exit game");
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "move" -> makeMove();
                case "exit game" -> exitGame();
                default -> System.out.println("Invalid command. Type 'move' or 'exit game'.");
            }
        }
    }

    private void displayHelp() {
        System.out.println("Commands available depend on the current mode:");
        System.out.println("In pre-login: help, login, register, quit");
        System.out.println("In post-login: help, logout, create game, list games, play game, observe game");
        System.out.println("In game: move, exit game");
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
                authToken = authData.authToken(); // Save auth token for further requests
                System.out.println("Login successful.");
            } else {
                System.out.println("Login failed. Check your username and password.");
            }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
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
            System.out.println("Error during registration: " + e.getMessage());
        }
    }

    private void logout() {
        try {
            serverFacade.logout(authToken);
            loggedIn = false;
            authToken = null;
            System.out.println("Logged out successfully.");
        } catch (Exception e) {
            System.out.println("Error during logout: " + e.getMessage());
        }
    }

    private void createGame() {
        System.out.print("Enter a name for the new game: ");
        String gameName = scanner.nextLine();
        System.out.print("Choose color (white or black): ");
        String playerColor = scanner.nextLine().trim().toLowerCase();

        if (!playerColor.equals("white") && !playerColor.equals("black")) {
            System.out.println("Invalid color. Please choose 'white' or 'black'.");
            return;
        }

        try {
            GameData gameData = serverFacade.createGame(authToken, gameName, playerColor);  // Create game
            currentGameId = gameData.getGameID();  // Set the currentGameId for the created game
            inGame = true;
            System.out.println("Game created successfully. You are now in the game.");
        } catch (Exception e) {
            System.out.println("Error creating game: " + e.getMessage());
        }
    }

    private void listGames() {
        try {
            var games = serverFacade.listGames(authToken);
            if (games.isEmpty()) {
                System.out.println("No games available.");
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
            serverFacade.joinGame(authToken, gameId, playerColor);  // Join game with token, gameId, and color
            currentGameId = gameId;  // Set the currentGameId after successfully joining
            inGame = true;
            System.out.println("Joined game. Ready to play.");
        } catch (Exception e) {
            System.out.println("Error joining game: " + e.getMessage());
        }
    }

    private void observeGame() {
        System.out.print("Enter game number to observe: ");
        int gameId = Integer.parseInt(scanner.nextLine().trim());

        try {
            serverFacade.observeGame(authToken, gameId);
            inGame = true;
            System.out.println("Observing game.");
        } catch (Exception e) {
            System.out.println("Error observing game: " + e.getMessage());
        }
    }

    private void makeMove() {
        System.out.print("Enter your move (e.g., e2 e4): ");
        String move = scanner.nextLine();

        try {
            serverFacade.makeMove(currentGameId, move, authToken); // Provide gameId, move, and authToken
            System.out.println("Move accepted.");
        } catch (Exception e) {
            System.out.println("Invalid move: " + e.getMessage());
        }
    }


    private void exitGame() {
        inGame = false;
        System.out.println("Exited game.");
    }
}


