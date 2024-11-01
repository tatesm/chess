package dataaccess;

import chess.ChessGame;
import model.GameData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static dataaccess.DatabaseManager.getConnection;

public class GameDAO {

    private static final Gson gson = new Gson();

    public GameData createGame(String gameName, String username, String playerColor) throws DataAccessException {
        String sql = "INSERT INTO games (game_name, game_state, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, gameName);
            stmt.setString(2, gson.toJson(new ChessGame())); // Empty initial game state serialized as JSON
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
                    ChessGame game = gson.fromJson(gameStateJson, ChessGame.class); // Deserialize JSON to ChessGame
                    return new GameData(gameID, gameName, game);
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
        String sql = "UPDATE games SET game_state = ?, white_username = ?, black_username = ? WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, gson.toJson(game.getGame()));
            stmt.setString(2, game.getWhiteUsername());
            stmt.setString(3, game.getBlackUsername());
            stmt.setInt(4, game.getGameID());
            stmt.executeUpdate();

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


