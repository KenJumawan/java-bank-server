package Client;

import java.io.PrintWriter;
import java.sql.*;

public class HistoryCommand {

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
            String sql =
                    "SELECT TXN_ID, CUS_ID_SOURCE, CUS_ID_DEST, TXN_AMOUNT, TXN_DATETIME " +
                            "FROM TRANSACTION_RECORD " +
                            "WHERE CUS_ID_SOURCE = ? OR CUS_ID_DEST = ? " +
                            "ORDER BY TXN_DATETIME DESC";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setString(1, currentUser);
                ps.setString(2, currentUser);

                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        int id = rs.getInt("TXN_ID");
                        String src = rs.getString("CUS_ID_SOURCE");
                        String dest = rs.getString("CUS_ID_DEST");
                        double amt = rs.getDouble("TXN_AMOUNT");
                        String ts = rs.getString("TXN_DATETIME");

                        String datePart = "1970-01-01";
                        String timePart = "00:00:00";

                        if (ts != null && ts.contains(" ")) {
                            String[] dt = ts.split(" ");
                            if (dt.length >= 2) {
                                datePart = dt[0];
                                timePart = dt[1];
                            }
                        }

                        String line;

                        if (src == null && currentUser.equals(dest)) {
                            // deposit
                            line = id + " DEPOSIT " + amt + " " + datePart + " " + timePart;
                        } else if (dest == null && currentUser.equals(src)) {
                            // withdraw
                            line = id + " WITHDRAW " + amt + " " + datePart + " " + timePart;
                        } else if (currentUser.equals(src) && dest != null) {
                            // transfer out
                            line = id + " TRANSFER_TO " + dest + " " + amt + " " + datePart + " " + timePart;
                        } else if (currentUser.equals(dest) && src != null) {
                            // transfer in
                            line = id + " TRANSFER_FROM " + src + " " + amt + " " + datePart + " " + timePart;
                        } else {
                            continue;
                        }

                        out.println(line);
                    }
                }
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
