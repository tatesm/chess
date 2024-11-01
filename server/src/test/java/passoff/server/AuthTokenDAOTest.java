package passoff.server;

import dataaccess.AuthTokenDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenDAOTest {

    private AuthTokenDAO authTokenDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        authTokenDAO = AuthTokenDAO.getInstance();
        authTokenDAO.clearAuthTokens();
    }

    @Test
    void testCreateAuth_Success() throws DataAccessException {

        String token = "newToken";
        String username = "newUser";
        AuthData authData = new AuthData(token, username);


        authTokenDAO.createAuth(authData);


        AuthData retrievedAuthData = authTokenDAO.getAuth(token);
        assertNotNull(retrievedAuthData, "AuthData should not be null after insertion");
        assertEquals(username, retrievedAuthData.username(), "Username should match the inserted username");
    }

    @Test
    void testCreateAuth_DuplicateToken() throws DataAccessException {

        String token = "duplicateToken";
        String username = "firstUser";
        AuthData firstAuthData = new AuthData(token, username);

        authTokenDAO.createAuth(firstAuthData);


        AuthData secondAuthData = new AuthData(token, "secondUser");
        assertThrows(DataAccessException.class, () -> authTokenDAO.createAuth(secondAuthData),
                "Inserting a duplicate token should throw DataAccessException");
    }

    @Test
    void testDeleteAuth_Success() throws DataAccessException {
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
    void testDeleteAuth_NonExistentToken() throws DataAccessException {

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
    void testGetAuth_NotFound() throws DataAccessException {

        AuthData result = authTokenDAO.getAuth("nonExistentToken");


        assertNull(result, "AuthData should be null for a non-existent token");
    }
}
