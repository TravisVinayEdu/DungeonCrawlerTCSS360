package dungeoncrawler.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:dungeon.db";
    protected Connection conn;

    public DatabaseManager() throws SQLException {
        conn = DriverManager.getConnection(DB_URL);
        conn.setAutoCommit(false);
        initSchema();
    }

    protected abstract void initSchema() throws SQLException;

    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
