package passoff.server;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameDAOTests {

    private GameDAO gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new GameDAO();
        gameDAO.clearGames(); // Clear any existing data before each test
    }

    @Test
    public void testCreateGame_Success() throws DataAccessException {
        GameData game = gameDAO.createGame("Test Game", "TestUser", "WHITE");
        assertNotNull(game);
        assertEquals("Test Game", game.getGameName());
        assertNotNull(game.getGameID());
    }

    @Test
    public void testCreateGame_NullGameName() {
        assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(null, "TestUser", "WHITE");
        });
    }

    @Test
    public void testGetGame_Success() throws DataAccessException {
        GameData createdGame = gameDAO.createGame("Test Game", "TestUser", "WHITE");
        GameData retrievedGame = gameDAO.getGame(createdGame.getGameID());
        assertNotNull(retrievedGame);
        assertEquals(createdGame.getGameID(), retrievedGame.getGameID());
        assertEquals("Test Game", retrievedGame.getGameName());
    }

    @Test
    public void testGetGame_InvalidID() throws DataAccessException {
        GameData game = gameDAO.getGame(-1); // Assuming -1 is an invalid ID
        assertNull(game);
    }

    @Test
    public void testClearGames() throws DataAccessException {
        gameDAO.createGame("Game 1", "User1", "WHITE");
        gameDAO.createGame("Game 2", "User2", "BLACK");

        gameDAO.clearGames();
        List<GameData> games = gameDAO.listGames();

        assertTrue(games.isEmpty());
    }

    @Test
    public void testUpdateGame_Success() throws DataAccessException {
        GameData game = gameDAO.createGame("Test Game", "User1", "WHITE");
        game.setWhiteUsername("User1");
        game.setBlackUsername("User2");

        gameDAO.updateGame(game);

        GameData updatedGame = gameDAO.getGame(game.getGameID());
        assertEquals("User1", updatedGame.getWhiteUsername());
        assertEquals("User2", updatedGame.getBlackUsername());
    }

    @Test
    public void testUpdateGame_InvalidID() {
        GameData game = new GameData(-1, "Invalid Game", new ChessGame());
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(game));
    }

    @Test
    public void testListGames_Success() throws DataAccessException {
        GameData game1 = gameDAO.createGame("Game 1", "User1", "WHITE");
        GameData game2 = gameDAO.createGame("Game 2", "User2", "BLACK");

        List<GameData> games = gameDAO.listGames();

        assertEquals(2, games.size());
        assertTrue(games.stream().anyMatch(g -> g.getGameID() == game1.getGameID()));
        assertTrue(games.stream().anyMatch(g -> g.getGameID() == game2.getGameID()));
    }

    @Test
    public void testListGames_NoGames() throws DataAccessException {
        gameDAO.clearGames();
        List<GameData> games = gameDAO.listGames();
        assertTrue(games.isEmpty());
    }
}

