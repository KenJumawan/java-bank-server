package Client;

import java.io.PrintWriter;
import java.sql.*;

public class DepositCommand {

    public static void handle(String[] parts, Connection db, PrintWriter out, String currentUser) {
        if (db == null) {
            out.println("ERROR Database not available.");
            return;
        }

        if (currentUser == null) {
            out.println("ERROR Not logged in.");
            return;
        }

        if (parts.length != 2) {
            out.println("ERROR Not enough arguments.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            out.println("ERROR Amount must be a number.");
            return;
        }

        if (amount <= 0) {
            out.println("ERROR Amount must be positive.");
            return;
        }

        try {
            // add to balance
            String updateSql =
                    "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE + ? WHERE CUS_UNAME = ?";
            try (PreparedStatement ps = db.prepareStatement(updateSql)) {
                ps.setDouble(1, amount);
                ps.setString(2, currentUser);
                ps.executeUpdate();
            }

            // record transaction
            String txnSql =
                    "INSERT INTO TRANSACTION_RECORD " +
                            "(CUS_ID_SOURCE, CUS_ID_DEST, TXN_AMOUNT) " +
                            "VALUES (NULL, ?, ?)";
            try (PreparedStatement ps = db.prepareStatement(txnSql)) {
                ps.setString(1, currentUser);
                ps.setDouble(2, amount);
                ps.executeUpdate();
            }

            db.commit();
            out.println("OK");

        } catch (SQLException e) {
            safeRollback(db);
            e.printStackTrace();
            out.println("ERROR Database error.");
        }
    }

    private static void safeRollback(Connection db) {
        try {
            if (db != null) db.rollback();
        } catch (SQLException ignored) {}
    }
}
