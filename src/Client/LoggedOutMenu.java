package Client;

import java.io.PrintWriter;
import java.util.Scanner;

/**
 * A menu used by BankClient for logged-out users.
 */
public class LoggedOutMenu {
    private final Scanner fromUser;
    private final Scanner fromServer;
    private final PrintWriter toServer;

    /**
     * Explicit constructor
     *
     * @param fromUser   Stream to read from user.
     * @param fromServer Stream to read from server.
     * @param toServer   Stream to write to server.
     */
    public LoggedOutMenu(Scanner fromUser, Scanner fromServer, PrintWriter toServer) {
        this.fromUser = fromUser;
        this.fromServer = fromServer;
        this.toServer = toServer;
    }

    /**
     * Attempts to log in.
     *
     * @return True if login attempt successful
     */
    private boolean login() {
        // Read input from user
        System.out.println("Enter your username.");
        System.out.print("> ");
        String username = fromUser.nextLine().trim();

        System.out.println("Enter your password.");
        System.out.print("> ");
        String password = fromUser.nextLine().trim();

        // Send request
        String request = "LOGIN " + username + " " + password;
        toServer.println(request);

        // Read response
        String response = fromServer.nextLine();
        boolean success = response.equals("OK");
        if (success) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Login failed! Server sent response: " + response);
        }
        return success;
    }

    /**
     * Attempts to register a new account.
     *
     * @return True if registration attempt successful
     */
    private boolean register() {
        // Read input from user
        System.out.println("Choose a username.");
        System.out.print("> ");
        String username = fromUser.nextLine().trim();

        System.out.println("Choose a password.");
        System.out.print("> ");
        String password = fromUser.nextLine().trim();

        // Send request
        String request = "REGISTER " + username + " " + password;
        toServer.println(request);

        // Read response
        String response = fromServer.nextLine();
        boolean success = response.equals("OK");
        if (success) {
            System.out.println("Registration successful! Logged in as newly created user.");
        } else {
            System.out.println("Registration failed! Server sent response: " + response);
        }
        return success;
    }

    void invalidRequest() {
        // Send request
        String request = "INVALID REQUEST";
        toServer.println(request);

        // Read response
        System.out.println("Server responded: " + fromServer.nextLine());
    }

    /**
     * Entry point for this method.
     *
     * @return True if user logged in; false if user quit.
     */
    boolean run() {
        boolean loggedIn = false;

        while (!loggedIn) {
            // Print menu
            System.out.println();
            System.out.println("Choose a command:");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("> ");

            // Read user input, and act accordingly.
            String line = fromUser.nextLine().toLowerCase().trim();

            switch (line) {
                case "1":
                case "register":
                case "r":
                    loggedIn = register();
                    break;

                case "2":
                case "login":
                case "l":
                    loggedIn = login();
                    break;

                case "3":
                case "exit":
                case "e":
                case "x":
                    return false;

                case "7":
                    invalidRequest();
                    break;

                default:
                    System.out.println("Invalid command. Type a number, letter, or word.");
            }
        }

        return true;
    }
}
