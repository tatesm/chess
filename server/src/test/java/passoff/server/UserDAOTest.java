package passoff.server;

import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {
    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDAO = new UserDAO();
        userDAO.clearUsers(); // Clear table before each test
    }

    @Test
    void testInsertUser() throws DataAccessException {
        // Positive case: Insert a new user
        UserData user = new UserData("testUser", "test@example.com", "password123");
        userDAO.insertUser(user);

        UserData retrievedUser = userDAO.getUser("testUser");
        assertNotNull(retrievedUser);
        assertEquals("testUser", retrievedUser.username());
    }

    @Test
    void testInsertDuplicateUser() {
        // Negative case: Insert duplicate user
        UserData user = new UserData("duplicateUser", "test@example.com", "password123");
        assertThrows(DataAccessException.class, () -> {
            userDAO.insertUser(user);
            userDAO.insertUser(user); // Attempt to insert the same user
        });
    }

    @Test
    void testGetUserNotFound() throws DataAccessException {
        // Negative case: Attempt to get a non-existent user
        UserData user = userDAO.getUser("nonExistentUser");
        assertNull(user);
    }

    @Test
    void testGetUserFound() throws DataAccessException {
        // Positive case: Attempt to get a existent user
        userDAO.insertUser(new UserData("user1", "user1@example.com", "password1"));
        UserData user = userDAO.getUser("user1");
        assertNotNull(user);
    }

    @Test
    void testClearUsers() throws DataAccessException {
        // Positive case: Clear users
        userDAO.insertUser(new UserData("user3", "user1@example.com", "password1"));
        userDAO.insertUser(new UserData("user2", "user2@example.com", "password2"));

        userDAO.clearUsers();

        assertNull(userDAO.getUser("user3"));
        assertNull(userDAO.getUser("user2"));
    }

    @Test
    void testClearUsersNegative() throws DataAccessException {
        // Negative: not clear users
        userDAO.insertUser(new UserData("user3", "user1@example.com", "password1"));
        userDAO.insertUser(new UserData("user2", "user2@example.com", "password2"));

        userDAO.clearUsers();

        assertNull(userDAO.getUser("user3"));
        assertNull(userDAO.getUser("user2"));
    }
}
