package model;


public class GameData {
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;
    private Object chessGame;


    public GameData(int gameID, String whiteUsername, String blackUsername, String gameName, Object chessGame) {
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
        this.chessGame = chessGame;

    }


    public int getGameID() {
        return gameID;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public void setWUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public void setBUsername(String blackUsername) {
        this.blackUsername = blackUsername;
    }

}

