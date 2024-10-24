package model;


import chess.ChessGame;

public class GameData {
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;
    private ChessGame game;


    public GameData(int gameID, String gameName, ChessGame game) {
        this.gameID = gameID;
        this.gameName = gameName;
        this.game = game;

    }


    public int getGameID() {
        return gameID;
    }
    

    public void setWUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public void setBUsername(String blackUsername) {
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

}

