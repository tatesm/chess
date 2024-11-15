package created;

import model.GameData;

import java.util.List;

public class CreatedStuff {

    // Class for creating a new game
    public static class CreateGameRequest {
        private String gameName;
        private String playerColor;
        private String username;

        // Constructor
        public CreateGameRequest(String gameName, String playerColor, String username) {
            this.gameName = gameName;
            this.playerColor = playerColor;
            this.username = username;
        }

        // Getters
        public String getGameName() {
            return gameName;
        }

        public String getPlayerColor() {
            return playerColor;
        }

        public String getUsername() {
            return username;
        }

        // Optional Setters
        public void setGameName(String gameName) {
            this.gameName = gameName;
        }

        public void setPlayerColor(String playerColor) {
            this.playerColor = playerColor;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }

    // Class for joining an existing game
    public static class JoinGameRequest {
        private int gameID;
        private String playerColor;

        // Constructor
        public JoinGameRequest(int gameID, String playerColor) {
            this.gameID = gameID;
            this.playerColor = playerColor;
        }

        // Getters
        public int getGameID() {
            return gameID;
        }

        public String getPlayerColor() {
            return playerColor;
        }

        // Optional Setters
        public void setGameID(int gameID) {
            this.gameID = gameID;
        }

        public void setPlayerColor(String playerColor) {
            this.playerColor = playerColor;
        }
    }

    // Class for handling a list of games response
    public static class GamesResponse {
        private List<GameData> games;

        // Constructor
        public GamesResponse(List<GameData> games) {
            this.games = games;
        }

        // Getter
        public List<GameData> getGames() {
            return games;
        }

        // Optional Setter
        public void setGames(List<GameData> games) {
            this.games = games;
        }
    }
}

