package ui;

import chess.ChessBoard;
import client.ServerFacade;
import ui.GameClient;
import ui.PostLoginClient;
import ui.PreLoginClient;

import java.util.Scanner;

public class MainRunner {
    private final ServerFacade serverFacade;
    private final WebSocketFacade webSocketFacade;
    private final Scanner scanner;
    private AppState currentState;
    private String authToken;
    private int gameId;
    private ChessBoard chessBoard;

    public MainRunner(ServerFacade serverFacade, WebSocketFacade webSocketFacade, Scanner scanner) {
        this.serverFacade = serverFacade;
        this.webSocketFacade = webSocketFacade;
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
        PostLoginClient postLoginClient = new PostLoginClient(serverFacade, webSocketFacade, scanner, authToken);
        String result = postLoginClient.run();

        if ("logout".equals(result)) {
            currentState = AppState.PRELOGIN;
        } else if ("play game".equals(result)) {
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

        GameClient gameClient = new GameClient(scanner, webSocketFacade, gameId, authToken);
        String result = gameClient.run();

        if (result == null) {
            System.err.println("Unexpected state. Returning to post-login.");
            currentState = AppState.POSTLOGIN;
            return;
        }

        switch (result) {
            case "postlogin", "leave", "resign" -> currentState = AppState.POSTLOGIN;
            case "logout" -> currentState = AppState.PRELOGIN;
            case "quit" -> currentState = AppState.EXIT;
            default -> {
                System.err.println("Unknown result: " + result + ". Returning to post-login.");
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

