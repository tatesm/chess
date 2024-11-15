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
    void registerUser_positive() {
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
    void registerUser_negative() {
        try {
            // Register the same user twice to trigger an error
            facade.register("testUser", "password", "test@example.com");
            Exception exception = assertThrows(Exception.class, () ->
                    facade.register("testUser", "password", "test@example.com")
            );
            assertTrue(exception.getMessage().contains("Username already taken"));
        } catch (Exception e) {
            fail("Unexpected failure: " + e.getMessage());
        }
    }

    @Test
    void loginUser_positive() {
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
    void loginUser_negative() throws Exception {
        AuthData result = facade.login("nonexistentUser", "wrongPassword");
        assertNull(result, "Expected login to return null for invalid credentials");
    }


    @Test
    void createGame_positive() {
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
    void createGame_negative() {
        Exception exception = assertThrows(Exception.class, () ->
                facade.createGame("invalidToken", "New Game", "white")
        );
        assertTrue(exception.getMessage().contains("Invalid authorization token"));
    }

    @Test
    void listGames_positive() {
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
    void listGames_negative() {
        Exception exception = assertThrows(Exception.class, () ->
                facade.listGames("invalidToken")
        );
        assertTrue(exception.getMessage().contains("Error: Invalid auth token"),
                "Expected 'Error: Invalid auth token' error, but got: " + exception.getMessage());
    }

    @Test
    void joinGame_positive() {
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
    void joinGame_negative() {
        Exception exception = assertThrows(Exception.class, () ->
                facade.joinGame("invalidToken", 1, "black")
        );
        assertTrue(exception.getMessage().contains("Error: Invalid auth token"),
                "Expected 'Error: Invalid auth token' error, but got: " + exception.getMessage());
    }


}

