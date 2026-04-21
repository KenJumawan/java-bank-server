package Client;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * A menu used by BankClient for logged-in users.
 */
public class LoggedInMenu {
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
    public LoggedInMenu(Scanner fromUser, Scanner fromServer, PrintWriter toServer) {
        this.fromUser = fromUser;
        this.fromServer = fromServer;
        this.toServer = toServer;
    }

    /**
     * Check the current balance in the bank account.
     */
    void checkBalance() {
        // Send request
        String request = "BALANCE";
        toServer.println(request);

        // Read response
        String response = fromServer.nextLine();
        StringTokenizer st = new StringTokenizer(response);

        if (!st.nextToken().equals("BALANCE")) {
            System.out.println("Received unexpected response: " + response);
            return;
        }

        double d = Double.parseDouble(st.nextToken());
        System.out.printf("Your balance is: $%.2f", d);
        System.out.println();
    }

    /**
     * Deposit funds into the bank account.
     */
    void deposit() {
        System.out.println("Enter the deposit amount.");
        System.out.print("> ");
        String line = fromUser.nextLine().trim();

        double d;
        try {
            d = Double.parseDouble(line);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a numeric value.");
            return;
        }

        if (d <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }

        String request = "DEPOSIT " + d;
        toServer.println(request);

        String response = fromServer.nextLine();
        StringTokenizer st = new StringTokenizer(response);
        if (!st.nextToken().equals("OK")) {
            System.out.println("Received unexpected response: " + response);
        } else {
            System.out.println("Deposit successful.");
        }
    }


    /**
     * Withdraw funds from the bank account.
     */
    void withdraw() {
        // Read input from user
        System.out.println("Enter the withdrawal amount.");
        System.out.print("> ");
        String line = fromUser.nextLine().trim();

        double d;
        try {
            d = Double.parseDouble(line);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a numeric value.");
            return;  // don't send anything to server
        }

        if (d <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }

        // Send request
        String request = "WITHDRAW " + d;
        toServer.println(request);

        // Read response
        String response = fromServer.nextLine();
        StringTokenizer st = new StringTokenizer(response);
        if (!st.nextToken().equals("OK")) {
            System.out.println("Received unexpected response: " + response);
        } else {
            System.out.println("Withdrawal successful.");
        }
    }


    /**
     * Transfer money to another user.
     */
    void transfer() {
        // Read recipient from user
        System.out.println("Enter the username you want to transfer to.");
        System.out.print("> ");
        String recipient = fromUser.nextLine().trim();

        if (recipient.isEmpty()) {
            System.out.println("Invalid username.");
            return;
        }

        // Read transfer amount
        System.out.println("Enter the transfer amount.");
        System.out.print("> ");
        String line = fromUser.nextLine().trim();

        double d;
        try {
            d = Double.parseDouble(line);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a numeric value.");
            return; // don't send anything to server
        }

        if (d <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }

        // Send request
        String request = "TRANSFER " + recipient + " " + d;
        toServer.println(request);

        // Read response
        String response = fromServer.nextLine();
        StringTokenizer st = new StringTokenizer(response);
        if (!st.nextToken().equals("OK")) {
            System.out.println("Received unexpected response: " + response);
        } else {
            System.out.println("Transfer successful.");
        }
    }


    /**
     * Print a record of all transactions involving this account.
     */
    void history() {
        // Send request
        String request = "HISTORY";
        toServer.println(request);

        // Loop until all transactions are processed.
        while (true) {
            // Read the next line from the server, and tokenize it.
            String response = fromServer.nextLine();

            // The server sends SUCCESS to indicate that there are no more transactions.
            if (response.equals("OK"))
                return;

            // Otherwise, tokenize the line to process it.
            StringTokenizer st = new StringTokenizer(response);

            // Process the transaction ID.
            int txn_id = Integer.parseInt(st.nextToken());
            String txn_type = st.nextToken();

            // Process the transaction type.
            System.out.print(txn_id + ": ");
            switch (txn_type) {
                case "DEPOSIT":
                    System.out.print("Deposited ");
                    break;

                case "WITHDRAW":
                    System.out.print("Withdrew ");
                    break;

                case "TRANSFER_FROM":
                    String sender = st.nextToken();
                    System.out.print("Received transfer from " + sender + " for ");
                    break;

                case "TRANSFER_TO":
                    String recipient = st.nextToken();
                    System.out.print("Made transfer to " + recipient + " for ");
                    break;

                default:
                    System.out.println("Received unexpected transaction type: " + txn_type);
                    break;
            }

            // Process the transaction amount.
            double txn_amount = Double.parseDouble(st.nextToken());
            System.out.printf("$%.2f", txn_amount);

            // Process the date and time.
            LocalDateTime dateTime = LocalDateTime.parse(st.nextToken() + "T" + st.nextToken());
            System.out.print(" on " + dateTime.toLocalDate() + " at " + dateTime.toLocalTime());
            System.out.println();
        }
    }

    void invalidRequest() {
        // Send request
        String request = "INVALID REQUEST";
        toServer.println(request);

        // Read response
        System.out.println("Server responded: " + fromServer.nextLine());
    }

    /**
     * Entry point for this menu.
     */
    void run() {
        while (true) {
            // Print the menu.
            System.out.println();
            System.out.println("Choose a command:");
            System.out.println("1. Check balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer");
            System.out.println("5. History");
            System.out.println("6. Sign out");
            System.out.print("> ");

            // Read user input, and act accordingly.
            String line = fromUser.nextLine().toLowerCase().trim();

            switch (line) {
                case "1":
                case "check":
                case "balance":
                case "check balance":
                case "checkbalance":
                case "b":
                case "c":
                    checkBalance();
                    break;

                case "2":
                case "deposit":
                case "d":
                    deposit();
                    break;

                case "3":
                case "withdraw":
                case "w":
                    withdraw();
                    break;

                case "4":
                case "transfer":
                case "t":
                    transfer();
                    break;

                case "5":
                case "history":
                case "h":
                    history();
                    break;

                case "6":
                case "sign out":
                case "signout":
                case "s":
                    return;

                case "7":
                    invalidRequest();
                    break;

                default:
                    System.out.println("Invalid command. Type a number, letter, or word.");
            }
        }
    }
}
