package service;

import dataaccess.AuthTokenDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import dataaccess.GameDAO;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    private ClearService clearService;
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthTokenDAO authTokenDAO;  // Rename for clarity

    @BeforeEach
    public void setUp() throws DataAccessException {
        clearService = new ClearService();
        userDAO = new UserDAO();
        gameDAO = new GameDAO();
        authTokenDAO = AuthTokenDAO.getInstance();  // Access the singleton instance

        // Insert some dummy data
        userDAO.insertUser(new UserData("testUser", "password", "test@example.com"));
        gameDAO.createGame("Test Game", "testUser", null);
        authTokenDAO.createAuth(new model.AuthData("token123", "testUser"));
    }

    @Test
    public void testClearDatabase() {
        // Clear the database
        clearService.clearDatabase();

        // Verify that the data has been cleared
        assertNull(userDAO.getUser("testUser"));
        assertNull(gameDAO.getGame(1));  // Assuming gameID 1
        assertNull(authTokenDAO.getAuth("token123"));
    }
}

