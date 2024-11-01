package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthTokenDAO {


    private static AuthTokenDAO instance = new AuthTokenDAO();

    private AuthTokenDAO() {
    }

    public static AuthTokenDAO getInstance() {
        return instance;
    }

    public void createAuth(AuthData authData) throws DataAccessException {
        String sql = "CREATE INTO auth_tokens (token, username) VALUES (?, ?, ?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authData.authToken());
            stmt.setString(2, authData.username());


            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DataAccessException("token already exists: " + authData.authToken());
            } else {
                throw new DataAccessException("Error creating token: " + e.getMessage());
            }
        }
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT * FROM auth_tokens WHERE token = ?;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            
            stmt.setString(1, authToken);


            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    String username = rs.getString("username");


                    return new AuthData(authToken, username);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving auth token: " + authToken, e);
        }
    }


    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }

    public void clearAuthTokens() {
        authTokens.clear();
    }
}

