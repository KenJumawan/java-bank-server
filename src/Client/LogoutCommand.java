package Client;

import java.io.PrintWriter;

public class LogoutCommand {

    public static String handle(PrintWriter out) {
        out.println("OK");
        return null; // no user logged in now
    }
}
