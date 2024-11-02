package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenDAOTest {

    private AuthTokenDAO authTokenDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.createDatabase();
        databaseManager.configureDatabase();
        authTokenDAO = AuthTokenDAO.getInstance();
        authTokenDAO.clearAuthTokens();

    }

    @Test
    void testCreateAuthSuccess() throws DataAccessException {

        String token = "newToken";
        String username = "newUser";
        AuthData authData = new AuthData(token, username);


        authTokenDAO.createAuth(authData);


        AuthData retrievedAuthData = authTokenDAO.getAuth(token);
        assertNotNull(retrievedAuthData, "AuthData should not be null after insertion");
        assertEquals(username, retrievedAuthData.username(), "Username should match the inserted username");
    }

    @Test
    void testCreateAuthDuplicateToken() throws DataAccessException {

        String token = "duplicateToken";
        String username = "firstUser";
        AuthData firstAuthData = new AuthData(token, username);

        authTokenDAO.createAuth(firstAuthData);


        AuthData secondAuthData = new AuthData(token, "secondUser");
        assertThrows(DataAccessException.class, () -> authTokenDAO.createAuth(secondAuthData),
                "Inserting a duplicate token should throw DataAccessException");
    }

    @Test
    void testDeleteAuthSuccess() throws DataAccessException {
        // Arrange
        String token = "tokenToDelete";
        String username = "userToDelete";
        AuthData authData = new AuthData(token, username);
        authTokenDAO.createAuth(authData);


        authTokenDAO.deleteAuth(token);


        AuthData retrievedAuthData = authTokenDAO.getAuth(token);
        assertNull(retrievedAuthData, "AuthData should be null after deletion");
    }

    @Test
    void testDeleteAuthNonExistentToken() throws DataAccessException {

        assertDoesNotThrow(() -> authTokenDAO.deleteAuth("nonExistentToken"),
                "Deleting a non-existent token should not throw an exception");
    }

    @Test
    void testClearAuthTokens() throws DataAccessException {

        authTokenDAO.createAuth(new AuthData("token1", "user1"));
        authTokenDAO.createAuth(new AuthData("token2", "user2"));


        authTokenDAO.clearAuthTokens();


        assertNull(authTokenDAO.getAuth("token1"), "AuthData for token1 should be null after clearing");
        assertNull(authTokenDAO.getAuth("token2"), "AuthData for token2 should be null after clearing");
    }

    @Test
    void testGetAuthNotFound() throws DataAccessException {

        AuthData result = authTokenDAO.getAuth("nonExistentToken");


        assertNull(result, "AuthData should be null for a non-existent token");
    }

    @Test
    void testGetAuthFound() throws DataAccessException {
        String uuid = UUID.randomUUID().toString();
        authTokenDAO.createAuth(new AuthData(uuid, "done"));
        AuthData result = authTokenDAO.getAuth(uuid);


        assertNotNull(result, "AuthData token1 for a existent token");
    }
}
