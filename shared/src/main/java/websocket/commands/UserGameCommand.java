package websocket.commands;

import chess.ChessMove;
import com.google.gson.Gson;

public class UserGameCommand {

    private final CommandType commandType;
    private final String authToken;
    private final Integer gameID;
    private final String square; // Optional field for commands like LEGAL_MOVES

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this(commandType, authToken, gameID, null);
    }

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, String square) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
        this.square = square;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        REDRAW_BOARD,
        LEGAL_MOVES,
        RESIGN
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    public String getSquare() {
        return square;
    }

    /**
     * Retrieve the ChessMove if this is a MAKE_MOVE command.
     *
     * @param gson The Gson instance used to deserialize the command.
     * @return The ChessMove for the command, or null if not a MAKE_MOVE command.
     */
    public ChessMove getMove(Gson gson, String message) {
        if (commandType == CommandType.MAKE_MOVE) {
            return gson.fromJson(message, MakeMoveCommand.class).getMove();
        }
        return null;
    }

    /**
     * Retrieve the square if this is a LEGAL_MOVES command.
     *
     * @param gson    The Gson instance used to deserialize the command.
     * @param message The JSON message to parse.
     * @return The square as a String, or null if not a LEGAL_MOVES command.
     */
    public String getSquare(Gson gson, String message) {
        if (commandType == CommandType.LEGAL_MOVES) {
            return gson.fromJson(message, LegalMovesCommand.class).getSquare();
        }
        return null;
    }

    // Inner class to handle deserialization for MAKE_MOVE
    private static class MakeMoveCommand {
        private ChessMove move;

        public ChessMove getMove() {
            return move;
        }
    }

    // Inner class to handle deserialization for LEGAL_MOVES
    private static class LegalMovesCommand {
        private String square;

        public String getSquare() {
            return square;
        }
    }
}
