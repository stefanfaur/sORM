package sORM.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


public class SQLiteAdapter implements DatabaseAdapter {
    private Connection connection;

    @Override
    public void connect(String connectionString) {
        try {
            connection = DriverManager.getConnection(connectionString);
            System.out.println("Connected to the database successfully.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to SQLite database", e);
        }
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Disconnected from the database.");
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close SQLite database connection", e);
            }
        }
    }

    @Override
    public void executeSQL(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void executeBatch(List<String> sqlCommands) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            for (String sql : sqlCommands) {
                stmt.execute(sql);
                System.out.println("Executed: " + sql);
            }
        }
    }
}
