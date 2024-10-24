package created;

import model.GameData;

import java.util.List;

public class CreatedStuff {
    public static class CreateGameRequest {
        private String gameName;
        private String playerColor;
        private String username;

        public String getGameName() {
            return gameName;
        }

        public String getPlayerColor() {
            return playerColor;
        }

        public String getUsername() {
            return username;
        }
    }

    public static class JoinGameRequest {
        private int gameID;
        private String playerColor;

        public int getGameID() {
            return gameID;
        }

        public String getPlayerColor() {
            return playerColor;
        }
    }

    public static class GamesResponse {
        private List<GameData> games;

        public GamesResponse(List<GameData> games) {
            this.games = games;
        }

        public List<GameData> getGames() {
            return games;
        }
    }
}
