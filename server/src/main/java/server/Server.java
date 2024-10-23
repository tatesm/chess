package server;

import service.ClearService;
import spark.Spark;

public class Server {

    private ClearService clearService = new ClearService();  // Create an instance of ClearService

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register the DELETE /db endpoint to clear the database
        Spark.delete("/db", (req, res) -> {
            clearService.clearDatabase();
            res.status(200);  // Set HTTP status 200 OK
            return "{}";  // Return an empty JSON object in response
        });

        //This line initializes the server and can be removed once you have a functioning endpoint
        Spark.init();

        Spark.awaitInitialization();  // Wait for server to initialize
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
