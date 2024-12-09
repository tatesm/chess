package websocket.commands;

import chess.ChessMove;
import com.google.gson.Gson;

public class UserGameCommand {

    private final CommandType commandType;
    private final String authToken;
    private final Integer gameID;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        REDRAW_BOARD, LEGAL_MOVES, RESIGN
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

    /**
     * Retrieve the ChessMove if this is a MAKE_MOVE command.
     *
     * @param gson The Gson instance used to deserialize the command.
     * @return The ChessMove for the command, or null if not a MAKE_MOVE command.
     */
    public ChessMove getMove(Gson gson, String message) {
        if (commandType == CommandType.MAKE_MOVE) {
            // Deserialize the move from the JSON message
            return gson.fromJson(message, MakeMoveCommand.class).getMove();
        }
        return null;
    }

    // Inner class to handle the deserialization of the move
    private static class MakeMoveCommand {
        private ChessMove move;

        public ChessMove getMove() {
            return move;
        }
    }
}
