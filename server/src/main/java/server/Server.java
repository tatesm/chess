package server;

import created.CreatedStuff;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import server.websocket.WebSocketHandler;
import service.ClearService;
import service.GameService;
import service.UserService;
import spark.Spark;
import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.List;

public class Server {

    private static final Gson GSON = new Gson();
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthTokenDAO authTokenDAO;
    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        initializeDAOs();
        initializeServices();
        registerRoutes();

        // Register WebSocket route
        Spark.webSocket("/ws", WebSocketHandler.class);

        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }


    private void initializeDAOs() {
        try {
            DatabaseManager databaseManager = new DatabaseManager();
            databaseManager.createDatabase();
            databaseManager.configureDatabase();
            userDAO = new UserDAO();
            gameDAO = new GameDAO();
            authTokenDAO = AuthTokenDAO.getInstance();
        } catch (DataAccessException e) {
            throw new RuntimeException(String.format("Unable to configure database" + e.getMessage()));
        }

    }

    private void initializeServices() {
        userService = new UserService(userDAO, authTokenDAO);
        gameService = new GameService(gameDAO, authTokenDAO);
        clearService = new ClearService();
    }

    private void registerRoutes() {
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearDatabase);
        Spark.post("/user", this::registerUser);
        Spark.get("/game", this::listGames);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
    }

    private Object createGame(spark.Request req, spark.Response res) throws DataAccessException {
        CreatedStuff.CreateGameRequest createGameRequest = GSON.fromJson(req.body(), CreatedStuff.CreateGameRequest.class);
        String gameName = createGameRequest.getGameName();
        String authToken = req.headers("authorization");
        String playerColor = createGameRequest.getPlayerColor();

        if (isInvalidAuthToken(authToken, res)) {
            return GSON.toJson(new ErrorResponse("Error: Missing authorization token"));
        }

        AuthData authData = authTokenDAO.getAuth(authToken);
        if (authData == null) {
            res.status(401);
            return GSON.toJson(new ErrorResponse("Error: Invalid authorization token"));
        }

        GameData gameData = gameService.createGame(gameName, authData.username(), playerColor);
        res.status(200);
        return GSON.toJson(gameData);
    }

    private Object joinGame(spark.Request req, spark.Response res) {

        CreatedStuff.JoinGameRequest joinGameRequest = GSON.fromJson(req.body(), CreatedStuff.JoinGameRequest.class);
        String authToken = req.headers("authorization");
        int gameID = joinGameRequest.getGameID();
        String playerColor = joinGameRequest.getPlayerColor();

        if (isInvalidAuthToken(authToken, res)) {
            return GSON.toJson(new ErrorResponse("Error: Missing authorization token"));
        }
        if (isInvalidPlayerColor(playerColor, res)) {
            return GSON.toJson(new ErrorResponse("Error: Invalid player color"));
        }

        try {
            gameService.joinGame(authToken, gameID, playerColor);
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {
            handleJoinGameExceptions(e, res);
            return GSON.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    private Object clearDatabase(spark.Request req, spark.Response res) throws DataAccessException {
        clearService.clearDatabase();
        res.status(200);
        return "{}";
    }

    private Object registerUser(spark.Request req, spark.Response res) {
        UserData userData = GSON.fromJson(req.body(), UserData.class);

        if (isMissingUserData(userData, res)) {
            return GSON.toJson(new ErrorResponse("Error: Missing required fields"));
        }

        try {
            if (userService.getUser(userData.username()) != null) {
                res.status(403);
                return GSON.toJson(new ErrorResponse("Error: Username already taken"));
            }
            var authData = userService.register(userData);
            res.status(200);
            return GSON.toJson(authData);
        } catch (DataAccessException e) {
            res.status(500);
            return GSON.toJson(new ErrorResponse("Error: Server error"));
        } catch (Exception e) {
            res.status(400);
            return GSON.toJson(new ErrorResponse("Error: Invalid request"));
        }
    }

    private Object listGames(spark.Request req, spark.Response res) throws DataAccessException {
        String authToken = req.headers("authorization");

        AuthData authData = authTokenDAO.getAuth(authToken);
        if (authData == null) {
            res.status(401);
            return GSON.toJson(new ErrorResponse("Error: Invalid auth token"));
        }

        List<GameData> games = gameService.listGames();
        res.status(200);
        return GSON.toJson(new CreatedStuff.GamesResponse(games));
    }

    private Object loginUser(spark.Request req, spark.Response res) {
        UserData loginRequest = GSON.fromJson(req.body(), UserData.class);
        try {
            var authData = userService.login(loginRequest);
            res.status(200);
            return GSON.toJson(authData);
        } catch (DataAccessException e) {
            res.status(401);
            return GSON.toJson(new ErrorResponse("Error: invalid credentials"));
        } catch (Exception e) {
            res.status(400);
            return GSON.toJson(new ErrorResponse("Error: bad request"));
        }
    }

    private Object logoutUser(spark.Request req, spark.Response res) throws DataAccessException {
        String authToken = req.headers("authorization");

        if (authToken == null) {
            res.status(400);
            return GSON.toJson(new ErrorResponse("Error: Missing authorization token"));
        }

        AuthData authData = authTokenDAO.getAuth(authToken);
        if (authData == null) {
            res.status(401);
            return GSON.toJson(new ErrorResponse("Error: Invalid auth token"));
        }

        authTokenDAO.deleteAuth(authToken);
        res.status(200);
        return "{}";
    }

    private boolean isInvalidAuthToken(String authToken, spark.Response res) {
        if (authToken == null || authToken.isEmpty()) {
            res.status(401);
            return true;
        }
        return false;
    }

    private boolean isInvalidPlayerColor(String playerColor, spark.Response res) {
        if (playerColor == null || (!playerColor.equalsIgnoreCase("WHITE") && !playerColor.equalsIgnoreCase("BLACK"))) {
            res.status(400);
            return true;
        }
        return false;
    }

    private void handleJoinGameExceptions(DataAccessException e, spark.Response res) {
        if (e.getMessage().contains("Invalid auth token")) { // Error from gameService
            res.status(401);
            res.body(GSON.toJson(new ErrorResponse("Error: Invalid authorization token")));
        } else if (e.getMessage().contains("Game not found") || e.getMessage().contains("Invalid player color")) {
            res.status(400);
        } else if (e.getMessage().contains("already taken")) {
            res.status(403);
        } else {
            res.status(500);
        }
    }


    private boolean isMissingUserData(UserData userData, spark.Response res) {
        if (userData.username() == null || userData.password() == null || userData.email() == null) {
            res.status(400);
            return true;
        }
        return false;
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private record ErrorResponse(String message) {
    }
}


