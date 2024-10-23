package service;

import dataaccess.AuthTokenDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GSTest {
    private GameService gameService;
    private GameDAO gameDAO;
    private AuthTokenDAO authTokenDAO;
    private AuthData authData;

    @BeforeEach
    public void setUp() {
        gameDAO = new GameDAO();
        authTokenDAO = AuthTokenDAO.getInstance();
        gameService = new GameService(gameDAO, authTokenDAO);

        // Mock AuthData for testing
        authData = new AuthData("testToken", "testUser");
        authTokenDAO.createAuth(authData); // Mock the creation of an auth token
    }

    @Test
    public void testCreateGameSuccess() {
        GameData gameData = gameService.createGame("Test Game", "WHITE", "testUser");
        assertNotNull(gameData);
        assertEquals("Test Game", gameData.getGameName());
        assertEquals("testUser", gameData.getWhiteUsername());
    }

    @Test
    public void testJoinGameSuccess() throws DataAccessException {
        GameData gameData = gameService.createGame("Test Game", "WHITE", "testUser");
        GameData joinedGame = gameService.joinGame("testToken", gameData.getGameID(), "BLACK");

        assertNotNull(joinedGame);
        assertEquals("testUser", joinedGame.getBlackUsername());
    }

    @Test
    public void testJoinGameInvalidToken() {
        GameData gameData = gameService.createGame("Test Game", "WHITE", "testUser");
        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("invalidToken", gameData.getGameID(), "BLACK");
        });
    }

    @Test
    public void testJoinGameFull() throws DataAccessException {
        GameData gameData = gameService.createGame("Test Game", "WHITE", "testUser");
        gameService.joinGame("testToken", gameData.getGameID(), "BLACK");

        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("testToken", gameData.getGameID(), "WHITE"); // This should fail
        });
    }

    @Test
    public void testListGames() {
        gameService.createGame("Test Game 1", "WHITE", "testUser");
        gameService.createGame("Test Game 2", "BLACK", "testUser");

        List<GameData> games = gameService.listGames();
        assertEquals(2, games.size());
    }
}

