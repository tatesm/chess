package service;

import dataaccess.AuthTokenDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class USTest {

    private UserService userService;
    private UserDAO userDAO;
    private AuthTokenDAO authTokenDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO = new UserDAO();
        authTokenDAO = AuthTokenDAO.getInstance();
        userService = new UserService(userDAO, authTokenDAO);
        userDAO.clearUsers(); // Ensure a clean state before each test
        authTokenDAO.clearAuthTokens(); // Clear any existing tokens
    }

    @Test
    public void testRegisterUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("testUser", "password", "email@example.com");
        AuthData authData = userService.register(newUser);
        assertNotNull(authData);
        assertEquals("testUser", authData.username());
    }

    @Test
    public void testRegisterDuplicateUser() throws DataAccessException {
        UserData newUser = new UserData("testUser", "password", "email@example.com");
        userService.register(newUser);

        assertThrows(DataAccessException.class, () -> {
            userService.register(newUser); // Should throw due to duplicate username
        });
    }

    @Test
    public void testLoginSuccess() throws DataAccessException {
        UserData newUser = new UserData("testUser", "password", "email@example.com");
        userService.register(newUser);

        AuthData authData = userService.login(newUser);
        assertNotNull(authData);
        assertEquals("testUser", authData.username());
    }

    @Test
    public void testLoginInvalidUser() {
        UserData newUser = new UserData("testUser", "password", "email@example.com");

        assertThrows(DataAccessException.class, () -> {
            userService.login(newUser); // Should throw due to invalid username
        });
    }

    @Test
    public void testLoginWrongPassword() throws DataAccessException {
        UserData newUser = new UserData("testUser", "password", "email@example.com");
        userService.register(newUser);

        UserData loginUser = new UserData("testUser", "wrongPassword", "email@example.com");
        assertThrows(DataAccessException.class, () -> {
            userService.login(loginUser); // Should throw due to wrong password
        });
    }

    @Test
    public void testGetUserSuccess() throws DataAccessException {
        UserData newUser = new UserData("testUser", "password", "email@example.com");
        userService.register(newUser);

        UserData foundUser = userService.getUser("testUser");
        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.username());
    }

    @Test
    public void testGetUserNotFound() throws DataAccessException {
        assertNull(userService.getUser("nonExistentUser")); // Should return null for non-existent user
    }
}
