package service;

import dataaccess.AuthTokenDAO;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class GSTest {

    private GameService gameService;
    private GameDAO gameDAO;
    private AuthTokenDAO authTokenDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.createDatabase();
        databaseManager.configureDatabase();
        gameDAO = new GameDAO();
        authTokenDAO = AuthTokenDAO.getInstance();
        gameService = new GameService(gameDAO, authTokenDAO);
        gameDAO.clearGames();
    }

    @Test
    public void testCreateGameSuccess() throws DataAccessException {

        UserData user = new UserData("testUser", "password", "email@example.com");
        String authToken = "newuuidAuthToken";
        authTokenDAO.createAuth(new AuthData(authToken, user.username()));


        GameData gameData = gameService.createGame("Test Game", user.username(), "WHITE");


        assertNotNull(gameData, "GameData should not be null");
        assertEquals("Test Game", gameData.getGameName(), "Game name should match");
    }

    @Test
    public void testJoinGameSuccess() throws DataAccessException {

        GameData gameData = gameService.createGame("Test Game", "testUser", "WHITE");


        String authToken = "validAuthToken";
        authTokenDAO.createAuth(new AuthData(authToken, "testUser"));


        GameData joinedGame = gameService.joinGame(authToken, gameData.getGameID(), "BLACK");
        assertEquals("testUser", joinedGame.getBlackUsername(), "Black username should match the user who joined");
    }

    @Test
    public void testJoinGameWithTakenSlot() throws DataAccessException {

        GameData gameData = gameService.createGame("Test Game", "testUser", "WHITE");


        gameService.joinGame("validAuthToken", gameData.getGameID(), "WHITE");


        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("validAuthToken", gameData.getGameID(), "WHITE");
        }, "Expected DataAccessException when trying to join an already taken slot");
    }

    @Test
    public void testJoinGameWithInvalidAuthToken() throws DataAccessException {
        GameData gameData = gameService.createGame("Test Game", "testUser", "WHITE");
        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("invalidAuthToken", gameData.getGameID(), "BLACK");
        }, "Expected DataAccessException for invalid auth token");
    }

    @Test
    public void testListGamesNoGames() throws DataAccessException {
        List<GameData> games = gameService.listGames();
        assertTrue(games.isEmpty(), "List should be empty when no games have been created");
    }

    @Test
    public void testListGamesWithGames() throws DataAccessException {
        gameService.createGame("Test Game 1", "testUser1", "WHITE");
        gameService.createGame("Test Game 2", "testUser2", "BLACK");

        List<GameData> games = gameService.listGames();
        assertEquals(2, games.size(), "List should contain two games");
    }
}

