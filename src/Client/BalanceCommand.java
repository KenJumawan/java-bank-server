package Client;

import java.io.PrintWriter;
import java.sql.*;

public class BalanceCommand {

    public static void handle(Connection db, PrintWriter out, String currentUser) {
        if (db == null) {
            out.println("ERROR Database not available.");
            return;
        }

        if (currentUser == null) {
            out.println("ERROR Not logged in.");
            return;
        }

        try {
            String sql = "SELECT CUS_BALANCE FROM CUSTOMER WHERE CUS_UNAME = ?";
            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setString(1, currentUser);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        db.rollback();
                        out.println("ERROR Account not found.");
                        return;
                    }

                    String balance = rs.getBigDecimal("CUS_BALANCE").toPlainString();
                    db.commit();
                    out.println("BALANCE " + balance);
                }
            }
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
