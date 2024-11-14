package ui;


import client.ServerFacade;
import exception.ResponseException;

import java.util.Scanner;

public class GameClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;
    private final int gameId;

    public GameClient(ServerFacade serverFacade, Scanner scanner, int gameId) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
        this.gameId = gameId;
    }


    public void run() {
        while (true) {
            System.out.print("Enter command (move, display board, quit game): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "move" -> makeMove();
                    case "display board" -> displayBoard();
                    case "quit game" -> {
                        quitGame();
                        return; // Exit GameClient and go back to PostLoginClient
                    }
                    default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
                }
            } catch (ResponseException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void makeMove() throws ResponseException {
        System.out.print("Enter your move (e.g., e2 e4): ");
        String move = scanner.nextLine();
        serverFacade.makeMove(gameId, move);
        System.out.println("Move made.");
    }

    private void displayBoard() throws ResponseException {
        System.out.println("Current board:");
        System.out.println(serverFacade.getBoard(gameId));
    }

    private void quitGame() throws ResponseException {
        serverFacade.quitGame(gameId);
        System.out.println("Exited game.");
    }
}

