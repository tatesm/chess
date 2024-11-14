package client;

import model.AuthData;
import model.GameData;
import model.UserData;
import dataaccess.*;
import service.UserService;

public class ServerFacade {

    private final GameDAO gameDAO;
    private final UserService userService; // Use UserService for user-related tasks
    private final AuthTokenDAO authTokenDAO; // Singleton instance of AuthTokenDAO

    public ServerFacade(String serverUrl) {
        // Assume DatabaseManager handles connection setup using serverUrl
        this.gameDAO = new GameDAO();
        this.userService = new UserService(new UserDAO(), AuthTokenDAO.getInstance()); // Inject singleton AuthTokenDAO into UserService
        this.authTokenDAO = AuthTokenDAO.getInstance(); // Use singleton instance
    }

    public AuthData register(String username, String password, String email) throws DataAccessException {
        UserData newUser = new UserData(username, password, email);
        return userService.register(newUser); // Delegate registration to UserService
    }

    public AuthData login(String username, String password) throws DataAccessException {
        UserData loginAttempt = new UserData(username, password, null);
        return userService.login(loginAttempt); // Delegate login to UserService
    }

    public void logout(String authToken) throws DataAccessException {
        // Deletes the token using the singleton AuthTokenDAO
        authTokenDAO.deleteAuth(authToken);
    }

    public GameData createGame(String gameName) throws DataAccessException {
        ChessGame newGame = new ChessGame(); // Assuming ChessGame has a default constructor
        GameData gameData = new GameData(0, gameName, newGame); // ID will be auto-generated in database
        gameDAO.insertGame(gameData);
        return gameDAO.getGameByName(gameName);
    }

    public GameData getGame(int gameId) throws DataAccessException {
        return gameDAO.getGameById(gameId);
    }

    public List<GameData> listGames() throws DataAccessException {
        return gameDAO.getAllGames();
    }

    public void joinGame(int gameId, String username, String color) throws DataAccessException {
        GameData game = gameDAO.getGameById(gameId);
        if (color.equalsIgnoreCase("white")) {
            game.setWhiteUsername(username);
        } else if (color.equalsIgnoreCase("black")) {
            game.setBlackUsername(username);
        } else {
            throw new DataAccessException("Invalid color choice. Choose 'white' or 'black'.");
        }
        gameDAO.updateGame(game);
    }

    public void observeGame(int gameId, String username) throws DataAccessException {
        System.out.println(username + " is now observing game " + gameId);
        // Add observer functionality here if supported by GameData
    }

    public String help() {
        return """
                Available commands:
                - register <username> <password> <email>
                - login <username> <password>
                - logout
                - creategame <game name>
                - listgames
                - playgame <game ID> <white|black>
                - observegame <game ID>
                """;
    }
}


