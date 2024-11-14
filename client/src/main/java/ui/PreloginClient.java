package ui;

import client.ServerFacade;
import exception.ResponseException;

import java.util.Scanner;

public class PreloginClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;

    public PreloginClient(ServerFacade serverFacade, Scanner scanner) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
    }

    public void run() {
        while (true) {
            System.out.print("Enter command (register, login, help, quit): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "register" -> register();
                    case "login" -> {
                        if (login()) return;  // Returns true if login is successful
                    }
                    case "help" -> displayHelp();
                    case "quit" -> {
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
                }
            } catch (ResponseException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void register() throws ResponseException {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        String email = scanner.nextLine();
        serverFacade.register(username, password, email);
        System.out.println("Successfully registered. You can now log in.");
    }

    private boolean login() throws ResponseException {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        serverFacade.login(username, password);
        System.out.println("Successfully logged in.");
        return true; // Indicates successful login and a switch to PostLoginClient
    }

    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("- register: Register a new account");
        System.out.println("- login: Log in to your account");
        System.out.println("- help: Display this help message");
        System.out.println("- quit: Exit the program");
    }
}
