package dungeoncrawler.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DatabaseManager implements AutoCloseable {
    private static final String DB_URL = "jdbc:sqlite:dungeon.db";
    private static Connection sharedConn;
    protected Connection conn;

    public DatabaseManager() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found.", e);
        }
        if (sharedConn == null || sharedConn.isClosed()) {
            sharedConn = DriverManager.getConnection(DB_URL);
            sharedConn.setAutoCommit(false);
        }
        conn = sharedConn;
        initSchema();
    }

    protected abstract void initSchema() throws SQLException;

    public void close() throws SQLException {
        if (sharedConn != null && !sharedConn.isClosed()) {
            sharedConn.close();
            sharedConn = null;
        }
    }
}
