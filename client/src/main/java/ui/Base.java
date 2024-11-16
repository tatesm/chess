package ui;

import java.util.Scanner;
import java.util.function.Supplier;
import java.util.function.Function;

public class Base {
    /**
     * Runs the main loop of the application, handling user commands and processing them.
     *
     * @param getUserCommand A supplier function to retrieve user commands (e.g., input from the console).
     * @param processCommand A function that processes user commands and determines the next action.
     * @return A string result indicating the outcome of the loop, or null if the loop continues indefinitely.
     */
    public static String run(Supplier<String> getUserCommand, Function<String, String> processCommand) {
        // Create a Scanner for reading input (useful if needed within the loop logic)
        Scanner scanner = new Scanner(System.in);

        while (true) { // Infinite loop to continuously prompt the user
            // Retrieve the next user command
            String command = getUserCommand.get();

            try {
                // Process the command and capture the result
                String result = processCommand.apply(command);
                if (result != null) {
                    return result; // Exit the loop if a result is returned (e.g., a transition command)
                }
            } catch (Exception e) {
                // Handle exceptions gracefully and notify the user
                System.err.println("An error occurred. Please try again.");
                System.out.println(e.getMessage()); // Provide additional error details for debugging
            }
        }
    }
}


