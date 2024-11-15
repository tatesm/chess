import chess.*;
import chess.ChessGame;
import chess.ChessPiece;
import client.ServerFacade;
import ui.GameClient;
import ui.PostLoginClient;
import ui.PreLoginClient;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        var scanner = new Scanner(System.in);
        var serverFacade = new ServerFacade(serverUrl);

        // Define application states
        enum AppState {PRELOGIN, POSTLOGIN, GAME, EXIT}

        AppState currentState = AppState.PRELOGIN;
        String authToken = null;
        int gameId = -1;

        while (currentState != AppState.EXIT) {
            switch (currentState) {
                case PRELOGIN -> {
                    PreLoginClient preLoginClient = new PreLoginClient(serverFacade, scanner);
                    String result = preLoginClient.run();

                    if (result.equals("quit")) {
                        currentState = AppState.EXIT;
                    } else {
                        authToken = result;
                        currentState = AppState.POSTLOGIN;
                    }
                }
                case POSTLOGIN -> {
                    PostLoginClient postLoginClient = new PostLoginClient(serverFacade, scanner, authToken);
                    String result = postLoginClient.run();

                    if (result.equals("logout")) {
                        currentState = AppState.PRELOGIN;
                    } else if (result.equals("game")) {
                        gameId = postLoginClient.getJoinedGameId();
                        currentState = AppState.GAME;
                    }
                }
                case GAME -> {
                    if (gameId != -1 && authToken != null) {
                        GameClient gameClient = new GameClient(serverFacade, scanner, gameId, authToken);
                        String result = gameClient.run();

                        if (result.equals("postlogin")) {
                            currentState = AppState.POSTLOGIN;
                        } else if (result.equals("logout")) {
                            currentState = AppState.PRELOGIN;
                        } else if (result.equals("quit")) {
                            currentState = AppState.EXIT;
                        }
                    } else {
                        System.err.println("Invalid game state. Returning to post-login.");
                        currentState = AppState.POSTLOGIN;
                    }
                }
            }
        }
        System.out.println("Goodbye!");
    }
}
