package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static dataaccess.DatabaseManager.getConnection;

public class GameDAO {

    public GameData createGame(String gameName, String username, String playerColor) throws DataAccessException {
        String sql = "INSERT INTO games (game_name, game_state, created_at) VALUES (?, '{}', CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, gameName);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int gameID = rs.getInt(1);
                    return new GameData(gameID, gameName, "{}"); // Consistent with HashMap: empty game state
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
                    String gameState = rs.getString("game_state");
                    String whiteUsername = rs.getString("white_username");
                    String blackUsername = rs.getString("black_username");
                    return new GameData(gameID, gameName, gameState, whiteUsername, blackUsername);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game with ID " + gameID + ": " + e.getMessage());
        }
        return null; // Consistent with HashMap behavior when game not found
    }


    public void clearGames() throws DataAccessException {
        String sql = "DELETE FROM games";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games", e);
        }
    }

    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET game_state = ?, last_updated = ? WHERE game_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, game.getGameState());
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(3, game.getGameID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + game.getGameID(), e);
        }
    }

    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        String sql = "SELECT * FROM games";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int gameID = rs.getInt("game_id");
                String gameName = rs.getString("game_name");
                String gameState = rs.getString("game_state");
                games.add(new GameData(gameID, gameName, gameState));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games", e);
        }
        return games;
    }
}


