package ui;

import client.ServerFacade;
import model.AuthData;

import java.util.Scanner;

public class PreLoginClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;

    public PreLoginClient(ServerFacade serverFacade, Scanner scanner) {
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
                        if (login()) return; // Transition to PostLoginClient on success
                    }
                    case "help" -> displayHelp();
                    case "quit" -> {
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private void register() throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        AuthData authData = serverFacade.register(username, password, email);
        System.out.println("Registration successful. You can now log in as " + username);
    }

    private boolean login() throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        AuthData authData = serverFacade.login(username, password);
        System.out.println("Successfully logged in as " + username);
        return true; // Transition to PostLoginClient
    }

    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("- register: Register a new account");
        System.out.println("- login: Log in to your account");
        System.out.println("- help: Display this help message");
        System.out.println("- quit: Exit the program");
    }
}

