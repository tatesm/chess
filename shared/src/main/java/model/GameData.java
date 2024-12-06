package model;


import chess.ChessGame;

public class GameData {
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;
    private ChessGame game;
    private boolean resigned;

    public GameData(int gameID, String gameName, ChessGame game) {
        this.gameID = gameID;
        this.gameName = gameName;
        this.game = game;

    }


    public int getGameID() {
        return gameID;
    }


    public void setWhiteUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public void setBlackUsername(String blackUsername) {
        this.blackUsername = blackUsername;
    }


    public String getWhiteUsername() {
        return whiteUsername;
    }

    public String getBlackUsername() {
        return blackUsername;
    }


    public String getGameName() {
        return gameName;
    }

    public ChessGame getGame() {
        return game;
    }

    public void setResigned(boolean resigned) {
        this.resigned = resigned;
    }

    /**
     * Checks if the game has been resigned.
     *
     * @return true if the game is resigned, false otherwise
     */
    public boolean isResigned() {
        return resigned;
    }
}

