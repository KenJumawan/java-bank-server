package Client;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetPrinter {
    /**
     * All methods are static; there is no need to construct this class.
     */
    private ResultSetPrinter() {
    }

    public static void printResultSet(ResultSet set) throws SQLException {
        int cols = set.getMetaData().getColumnCount();
        for (int i = 1; i <= cols; i++) {
            System.out.printf("%-16s", set.getMetaData().getColumnName(i));
        }
        System.out.println();

        while (set.next()) {
            for (int i = 1; i <= cols; i++)
                System.out.printf("%-16s", set.getString(i));
            System.out.println();
        }
    }
}
