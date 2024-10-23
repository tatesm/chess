package dataaccess;

import chess.ChessGame;
import chess.ChessPiece;
import server.Server;

public class UserDAO {
    public static void main(String[] args) {
        // Initialize the chess piece for display
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        // Create the Server object and run it on port 8080
        Server server = new Server();
        int runningPort = server.run(8080);  // Starts the server on the specified port

        // Confirming server start
        System.out.println("Server is running on port " + runningPort + ". Go to http://localhost:" + runningPort);
}
