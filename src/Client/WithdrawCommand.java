package Client;

import java.io.PrintWriter;
import java.sql.*;

public class WithdrawCommand {

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
            double currentBal;
            String checkSql = "SELECT CUS_BALANCE FROM CUSTOMER WHERE CUS_UNAME = ?";
            try (PreparedStatement ps = db.prepareStatement(checkSql)) {
                ps.setString(1, currentUser);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        db.rollback();
                        out.println("ERROR Account not found.");
                        return;
                    }
                    currentBal = rs.getDouble("CUS_BALANCE");
                }
            }

            if (currentBal < amount) {
                out.println("ERROR Insufficient funds.");
                return;
            }

            // subtract from balance
            String updateSql =
                    "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE - ? WHERE CUS_UNAME = ?";
            try (PreparedStatement ps = db.prepareStatement(updateSql)) {
                ps.setDouble(1, amount);
                ps.setString(2, currentUser);
                ps.executeUpdate();
            }

            // record transaction
            String txnSql =
                    "INSERT INTO TRANSACTION_RECORD " +
                            "(CUS_ID_SOURCE, CUS_ID_DEST, TXN_AMOUNT) " +
                            "VALUES (?, NULL, ?)";
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
