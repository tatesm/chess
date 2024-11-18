package ui;

import client.ServerFacade;
import ui.GameClient;
import ui.PostLoginClient;
import ui.PreLoginClient;

import java.util.Scanner;

public class MainRunner {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private AppState currentState;
    private String authToken;
    private int gameId;

    public MainRunner(ServerFacade serverFacade, Scanner scanner) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
        this.currentState = AppState.PRELOGIN;
        this.authToken = null;
        this.gameId = -1;
    }

    public void run() {
        while (currentState != AppState.EXIT) {
            switch (currentState) {
                case PRELOGIN -> handlePreLogin();
                case POSTLOGIN -> handlePostLogin();
                case GAME -> handleGame();
            }
        }
    }

    private void handlePreLogin() {
        PreLoginClient preLoginClient = new PreLoginClient(serverFacade, scanner);
        String result = preLoginClient.run();

        if ("quit".equals(result)) {
            currentState = AppState.EXIT;
        } else {
            authToken = result;
            currentState = AppState.POSTLOGIN;
        }
    }

    private void handlePostLogin() {
        PostLoginClient postLoginClient = new PostLoginClient(serverFacade, scanner, authToken);
        String result = postLoginClient.run();

        if ("logout".equals(result)) {
            currentState = AppState.PRELOGIN;
        } else if ("game".equals(result)) {
            gameId = postLoginClient.getJoinedGameId();
            currentState = AppState.GAME;
        }
    }

    private void handleGame() {
        if (gameId == -1 || authToken == null) {
            System.err.println("Invalid game state. Returning to post-login.");
            currentState = AppState.POSTLOGIN;
            return;
        }

        GameClient gameClient = new GameClient(serverFacade, scanner, gameId, authToken);
        String result = gameClient.run();

        switch (result) {
            case "postlogin" -> currentState = AppState.POSTLOGIN;
            case "logout" -> currentState = AppState.PRELOGIN;
            case "quit" -> currentState = AppState.EXIT;
        }
    }

    private enum AppState {
        PRELOGIN,
        POSTLOGIN,
        GAME,
        EXIT
    }
}

