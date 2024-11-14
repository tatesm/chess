package client;

import ui.PreloginClient;
import ui.PostloginClient;
import ui.GameClient;
import client.ServerFacade;

import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }

        ServerFacade serverFacade = new ServerFacade(serverUrl);
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Start in PreloginClient
            PreloginClient preloginClient = new PreloginClient(serverFacade, scanner);
            preloginClient.run();

            // After successful login, move to PostloginClient
            PostloginClient postloginClient = new PostloginClient(serverFacade, scanner);
            postloginClient.run();

            // If a game is joined, enter GameClient
            int gameId = postloginClient.getJoinedGameId();
            if (gameId != -1) {  // Check if a game has been joined
                GameClient gameClient = new GameClient(serverFacade, scanner, gameId);
                gameClient.run();
            }
        }
    }
}

