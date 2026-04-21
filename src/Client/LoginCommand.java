package Client;

import java.io.PrintWriter;
import java.sql.*;

public class LoginCommand {

    public static String handle(String[] parts, Connection db, PrintWriter out, String currentUser) {
        if (db == null) {
            out.println("ERROR Database not available.");
            return currentUser;
        }

        // LOGIN username password
        if (parts.length != 3) {
            out.println("ERROR Not enough arguments.");
            return currentUser;
        }

        String username = parts[1];
        String password = parts[2];

        try {
            String sql = "SELECT CUS_UNAME FROM CUSTOMER " +
                    "WHERE CUS_UNAME = ? AND CUS_PASSWD = ?";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        db.rollback();
                        out.println("ERROR Invalid username and/or password.");
                        return currentUser;
                    }
                }
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
