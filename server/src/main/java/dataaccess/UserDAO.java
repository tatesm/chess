package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UserDAO {


    public void insertUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email());

            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DataAccessException("Username already exists: " + user.username());
            } else {
                throw new DataAccessException("Error inserting user: " + e.getMessage());
            }
        }
    }

    /*
        public UserData getUser(String username) {
            return users.get(username);
        }

        public void clearUsers() {
            users.clear();
        }*/
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    String userUsername = rs.getString("username");
                    String password = rs.getString("password");
                    String email = rs.getString("email");
                    return new UserData(userUsername, email, password);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user: " + username + ". " + e.getMessage());
        }
    }

    public void clearUsers() throws DataAccessException {
        String sql = "DELETE FROM users";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users: " + e.getMessage());
        }
    }


}

