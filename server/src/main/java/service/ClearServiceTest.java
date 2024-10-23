package service;

import dataaccess.UserDAO;
import dataaccess.GameDAO;
import dataaccess.AuthDAO;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    private ClearService clearService;
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    @BeforeEach
    public void setUp() {
        clearService = new ClearService();
        userDAO = new UserDAO();
        gameDAO = new GameDAO();
        authDAO = new AuthDAO();

        // Insert some dummy data
        userDAO.insertUser(new UserData("testUser", "password", "test@example.com"));
        gameDAO.createGame("Test Game", "testUser", null);
        authDAO.createAuth(new model.AuthData("token123", "testUser"));
    }

    @Test
    public void testClearDatabase() {
        // Clear the database
        clearService.clearDatabase();

        // Verify that the data has been cleared
        assertNull(userDAO.getUser("testUser"));
        assertNull(gameDAO.getGame(1));  // Assuming gameID 1
        assertNull(authDAO.getAuth("token123"));
    }
}
