package Client;

public class Credentials {

    // BankLab database
    public static final String DB_URL =
            "jdbc:mysql://classdb.mads.commonwealthu.edu:3306/pdj1404banklab";

    public static final String DB_USER = "pdj1404";

    // Password
    public static final String DB_PASSWORD = "Ju!sStudying@69";

    private final String username;
    private final String password;

    public Credentials() {
        this.username = DB_USER;
        this.password = DB_PASSWORD;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
