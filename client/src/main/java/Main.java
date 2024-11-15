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

        while (true) {

            PreLoginClient preloginClient = new PreLoginClient(serverFacade, scanner);
            preloginClient.run();

            PostLoginClient postLoginClient = new PostLoginClient(serverFacade, scanner);
            postLoginClient.run();

            int gameId = postLoginClient.getJoinedGameId();
            String authToken = postLoginClient.getAuthToken(); // Retrieve authToken

            if (gameId != -1 && authToken != null) {
                GameClient gameClient = new GameClient(serverFacade, scanner, gameId, authToken); // Pass authToken
                gameClient.run();
            }
        }
    }
}