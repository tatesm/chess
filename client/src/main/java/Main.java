import chess.*;
import client.ServerFacade;

import ui.MainRunner;
import ui.WebSocketFacade;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        var serverUrl = args.length == 1 ? args[0] : "http://localhost:8080";
        var scanner = new Scanner(System.in);
        var serverFacade = new ServerFacade(serverUrl);

        WebSocketFacade webSocketFacade = new WebSocketFacade();
        MainRunner appRunner = new MainRunner(serverFacade, scanner, webSocketFacade);
        appRunner.run(); // Delegate the loop to the ApplicationRunner class

        System.out.println("Goodbye!");
    }
}


