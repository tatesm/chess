import chess.*;
import client.ServerFacade;
import ui.GameClient;
import ui.PostLoginClient;
import ui.PreLoginClient;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        var serverUrl = args.length == 1 ? args[0] : "http://localhost:8080";
        var scanner = new Scanner(System.in);
        var serverFacade = new ServerFacade(serverUrl);

        AppState currentState = AppState.PRELOGIN;
        String authToken = null;
        int gameId = -1;

        while (currentState != AppState.EXIT) {
            if (currentState == AppState.PRELOGIN) {
                currentState = handlePreLogin(serverFacade, scanner);
            } else if (currentState == AppState.POSTLOGIN) {
                var result = handlePostLogin(serverFacade, scanner, authToken);
                currentState = result.nextState();
                authToken = result.authToken();
                gameId = result.gameId();
            } else if (currentState == AppState.GAME) {
                currentState = handleGame(serverFacade, scanner, authToken, gameId);
            }
        }

        System.out.println("Goodbye!");
    }

    private static AppState handlePreLogin(ServerFacade serverFacade, Scanner scanner) {
        PreLoginClient preLoginClient = new PreLoginClient(serverFacade, scanner);
        String result = preLoginClient.run();
        if ("quit".equals(result)) {
            return AppState.EXIT;
        }
        return AppState.POSTLOGIN;
    }

    private static PostLoginResult handlePostLogin(ServerFacade serverFacade, Scanner scanner, String authToken) {
        PostLoginClient postLoginClient = new PostLoginClient(serverFacade, scanner, authToken);
        String result = postLoginClient.run();

        if ("logout".equals(result)) {
            return new PostLoginResult(null, -1, AppState.PRELOGIN);
        } else if ("game".equals(result)) {
            int gameId = postLoginClient.getJoinedGameId();
            return new PostLoginResult(authToken, gameId, AppState.GAME);
        }

        return new PostLoginResult(authToken, -1, AppState.POSTLOGIN);
    }

    private static AppState handleGame(ServerFacade serverFacade, Scanner scanner, String authToken, int gameId) {
        if (gameId == -1 || authToken == null) {
            System.err.println("Invalid game state. Returning to post-login.");
            return AppState.POSTLOGIN;
        }

        GameClient gameClient = new GameClient(serverFacade, scanner, gameId, authToken);
        String result = gameClient.run();

        return switch (result) {
            case "postlogin" -> AppState.POSTLOGIN;
            case "logout" -> AppState.PRELOGIN;
            case "quit" -> AppState.EXIT;
            default -> AppState.GAME;
        };
    }

    public enum AppState {
        PRELOGIN,
        POSTLOGIN,
        GAME,
        EXIT
    }

    private static class PostLoginResult {
        private final String authToken;
        private final int gameId;
        private final AppState nextState;

        public PostLoginResult(String authToken, int gameId, AppState nextState) {
            this.authToken = authToken;
            this.gameId = gameId;
            this.nextState = nextState;
        }

        public String authToken() {
            return authToken;
        }

        public int gameId() {
            return gameId;
        }

        public AppState nextState() {
            return nextState;
        }
    }
}



