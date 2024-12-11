package ui;

import chess.ChessBoard;
import client.ServerFacade;

import java.util.Scanner;

public class MainRunner {
    private final ServerFacade serverFacade;
    private final WebSocketFacade webSocketFacade;
    private final Scanner scanner;
    private AppState currentState;
    private String authToken;
    private int gameId;
    private final String serverUrl;

    public MainRunner(ServerFacade serverFacade, WebSocketFacade webSocketFacade, Scanner scanner, String serverUrl) {
        this.serverFacade = serverFacade;
        this.webSocketFacade = webSocketFacade;
        this.scanner = scanner;
        this.currentState = AppState.PRELOGIN;
        this.authToken = null;
        this.gameId = -1;
        this.serverUrl = serverUrl;
    }

    public void run() {
        while (currentState != AppState.EXIT) {
            try {
                switch (currentState) {
                    case PRELOGIN -> handlePreLogin();
                    case POSTLOGIN -> handlePostLogin();
                    case GAME -> handleGame();
                }
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
                currentState = AppState.POSTLOGIN; // Recover to post-login
            }
        }
        System.out.println("Exiting application. Goodbye!");
    }

    private void handlePreLogin() {
        PreLoginClient preLoginClient = new PreLoginClient(serverFacade, scanner);
        String result = preLoginClient.run();

        if ("quit".equals(result)) {
            currentState = AppState.EXIT;
        } else if (result != null) {
            authToken = result;
            currentState = AppState.POSTLOGIN;
        } else {
            System.err.println("Unexpected result in PreLoginClient. Staying in PRELOGIN.");
        }
    }

    private void handlePostLogin() {
        PostLoginClient postLoginClient = new PostLoginClient(serverFacade, webSocketFacade, scanner, authToken, serverUrl);
        String result = postLoginClient.run();

        if ("logout".equals(result)) {
            authToken = null; // Clear authentication token on logout
            currentState = AppState.PRELOGIN;
        } else if ("play game".equals(result)) {
            gameId = postLoginClient.getJoinedGameId();
            if (gameId != -1) {
                currentState = AppState.GAME;
            } else {
                System.err.println("No game joined. Staying in POSTLOGIN.");
            }
        } else if ("quit".equals(result)) {
            currentState = AppState.EXIT;
        } else {
            System.err.println("Unexpected result in PostLoginClient. Staying in POSTLOGIN.");
        }
    }

    private void handleGame() {
        if (gameId == -1 || authToken == null) {
            System.err.println("Invalid game state. Returning to POSTLOGIN.");
            currentState = AppState.POSTLOGIN;
            return;
        }

        GameClient gameClient = new GameClient(webSocketFacade, gameId, authToken, scanner);
        String result = gameClient.run();

        switch (result) {
            case "postlogin", "leave", "resign" -> currentState = AppState.POSTLOGIN;
            case "logout" -> {
                authToken = null; // Clear authentication token
                currentState = AppState.PRELOGIN;
            }
            case "quit" -> currentState = AppState.EXIT;
            default -> {
                System.err.println("Unknown result: " + result + ". Returning to POSTLOGIN.");
                currentState = AppState.POSTLOGIN;
            }
        }
    }

    private enum AppState {
        PRELOGIN,
        POSTLOGIN,
        GAME,
        EXIT
    }
}
