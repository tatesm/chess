package dataaccess;

import chess.ChessGame;
import model.GameData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {

    private static final Gson gson = new Gson();

    public GameData createGame(String gameName, String username, String playerColor) throws DataAccessException {
        String sql = "INSERT INTO games (game_name, game_state, username, player_color, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, gameName);
            stmt.setString(2, gson.toJson(new ChessGame())); // Empty initial game state serialized as JSON
            stmt.setString(3, username); // Set the username correctly
            stmt.setString(4, playerColor); // Set the player color correctly

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int gameID = rs.getInt(1);
                    return new GameData(gameID, gameName, new ChessGame()); // Empty initial state
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
        return null;
    }


    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM games WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String gameName = rs.getString("game_name");
                    String gameStateJson = rs.getString("game_state");
                    String whiteUsername = rs.getString("white_username");
                    String blackUsername = rs.getString("black_username");


                    ChessGame game = gson.fromJson(gameStateJson, ChessGame.class);


                    GameData gameData = new GameData(gameID, gameName, game);
                    gameData.setWhiteUsername(whiteUsername);
                    gameData.setBlackUsername(blackUsername);

                    System.out.println("getGame: Retrieved data - GameID: " + gameID + ", WhiteUsername: " + whiteUsername + ", BlackUsername: " + blackUsername);

                    return gameData;
                }
            }
        } catch (SQLException | JsonSyntaxException e) {
            throw new DataAccessException("Error retrieving game with ID " + gameID + ": " + e.getMessage());
        }
        return null;
    }


    public void clearGames() throws DataAccessException {
        String sql = "DELETE FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage());
        }
    }


    public void updateGame(GameData game) throws DataAccessException {
        // Base SQL for updating game_state and the dynamic SQL parts for white and black usernames
        StringBuilder sqlBuilder = new StringBuilder("UPDATE games SET game_state = ?");

        if (game.getWhiteUsername() != null) {
            sqlBuilder.append(", white_username = ?");
        }
        if (game.getBlackUsername() != null) {
            sqlBuilder.append(", black_username = ?");
        }
        sqlBuilder.append(" WHERE game_id = ?");

        String sql = sqlBuilder.toString();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;
            stmt.setString(index++, gson.toJson(game.getGame())); // Convert game state to JSON and set it

            // Conditionally set white and black usernames if they are present
            if (game.getWhiteUsername() != null) {
                stmt.setString(index++, game.getWhiteUsername());
            }
            if (game.getBlackUsername() != null) {
                stmt.setString(index++, game.getBlackUsername());
            }

            stmt.setInt(index, game.getGameID()); // Always set game_id at the end

            int rowsUpdated = stmt.executeUpdate();
            System.out.println("updateGame: Rows updated - " + rowsUpdated + " for GameID: " + game.getGameID());

            if (rowsUpdated == 0) {
                throw new DataAccessException("No rows updated. Game ID might be incorrect.");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }


    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        String sql = "SELECT * FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int gameID = rs.getInt("game_id");
                String gameName = rs.getString("game_name");
                String gameStateJson = rs.getString("game_state");
                ChessGame game = gson.fromJson(gameStateJson, ChessGame.class);
                games.add(new GameData(gameID, gameName, game));
            }
        } catch (SQLException | JsonSyntaxException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
        return games;
    }

}


