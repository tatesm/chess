import ui.chessdoing;

import java.util.Scanner;

public class ChessClient {
    private final Scanner scanner = new Scanner(System.in);
    private boolean loggedIn = false;
    private final chessdoing ui = new chessdoing();

    public void run() {
        while (true) {
            if (!loggedIn) {
                ui.displayPreloginMenu();
                handlePreloginInput();
            } else {
                handlePostloginInput();
            }
        }
    }

    private void handlePreloginInput() {
        System.out.print("Enter command: ");
        String command = scanner.nextLine().trim().toLowerCase();

        switch (command) {
            case "help":
                // Display help text
                ui.displayPreloginMenu();
                break;
            case "login":
                promptLogin();
                break;
            case "register":
                promptRegister();
                break;
            case "quit":
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid command. Type 'Help' for a list of commands.");
        }
    }

    private void handlePostloginInput() {
        System.out.print("Enter command: ");
        String command = scanner.nextLine().trim().toLowerCase();

        switch (command) {
            case "help":
                displayPostloginMenu();
                break;
            case "logout":
                logout();
                break;
            case "create game":
                createGame();
                break;
            case "list games":
                listGames();
                break;
            case "play game":
                playGame();
                break;
            case "observe game":
                observeGame();
                break;
            default:
                System.out.println("Invalid command. Type 'Help' for a list of commands.");
        }
    }

    private void promptLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        // Call server facade for login logic here
        // If successful:
        loggedIn = true;
    }

    private void promptRegister() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();

        // Call server facade for register logic here
        // If successful:
        loggedIn = true;
    }

    // Add methods for logout, createGame, listGames, playGame, and observeGame here

    private void displayPostloginMenu() {
        System.out.println("Available Commands:");
        System.out.println("- Help: Displays help text.");
        System.out.println("- Logout: Log out of your account.");
        System.out.println("- Create Game: Create a new chess game.");
        System.out.println("- List Games: List all available games.");
        System.out.println("- Play Game: Join a game to play.");
        System.out.println("- Observe Game: Join a game to observe.");
    }
}
