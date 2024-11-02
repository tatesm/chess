package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // checks if the password matches the stored hashed password for the user
    public boolean verifyUser(String username, String password) throws DataAccessException {
        String hashedPassword = fetchHashedPassword(username);
        return hashedPassword != null && BCrypt.checkpw(password, hashedPassword);
    }

    // stores a user password after hashing it
    public void storeUserPassword(String username, String clearTextPassword) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
        savePassword(username, hashedPassword);
    }

    // updates the users password in the database
    private void savePassword(String username, String hashedPassword) throws DataAccessException {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);
            if (stmt.executeUpdate() == 0) {
                throw new DataAccessException("No such user: " + username);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update password: " + e.getMessage());
        }
    }

    // grabs the hashed password from the database
    private String fetchHashedPassword(String username) throws DataAccessException {
        String sql = "SELECT password FROM users WHERE username = ?";
        // query database for hashed password
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("password") : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not retrieve password for: " + username + ". Reason: " + e.getMessage());
        }
    }

    // inserts new user rec into the database
    public void insertUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.username());
            stmt.setString(2, user.password());
            stmt.setString(3, user.email());
            stmt.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // dupe username
                throw new DataAccessException("User already exists: " + user.username());
            } else {
                throw new DataAccessException("Could not insert user: " + e.getMessage());
            }
        }
    }

    // retrieves users data from the database
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ?";

        // query datab and create userdata if need to
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user: " + username + ". Cause: " + e.getMessage());
        }
        return null;
    }

    // deletes all user info.
    public void clearUsers() throws DataAccessException {
        String sql = "DELETE FROM users";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear users: " + e.getMessage());
        }
    }
}

