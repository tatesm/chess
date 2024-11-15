package ui;

import java.util.Scanner;
import java.util.function.Supplier;
import java.util.function.Function;

public class Base {
    public static String run(Supplier<String> getUserCommand, Function<String, String> processCommand) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String command = getUserCommand.get();

            try {
                String result = processCommand.apply(command);
                if (result != null) {
                    return result; // Exit loop if a result is returned
                }
            } catch (Exception e) {
                System.err.println("An error occurred. Please try again.");
                System.out.println(e.getMessage());
            }
        }
    }
}

