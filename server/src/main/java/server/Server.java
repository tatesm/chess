package server;

import dataaccess.DataAccessException;
import model.UserData;
import service.UserService;
import dataaccess.UserDAO;
import spark.Spark;
import com.google.gson.Gson;

public class Server {

    private static final Gson gson = new Gson();
    private UserService userService;  // UserService instance

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");
        Spark.delete("/db", (req, res) -> {
            clearService.clearDatabase();  // Assuming clearService is initialized
            res.status(200);
            return "{}";  // Return an empty JSON object
        });


        // Initialize UserDAO and UserService
        UserDAO userDAO = new UserDAO();  // Ideally, this should be a singleton or passed from a service layer
        userService = new UserService(userDAO);  // Injecting UserDAO into UserService

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
}