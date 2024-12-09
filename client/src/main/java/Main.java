import chess.*;
import client.ServerFacade;
import ui.MainRunner;
import ui.WebSocketFacade;
import ui.WebSocketCommunicator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Display client banner
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        // Default server URL or override with CLI argument
        var serverUrl = args.length == 1 ? args[0] : "ws://localhost:8080/ws";
        var scanner = new Scanner(System.in);

        // Create server and WebSocket instances
        var serverFacade = new ServerFacade(serverUrl);
        var webSocketCommunicator = WebSocketCommunicator.getInstance();

        try {
            // Connect to WebSocket server
            webSocketCommunicator.connect(serverUrl);
            System.out.println("Connected to WebSocket server!");

            // Initialize the WebSocket facade and application runner
            WebSocketFacade webSocketFacade = new WebSocketFacade(webSocketCommunicator);
            MainRunner appRunner = new MainRunner(serverFacade, scanner, webSocketFacade);

            // Start the main application loop
            appRunner.run();
        } catch (Exception e) {
            System.err.println("Failed to initialize application: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up and disconnect WebSocket
            webSocketCommunicator.disconnect();
            System.out.println("Goodbye!");
        }
    }
}
