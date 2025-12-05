import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/shadowtrail";
    private static final String USER = "pwstile";
    private static final String PASS = "pawas";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    public DatabaseManager() {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MariaDB/MySQL driver not found", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASS);
    }

    public void close() {
    }
}