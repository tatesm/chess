package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static String authToken;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        System.out.println("Started test HTTP server on port " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void setup() {
        try {
            facade.clearDatabase();
        } catch (Exception e) {
            fail("Failed to clear the database: " + e.getMessage());
        }
    }

    @Test
    void registerUserPositive() {
        try {
            AuthData authData = facade.register("testUser", "password", "test@example.com");
            assertNotNull(authData);
            assertNotNull(authData.authToken());
            authToken = authData.authToken();
        } catch (Exception e) {
            fail("Registration failed: " + e.getMessage());
        }
    }

    @Test
    void registerUserNegative() {
        try {
            // Register the same user twice to trigger an error
            facade.register("testUser", "password", "test@example.com");
            Exception exception = assertThrows(Exception.class, () ->
                    facade.register("testUser", "password", "test@example.com")
            );
            assertTrue(exception.getMessage().contains("Error: Forbidden"));
        } catch (Exception e) {
            fail("Unexpected failure: " + e.getMessage());
        }
    }

    @Test
    void loginUserPositive() {
        try {
            facade.register("testUser", "password", "test@example.com");
            AuthData authData = facade.login("testUser", "password");
            assertNotNull(authData);
            assertNotNull(authData.authToken());
            authToken = authData.authToken();
        } catch (Exception e) {
            fail("Login failed: " + e.getMessage());
        }
    }


    @Test
    void loginUserNegative() throws Exception {
        AuthData result = facade.login("nonexistentUser", "wrongPassword");
        assertNull(result, "Expected login to return null for invalid credentials");
    }


    @Test
    void createGamePositive() {
        try {
            facade.register("player", "password", "player@example.com");
            authToken = facade.login("player", "password").authToken();
            GameData gameData = facade.createGame(authToken, "New Game", "white");
            assertNotNull(gameData);
            assertEquals("New Game", gameData.getGameName());
        } catch (Exception e) {
            fail("Failed to create game: " + e.getMessage());
        }
    }

    @Test
    void createGameNegative() {
        Exception exception = assertThrows(Exception.class, () ->
                facade.createGame("invalidToken", "New Game", "white")
        );
        assertTrue(exception.getMessage().contains("Error: Unauthorized"));
    }

    @Test
    void listGamesPositive() {
        try {
            facade.register("player", "password", "player@example.com");
            authToken = facade.login("player", "password").authToken();
            facade.createGame(authToken, "Game1", "white");
            facade.createGame(authToken, "Game2", "black");

            List<GameData> games = facade.listGames(authToken);
            assertNotNull(games);
            assertEquals(2, games.size());
        } catch (Exception e) {
            fail("Failed to list games: " + e.getMessage());
        }
    }

    @Test
    void listGamesNegative() {
        Exception exception = assertThrows(Exception.class, () ->
                facade.listGames("invalidToken")
        );
        assertTrue(exception.getMessage().contains("Error: Unauthorized"),
                "Expected 'Error: Invalid auth token' error, but got: " + exception.getMessage());
    }

    @Test
    void joinGamePositive() {
        try {
            facade.register("player", "password", "player@example.com");
            authToken = facade.login("player", "password").authToken();
            GameData gameData = facade.createGame(authToken, "Joinable Game", "white");
            facade.joinGame(authToken, gameData.getGameID(), "black");
        } catch (Exception e) {
            fail("Failed to join game: " + e.getMessage());
        }
    }

    @Test
    void joinGameNegative() {
        Exception exception = assertThrows(Exception.class, () ->
                facade.joinGame("invalidToken", 1, "black")
        );
        assertTrue(exception.getMessage().contains("Error: Invalid auth token"),
                "Expected 'Error: Invalid auth token' error, but got: " + exception.getMessage());
    }
/*
    @Test
    void makeMovePositive() {
        try {
            facade.register("player", "password", "player@example.com");
            authToken = facade.login("player", "password").authToken();
            GameData gameData = facade.createGame(authToken, "Game with Move", "white");
            facade.makeMove(gameData.getGameID(), "e2e4", authToken); // Example move
        } catch (Exception e) {
            fail("Failed to make a move: " + e.getMessage());
        }
    }

    @Test
    void makeMoveNegative() {
        Exception exception = assertThrows(Exception.class, () ->
                facade.makeMove(1, "e2e4", "invalidToken")
        );
        assertTrue(exception.getMessage().contains("Error: Unauthorized"),
                "Expected 'Error: Unauthorized' but got: " + exception.getMessage());
    }

    @Test
    void getBoardPositive() {
        try {
            facade.register("player", "password", "player@example.com");
            authToken = facade.login("player", "password").authToken();
            GameData gameData = facade.createGame(authToken, "Board Game", "white");
            String board = facade.getBoard(gameData.getGameID(), authToken);
            assertNotNull(board, "Board state should not be null");
        } catch (Exception e) {
            fail("Failed to retrieve board state: " + e.getMessage());
        }
    }

    @Test
    void getBoardNegative() {
        Exception exception = assertThrows(Exception.class, () ->
                facade.getBoard(1, "invalidToken")
        );
        assertTrue(exception.getMessage().contains("Error: Unauthorized"),
                "Expected 'Error: Unauthorized' but got: " + exception.getMessage());
    }

    @Test
    void quitGamePositive() {
        try {
            facade.register("player", "password", "player@example.com");
            authToken = facade.login("player", "password").authToken();
            GameData gameData = facade.createGame(authToken, "Quit Game", "white");
            boolean result = facade.quitGame(gameData.getGameID(), authToken);
            assertTrue(result, "Expected to successfully quit the game");
        } catch (Exception e) {
            fail("Failed to quit game: " + e.getMessage());
        }
    }

    @Test
    void quitGameNegative() {
        Exception exception = assertThrows(Exception.class, () ->
                facade.quitGame(1, "invalidToken")
        );
        assertTrue(exception.getMessage().contains("Error: Unauthorized"),
                "Expected 'Error: Unauthorized' but got: " + exception.getMessage());
    }

    @Test
    void observeGamePositive() {
        try {
            facade.register("observer", "password", "observer@example.com");
            authToken = facade.login("observer", "password").authToken();
            GameData gameData = facade.createGame(authToken, "Observable Game", "white");
            facade.observeGame(authToken, gameData.getGameID());
        } catch (Exception e) {
            fail("Failed to observe game: " + e.getMessage());
        }
    }

    @Test
    void observeGameNegative() {
        Exception exception = assertThrows(Exception.class, () ->
                facade.observeGame("invalidToken", 1)
        );
        assertTrue(exception.getMessage().contains("Error: Unauthorized"),
                "Expected 'Error: Unauthorized' but got: " + exception.getMessage());
    }

    @Test
    void logoutPositive() {
        try {
            facade.register("player", "password", "player@example.com");
            authToken = facade.login("player", "password").authToken();
            facade.logout(authToken);
        } catch (Exception e) {
            fail("Failed to log out: " + e.getMessage());
        }
    }

    @Test
    void logoutNegative() {
        Exception exception = assertThrows(Exception.class, () ->
                facade.logout("invalidToken")
        );
        assertTrue(exception.getMessage().contains("Error: Unauthorized"),
                "Expected 'Error: Unauthorized' but got: " + exception.getMessage());
    }
*/

}

