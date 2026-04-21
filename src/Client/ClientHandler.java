package Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private Scanner in;
    private PrintWriter out;
    Connection db;              // one DB connection per client
    String currentUser = null;  // logged-in user for this client

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;

        // set up client
        try {
            this.in = new Scanner(clientSocket.getInputStream());
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error setting up client I/O.");
            e.printStackTrace();
        }

        // set up DB
        try {
            db = DriverManager.getConnection(
                    Credentials.DB_URL,
                    Credentials.DB_USER,
                    Credentials.DB_PASSWORD
            );
            db.setAutoCommit(false);   // manual transactions
            System.out.println("DB connection ready for client.");
        } catch (SQLException e) {
            System.out.println("Error connecting to DB for client.");
            e.printStackTrace();
            db = null;
        }
    }

    @Override
    public void run() {
        System.out.println("Client handler thread started.");

        while (true) {
            if (!in.hasNextLine()) {
                break;  // client disconnected
            }

            String line = in.nextLine().trim();
            if (line.isEmpty()) continue;

            System.out.println("Client request: " + line);

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toUpperCase();

            try {
                switch (cmd) {
                    case "REGISTER" ->
                            currentUser = RegisterCommand.handle(parts, db, out, currentUser);

                    case "LOGIN" ->
                            currentUser = LoginCommand.handle(parts, db, out, currentUser);

                    case "BALANCE" ->
                            BalanceCommand.handle(db, out, currentUser);

                    case "DEPOSIT" ->
                            DepositCommand.handle(parts, db, out, currentUser);

                    case "WITHDRAW" ->
                            WithdrawCommand.handle(parts, db, out, currentUser);

                    case "TRANSFER" ->
                            TransferCommand.handle(parts, db, out, currentUser);

                    case "HISTORY" ->
                            HistoryCommand.handle(db, out, currentUser);

                    case "LOGOUT" ->
                            currentUser = LogoutCommand.handle(out);

                    default ->
                            out.println("ERROR Unknown command.");
                }
            } catch (Exception e) {
                out.println("ERROR Internal server error.");
                e.printStackTrace();
            }
        }

        // clean up
        try {
            if (db != null && !db.isClosed()) db.close();
            clientSocket.close();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Client disconnected.");
    }
}
