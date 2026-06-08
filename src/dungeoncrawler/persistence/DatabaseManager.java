package dungeoncrawler.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Base class for SQLite-backed persistence managers.
 *
 * <p>The manager opens a shared SQLite connection to {@code dungeon.db} and
 * lets subclasses initialize their own tables through {@link #initSchema()}.</p>
 */
public abstract class DatabaseManager implements AutoCloseable {
    private static final String DB_URL = "jdbc:sqlite:dungeon.db";
    private static Connection sharedConn;
    /** SQLite connection used by concrete persistence managers. */
    protected Connection conn;

    /**
     * Opens the shared SQLite connection and initializes subclass schema.
     *
     * @throws SQLException if the driver or database cannot be opened
     */
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

    /**
     * Creates or migrates the tables required by a concrete manager.
     *
     * @throws SQLException if schema setup fails
     */
    protected abstract void initSchema() throws SQLException;

    /**
     * Closes the shared SQLite connection.
     *
     * @throws SQLException if the connection cannot be closed
     */
    @Override
    public void close() throws SQLException {
        if (sharedConn != null && !sharedConn.isClosed()) {
            sharedConn.close();
            sharedConn = null;
        }
    }
}
