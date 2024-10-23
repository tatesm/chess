package dataaccess;

public class clearservice {
    public void clearDatabase() {
        // Clear the in-memory users, games, and auth tokens
        UserDAO.getInstance().clearUsers();
        GameDAO.getInstance().clearGames();
        AuthTokenDAO.getInstance().clearAuthTokens();
}
