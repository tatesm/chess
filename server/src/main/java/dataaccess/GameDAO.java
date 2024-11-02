package dataaccess;

import model.GameData;
import com.google.gson.Gson;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {
    private Object chessGameState;
    String gameStateJson = GSON.toJson(chessGameState);


    private static final Gson GSON = new Gson();

    public GameData createGame(String gameName, String username, String playerColor) throws DataAccessException {
        String sql = "INSERT INTO games (game_name, game_state) VALUES (?, NULL)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, gameName);


            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int gameID = rs.getInt(1);
                    return new GameData(gameID, gameName, null);
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
                    String whiteUsername = rs.getString("white_username");
                    String blackUsername = rs.getString("black_username");

                    GameData gameData = new GameData(gameID, gameName, null);
                    gameData.setWhiteUsername(whiteUsername);
                    gameData.setBlackUsername(blackUsername);

                    return gameData;
                }
            }
        } catch (SQLException e) {
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
        String sql = "UPDATE games SET game_state = NULL, white_username = ?, black_username = ? WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, game.getWhiteUsername());
            stmt.setString(2, game.getBlackUsername());
            stmt.setInt(3, game.getGameID());

            int rowsUpdated = stmt.executeUpdate();
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

                GameData gameData = new GameData(gameID, gameName, null);
                gameData.setWhiteUsername(rs.getString("white_username"));
                gameData.setBlackUsername(rs.getString("black_username"));
                games.add(gameData);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
        return games;
    }
}


