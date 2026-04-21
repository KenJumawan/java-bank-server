package Client;

import java.io.PrintWriter;
import java.sql.*;

public class RegisterCommand {

    public static String handle(String[] parts, Connection db, PrintWriter out, String currentUser) {
        if (db == null) {
            out.println("ERROR Database not available.");
            return currentUser;
        }

        // REGISTER username password
        if (parts.length != 3) {
            out.println("ERROR Not enough arguments.");
            return currentUser;
        }

        String username = parts[1];
        String password = parts[2];

        try {
            // check if username already exists
            String checkSql = "SELECT CUS_UNAME FROM CUSTOMER WHERE CUS_UNAME = ?";
            try (PreparedStatement ps = db.prepareStatement(checkSql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        db.rollback();
                        out.println("ERROR That username is already taken.");
                        return currentUser;
                    }
                }
            }

            // insert new customer with balance 0
            String insertSql =
                    "INSERT INTO CUSTOMER (CUS_UNAME, CUS_PASSWD, CUS_BALANCE) " +
                            "VALUES (?, ?, 0.00)";
            int updated;
            try (PreparedStatement ps = db.prepareStatement(insertSql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                updated = ps.executeUpdate();
            }

            if (updated != 1) {
                db.rollback();
                out.println("ERROR Could not create account.");
                return currentUser;
            }

            currentUser = username;
            db.commit();
            out.println("OK");
            return currentUser;

        } catch (SQLException e) {
            safeRollback(db);
            e.printStackTrace();
            out.println("ERROR Database error.");
            return currentUser;
        }
    }

    private static void safeRollback(Connection db) {
        try {
            if (db != null) db.rollback();
        } catch (SQLException ignored) {}
    }
}
