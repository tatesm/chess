package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class AuthTokenDAO {

    private final Map<String, AuthData> authTokens = new HashMap<>();
    private static AuthTokenDAO instance = new AuthTokenDAO();

    private AuthTokenDAO() {
    }

    public static AuthTokenDAO getInstance() {
        return instance;
    }

    public void createAuth(AuthData authData) {
        authTokens.put(authData.authToken(), authData);
    }

    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }

    public void clearAuthTokens() {
        authTokens.clear();
    }
}
