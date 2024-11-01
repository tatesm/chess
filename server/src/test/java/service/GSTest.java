package service;

import dataaccess.AuthTokenDAO;
import dataaccess.DataAccessException;
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
    public void setUp() {
        gameDAO = new GameDAO();
        authTokenDAO = AuthTokenDAO.getInstance();
        gameService = new GameService(gameDAO, authTokenDAO);
        gameDAO.clearGames();
    }

    @Test
    public void testCreateGameSuccess() throws DataAccessException {

        UserData user = new UserData("testUser", "password", "email@example.com");
        String authToken = "validAuthToken";
        authTokenDAO.createAuth(new AuthData(authToken, user.username()));


        GameData gameData = gameService.createGame("Test Game", "WHITE", user.username());


        assertNotNull(gameData, "GameData should not be null");
        assertEquals("Test Game", gameData.getGameName(), "Game name should match");
    }

    @Test
    public void testJoinGameSuccess() throws DataAccessException {

        GameData gameData = gameService.createGame("Test Game", "WHITE", "testUser");


        String authToken = "validAuthToken";
        authTokenDAO.createAuth(new AuthData(authToken, "testUser"));


        GameData joinedGame = gameService.joinGame(authToken, gameData.getGameID(), "BLACK");
        assertEquals("testUser", joinedGame.getBlackUsername(), "Black username should match the user who joined");
    }

    @Test
    public void testJoinGameWithTakenSlot() throws DataAccessException {

        GameData gameData = gameService.createGame("Test Game", "WHITE", "testUser");


        gameService.joinGame("validAuthToken", gameData.getGameID(), "WHITE");


        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("validAuthToken", gameData.getGameID(), "WHITE");
        }, "Expected DataAccessException when trying to join an already taken slot");
    }

    @Test
    public void testJoinGameWithInvalidAuthToken() {
        GameData gameData = gameService.createGame("Test Game", "WHITE", "testUser");
        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("invalidAuthToken", gameData.getGameID(), "BLACK");
        }, "Expected DataAccessException for invalid auth token");
    }

    @Test
    public void testListGamesNoGames() {
        List<GameData> games = gameService.listGames();
        assertTrue(games.isEmpty(), "List should be empty when no games have been created");
    }

    @Test
    public void testListGamesWithGames() {
        gameService.createGame("Test Game 1", "WHITE", "testUser1");
        gameService.createGame("Test Game 2", "BLACK", "testUser2");

        List<GameData> games = gameService.listGames();
        assertEquals(2, games.size(), "List should contain two games");
    }
}

