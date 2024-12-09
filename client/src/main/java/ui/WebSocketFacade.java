package ui;

import websocket.commands.UserGameCommand;

public class WebSocketFacade {
    private final WebSocketCommunicator communicator;

    public WebSocketFacade(WebSocketCommunicator communicator) {
        this.communicator = communicator;
    }

    public void makeMove(int gameId, String move, String authToken) throws Exception {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId, move);
        communicator.sendCommand(command);
    }

    public void resignGamePlayer(int gameId, String authToken) throws Exception {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId);
        communicator.sendCommand(command);
    }

    public void leaveGameObserver(int gameId, String authToken) throws Exception {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId);
        communicator.sendCommand(command);
    }

    public void highlightLegalMoves(int gameId, String position, String authToken) throws Exception {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEGAL_MOVES, authToken, gameId, position);
        communicator.sendCommand(command);
    }

    public void redrawBoard(int gameId, String authToken) throws Exception {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.REDRAW_BOARD, authToken, gameId);
        communicator.sendCommand(command);
    }
}
