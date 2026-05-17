package dungeoncrawler.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DatabaseManager implements AutoCloseable {
    private static final String DB_URL = "jdbc:sqlite:dungeon.db";
    protected Connection conn;

    public DatabaseManager() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found.", e);
        }
        conn = DriverManager.getConnection(DB_URL);
        conn.setAutoCommit(false);
        initSchema();
    }

    protected abstract void initSchema() throws SQLException;

    @Override
    public void close() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
