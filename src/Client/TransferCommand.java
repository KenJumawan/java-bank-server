package Client;

import java.io.PrintWriter;
import java.sql.*;

public class TransferCommand {

    public static void handle(String[] parts, Connection db, PrintWriter out, String currentUser) {
        if (db == null) {
            out.println("ERROR Database not available.");
            return;
        }

        if (currentUser == null) {
            out.println("ERROR Not logged in.");
            return;
        }

        // TRANSFER destUser amount
        if (parts.length != 3) {
            out.println("ERROR Not enough arguments.");
            return;
        }

        String destUser = parts[1];
        String amountStr = parts[2];

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            out.println("ERROR Amount must be a number.");
            return;
        }

        if (amount <= 0) {
            out.println("ERROR Amount must be positive.");
            return;
        }

        if (currentUser.equals(destUser)) {
            out.println("ERROR Cannot transfer to yourself.");
            return;
        }

        try {
            // check dest exists
            String checkDestSql = "SELECT CUS_UNAME FROM CUSTOMER WHERE CUS_UNAME = ?";
            try (PreparedStatement ps = db.prepareStatement(checkDestSql)) {
                ps.setString(1, destUser);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        db.rollback();
                        out.println("ERROR Destination account does not exist.");
                        return;
                    }
                }
            }

            // get source balance
            double srcBal;
            String checkSrcSql = "SELECT CUS_BALANCE FROM CUSTOMER WHERE CUS_UNAME = ?";
            try (PreparedStatement ps = db.prepareStatement(checkSrcSql)) {
                ps.setString(1, currentUser);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        db.rollback();
                        out.println("ERROR Source account not found.");
                        return;
                    }
                    srcBal = rs.getDouble("CUS_BALANCE");
                }
            }

            if (srcBal < amount) {
                out.println("ERROR Insufficient funds.");
                return;
            }

            // subtract from source
            String subSql =
                    "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE - ? WHERE CUS_UNAME = ?";
            try (PreparedStatement ps = db.prepareStatement(subSql)) {
                ps.setDouble(1, amount);
                ps.setString(2, currentUser);
                ps.executeUpdate();
            }

            // add to dest
            String addSql =
                    "UPDATE CUSTOMER SET CUS_BALANCE = CUS_BALANCE + ? WHERE CUS_UNAME = ?";
            try (PreparedStatement ps = db.prepareStatement(addSql)) {
                ps.setDouble(1, amount);
                ps.setString(2, destUser);
                ps.executeUpdate();
            }

            // record transaction
            String txnSql =
                    "INSERT INTO TRANSACTION_RECORD " +
                            "(CUS_ID_SOURCE, CUS_ID_DEST, TXN_AMOUNT) " +
                            "VALUES (?, ?, ?)";
            try (PreparedStatement ps = db.prepareStatement(txnSql)) {
                ps.setString(1, currentUser);
                ps.setString(2, destUser);
                ps.setDouble(3, amount);
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
