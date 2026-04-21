package Client;

import java.sql.*;

public class BankSelectAll {

    public static void main(String[] args) {
        System.out.println("Connecting to banklab database...");

        try (Connection conn = DriverManager.getConnection(
                Credentials.DB_URL,
                Credentials.DB_USER,
                Credentials.DB_PASSWORD)) {

            System.out.println("Connected.");
            System.out.println();

            try (Statement stmt = conn.createStatement()) {

                // Dump CUSTOMER table
                System.out.println("CUSTOMER:");
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM CUSTOMER;")) {
                    ResultSetPrinter.printResultSet(rs);
                }
                System.out.println();

                // Dump TRANSACTION_RECORD table
                System.out.println("TRANSACTION_RECORD:");
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM TRANSACTION_RECORD;")) {
                    ResultSetPrinter.printResultSet(rs);
                }
                System.out.println();
            }

        } catch (SQLException e) {
            System.out.println("Error talking to database.");
            e.printStackTrace();
        }
    }
}
