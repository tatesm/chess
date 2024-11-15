package ui;

import client.ServerFacade;
import model.AuthData;

import java.util.Scanner;

/**
 * PreLoginClient provides the command-line interface for user registration and login.
 */
public class PreLoginClient {
    private final ServerFacade serverFacade;
    private final Scanner scanner;

    /**
     * Constructs a PreLoginClient instance with a server connection and scanner for user input.
     *
     * @param serverFacade The server interface to handle login and registration.
     * @param scanner      The scanner for reading user input.
     */
    public PreLoginClient(ServerFacade serverFacade, Scanner scanner) {
        this.serverFacade = serverFacade;
        this.scanner = scanner;
    }

    /**
     * Starts the main command loop for pre-login functionality.
     */
    public void run() {
        while (true) {
            System.out.print("Enter command (register, login, help, quit): ");
            String command = scanner.nextLine().trim().toLowerCase();

            try {
                switch (command) {
                    case "register" -> register();
                    case "login" -> {
                        if (login()) return; // Exit to proceed to post-login
                    }
                    case "help" -> displayHelp();
                    case "quit" -> exitProgram();
                    default -> System.out.println("Invalid command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Prompts the user for registration details and attempts to register a new account.
     */
    private void register() throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        AuthData authData = serverFacade.register(username, password, email);
        System.out.println("Registration successful! You can now log in with username: " + username);
    }

    /**
     * Prompts the user for login credentials and attempts to authenticate.
     *
     * @return true if login is successful, triggering exit to PostLoginClient
     */
    private boolean login() throws Exception {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        AuthData authData = serverFacade.login(username, password);
        System.out.println("Welcome, " + username + "! You are now logged in.");
        return true; // Exit loop to proceed to PostLoginClient
    }

    /**
     * Displays available commands in the pre-login mode.
     */
    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("- register: Register a new account");
        System.out.println("- login: Log in to your account");
        System.out.println("- help: Display this help message");
        System.out.println("- quit: Exit the program");
    }

    /**
     * Exits the program.
     */
    private void exitProgram() {
        System.out.println("Goodbye!");
        System.exit(0);
    }