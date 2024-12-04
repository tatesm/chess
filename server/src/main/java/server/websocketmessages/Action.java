package server.websocketmessages;

public class Action {
    private String type;
    private String playerName;
    private String move;

    public String type() {
        return type;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getMove() {
        return move;
    }
}
