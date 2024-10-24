package server;

import created.CreatedStuff;
import dataaccess.AuthTokenDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.ClearService;
import service.GameService;
import service.UserService;

import dataaccess.UserDAO;
import spark.Spark;
import com.google.gson.Gson;

import java.util.List;

public class Server {

    private static final Gson GSON = new Gson();
    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        UserDAO userDAO = new UserDAO();
        GameDAO gameDAO = new GameDAO();
        AuthTokenDAO authTokenDAO = AuthTokenDAO.getInstance();
        userService = new UserService(userDAO, authTokenDAO);
        gameService = new GameService(gameDAO, authTokenDAO);
        clearService = new ClearService();


        Spark.post("/game", (req, res) -> {
            CreatedStuff.CreateGameRequest createGameRequest = GSON.fromJson(req.body(), CreatedStuff.CreateGameRequest.class);
            String gameName = createGameRequest.getGameName();
            String authToken = req.headers("authorization");
            CreatedStuff.JoinGameRequest joinGameRequest = GSON.fromJson(req.body(), CreatedStuff.JoinGameRequest.class);
            String playerColor = joinGameRequest.getPlayerColor();


            if (authToken == null || authToken.isEmpty()) {
                res.status(401);
                return GSON.toJson(new ErrorResponse("Error: Missing authorization token"));
            }


            AuthData authData = AuthTokenDAO.getInstance().getAuth(authToken);
            if (authData == null) {
                res.status(401);
                return GSON.toJson(new ErrorResponse("Error: Invalid authorization token"));
            }


            GameData gameData = gameService.createGame(gameName, playerColor, authData.username());
            res.status(200);
            return GSON.toJson(gameData);
        });


        Spark.put("/game", (req, res) -> {
            CreatedStuff.JoinGameRequest joinGameRequest = GSON.fromJson(req.body(), CreatedStuff.JoinGameRequest.class);
            String authToken = req.headers("authorization");
            int gameID = joinGameRequest.getGameID();
            String playerColor = joinGameRequest.getPlayerColor();


            if (authToken == null || authToken.isEmpty()) {
                res.status(401);
                return GSON.toJson(new ErrorResponse("Error: Missing authorization token"));
            }


            if (playerColor == null || (!playerColor.equalsIgnoreCase("WHITE") && !playerColor.equalsIgnoreCase("BLACK"))) {
                res.status(400);
                return GSON.toJson(new ErrorResponse("Error: Invalid player color"));
            }

            try {
                gameService.joinGame(authToken, gameID, playerColor);
                res.status(200);
                return "{}";
            } catch (DataAccessException e) {
                if (e.getMessage().contains("Invalid auth token")) {
                    res.status(401); // Unauthorized
                } else if (e.getMessage().contains("Game not found") || e.getMessage().contains("Invalid player color")) {
                    res.status(400); // Bad request
                } else if (e.getMessage().contains("already taken")) {
                    res.status(403); // Forbidden
                } else {
                    res.status(500); // Internal server error
                }
                return GSON.toJson(new ErrorResponse("Error: " + e.getMessage()));
            }
        });


        Spark.delete("/db", (req, res) -> {
            clearService.clearDatabase();
            res.status(200);
            return "{}";
        });

        Spark.post("/user", (req, res) -> {

            UserData userData = GSON.fromJson(req.body(), UserData.class);


            if (userData.username() == null || userData.password() == null || userData.email() == null) {
                res.status(400);
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
        });
        Spark.get("/game", (req, res) -> {
            String authToken = req.headers("authorization");

            AuthData authData = AuthTokenDAO.getInstance().getAuth(authToken);
            if (authData == null) {
                res.status(401);
                return GSON.toJson(new ErrorResponse("Error: Invalid auth token"));
            }

            List<GameData> games = gameService.listGames();
            res.status(200);
            return GSON.toJson(new CreatedStuff.GamesResponse(games));
        });

        Spark.post("/session", (req, res) -> {
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
        });
        Spark.delete("/session", (req, res) -> {

            String authToken = req.headers("authorization");


            if (authToken == null) {
                res.status(400);
                return GSON.toJson(new ErrorResponse("Error: Missing authorization token"));
            }


            AuthData authData = AuthTokenDAO.getInstance().getAuth(authToken);
            if (authData == null) {
                res.status(401);
                return GSON.toJson(new ErrorResponse("Error: Invalid auth token"));
            }


            AuthTokenDAO.getInstance().deleteAuth(authToken);


            res.status(200);
            return "{}";
        });

        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private record ErrorResponse(String message) {
    }


}


