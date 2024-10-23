package server;

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

public class Server {

    private static final Gson gson = new Gson();
    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    public int run(int desiredPort) {
        // Set the port
        Spark.port(desiredPort);

        // Set static file location before any route mappings
        Spark.staticFiles.location("web");

        // Initialize DAOs and Services
        UserDAO userDAO = new UserDAO();
        GameDAO gameDAO = new GameDAO();
        AuthTokenDAO authTokenDAO = AuthTokenDAO.getInstance();
        userService = new UserService(userDAO, authTokenDAO);
        gameService = new GameService(gameDAO);
        clearService = new ClearService();

        // Register the POST /game endpoint to create a new game
        Spark.post("/game", (req, res) -> {
            // Parse the request body
            CreateGameRequest createGameRequest = gson.fromJson(req.body(), CreateGameRequest.class);
            String gameName = createGameRequest.getGameName();
            String authToken = req.headers("authorization");
            String playerColor = createGameRequest.getPlayerColor();  // Added playerColor

            // Create the game with 3 arguments
            GameData gameData = gameService.createGame(gameName, authToken, playerColor);
            res.status(200);  // OK status
            return gson.toJson(gameData);  // Return the created game's data
        });

        // Register the DELETE /db endpoint to clear the database
        Spark.delete("/db", (req, res) -> {
            clearService.clearDatabase();  // Assuming clearService is initialized
            res.status(200);
            return "{}";  // Return an empty JSON object
        });

        // Register the POST /user endpoint to register a new user
        Spark.post("/user", (req, res) -> {
            // Parse the request body to get user data
            UserData userData = gson.fromJson(req.body(), UserData.class);
            try {
                var authData = userService.register(userData);  // Register the user
                res.status(200);
                return gson.toJson(authData);  // Return auth token and username
            } catch (DataAccessException e) {
                res.status(403);  // If username already exists, respond with Forbidden
                return gson.toJson(new ErrorResponse("Error: username already taken"));
            } catch (Exception e) {
                res.status(400);  // Handle any other bad requests
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }
        });

        // Register the POST /session endpoint to log in a user
        Spark.post("/session", (req, res) -> {
            // Parse the request body to get user data
            UserData loginRequest = gson.fromJson(req.body(), UserData.class);
            try {
                var authData = userService.login(loginRequest);  // Log in the user
                res.status(200);
                return gson.toJson(authData);  // Return auth token and username
            } catch (DataAccessException e) {
                res.status(401);  // Unauthorized access if credentials are wrong
                return gson.toJson(new ErrorResponse("Error: invalid credentials"));
            } catch (Exception e) {
                res.status(400);  // Handle any other bad requests
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }
        });

        Spark.init();
        Spark.awaitInitialization();  // Wait for server to initialize
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    // Helper class to send error responses
    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    class CreateGameRequest {
        private String gameName;
        private String playerColor;
        private String username;

        public String getGameName() {
            return gameName;
        }

        public String getPlayerColor() {
            return playerColor;
        }

        public String getUsername() {
            return username;
        }
    }
}
