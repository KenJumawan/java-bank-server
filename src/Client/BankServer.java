package Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BankServer {

    public static void main(String[] args) {
        final int PORT = 5230;  // must match BankClient

        System.out.println("Starting BankServer on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("BankServer is running. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Create a new handler thread for this client
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread t = new Thread(handler);
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
