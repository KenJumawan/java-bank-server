package Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * A simple, command-line based client program for interacting with a bank server.
 */
public class BankClient {
    public static final int PORT = 5230;

    private final Scanner fromUser;
    private final Scanner fromServer;
    private final PrintWriter toServer;

    /**
     * Create a new BankClient.
     *
     * @param url The URL of the bank server.
     * @throws IOException On any unrecoverable I/O or network error.
     */
    public BankClient(String url) throws IOException {
        try (Socket socket = new Socket(url, PORT)) {
            fromUser = new Scanner(System.in);
            fromServer = new Scanner(socket.getInputStream());
            toServer = new PrintWriter(socket.getOutputStream(), true);
            run();
        }
    }

    /**
     * Entry point for bank client program.
     *
     * @param args Allows the user to specify the bank server URL.
     * @throws IOException On any unrecoverable I/O or network error.
     */
    public static void main(String[] args) throws IOException {
        // Read URL from args[0]. Default to localhost if no argument provided.
        String url = "localhost";
        if (args.length > 0)
            url = args[0];

        new BankClient(url);
    }

    /**
     * This method contains the main loop of the client program.
     */
    private void run() {
        System.out.println("Welcome!");
        LoggedOutMenu loggedOutMenu = new LoggedOutMenu(fromUser, fromServer, toServer);
        LoggedInMenu loggedInMenu = new LoggedInMenu(fromUser, fromServer, toServer);

        while (true) {
            // Have the user log in or register an account.
            boolean loggedIn = loggedOutMenu.run();

            if (loggedIn) {
                // Allow the user to access the account they logged into or registered.
                loggedInMenu.run();
            } else {
                // Quit the client.
                return;
            }
        }
    }
}
