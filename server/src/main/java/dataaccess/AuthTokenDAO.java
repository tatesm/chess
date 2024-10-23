package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class AuthTokenDAO {

    // In-memory storage for auth tokens
    private static Map<String, AuthData> authTokens = new HashMap<>();

    // Singleton instance of AuthTokenDAO
    private static AuthTokenDAO instance = new AuthTokenDAO();

    // Private constructor to enforce singleton pattern
    private AuthTokenDAO() {
    }

    // Static method to get the singleton instance
    public static AuthTokenDAO getInstance() {
        return instance;
    }

    // Method to store a new auth token
    public void createAuth(AuthData authData) {
        authTokens.put(authData.authToken(), authData);
    }

    // Method to retrieve an auth token
    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    // Method to delete an auth token
    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }

    // Method to clear all auth tokens (useful for testing and resetting)
    public void clearAuthTokens() {
        authTokens.clear();
    }
}
