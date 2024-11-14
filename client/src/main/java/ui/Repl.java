package ui;

import ServerFacade.java;

import java.util.Scanner;

public class Repl {
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade serverFacade;
    private boolean loggedIn = false;
    private boolean inGame = false;

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
        boolean continueLoop = true;
        while (continueLoop && !loggedIn) {
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
            continueLoop = !loggedIn;  // Exit loop if logged in
        }
    }

    private void postLoginLoop() {
        boolean continueLoop = true;
        while (continueLoop && loggedIn && !inGame) {
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
            continueLoop = loggedIn && !inGame;  // Exit loop if logged out or in a game
        }
    }

    private void gameplayLoop() {
        boolean continueLoop = true;
        while (continueLoop && inGame) {
            System.out.println("Gameplay commands: move, exit game");
            System.out.print("Enter command: ");
            String command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "move" -> makeMove();
                case "exit game" -> exitGame();
                default -> System.out.println("Invalid command. Type 'move' or 'exit game'.");
            }
            continueLoop = inGame;  // Exit loop if the user exits the game
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

        if (serverFacade.login(username, password)) {
            loggedIn = true;
            System.out.println("Login successful.");
        } else {
            System.out.println("Login failed. Check your username and password.");
        }
    }

    private void promptRegister() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        if (serverFacade.register(username, password, email)) {
            loggedIn = true;
            System.out.println("Registration successful.");
        } else {
            System.out.println("Registration failed. Try a different username.");
        }
    }

    private void logout() {
        serverFacade.logout();
        loggedIn = false;
        System.out.println("Logged out successfully.");
    }

    private void createGame() {
        System.out.print("Enter a name for the new game: ");
        String gameName = scanner.nextLine();
        if (serverFacade.createGame(gameName)) {
            System.out.println("Game created successfully.");
        } else {
            System.out.println("Failed to create game.");
        }
    }

    private void listGames() {
        var games = serverFacade.listGames();
        if (games.isEmpty()) {
            System.out.println("No games available.");
        } else {
            System.out.println("Available games:");
            for (int i = 0; i < games.size(); i++) {
                System.out.println((i + 1) + ": " + games.get(i));
            }
        }
    }

    private void playGame() {
        System.out.print("Enter game number to play: ");
        int gameNumber = Integer.parseInt(scanner.nextLine().trim());
        if (serverFacade.joinGame(gameNumber, "play")) {
            inGame = true;
            System.out.println("Joined game. Ready to play.");
        } else {
            System.out.println("Failed to join game.");
        }
    }

    private void observeGame() {
        System.out.print("Enter game number to observe: ");
        int gameNumber = Integer.parseInt(scanner.nextLine().trim());
        if (serverFacade.joinGame(gameNumber, "observe")) {
            inGame = true;
            System.out.println("Observing game.");
        } else {
            System.out.println("Failed to observe game.");
        }
    }

    private void makeMove() {
        System.out.print("Enter your move (e.g., e2 e4): ");
        String move = scanner.nextLine();
        if (serverFacade.makeMove(move)) {
            System.out.println("Move accepted.");
        } else {
            System.out.println("Invalid move.");
        }
    }

    private void exitGame() {
        inGame = false;
        System.out.println("Exited game.");
    }
}

