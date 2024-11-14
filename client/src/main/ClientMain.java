package ui;


import client.ServerFacade;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        var scanner = new Scanner(System.in);
        var serverFacade = new ServerFacade(serverUrl);

        while (true) {
            // Start in the PreloginClient
            PreloginClient preloginClient = new PreloginClient(serverFacade, scanner);
            preloginClient.run();

            // After successful login, enter PostLoginClient
            PostLoginClient postLoginClient = new PostLoginClient(serverFacade, scanner);
            postLoginClient.run();

            // If playing a game, enter GameClient
            int gameId = postLoginClient.getJoinedGameId();
            if (gameId != -1) { // Example check if a game has been joined
                GameClient gameClient = new GameClient(serverFacade, scanner, gameId);
                gameClient.run();
            }
        }
    }
}
