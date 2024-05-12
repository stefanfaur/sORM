package sORM.impl;

import java.lang.reflect.Field;
import java.sql.*;
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



    // Executes SQL commands that do not return data
    @Override
    public void executeSQL(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    // Executes queries that modify data and returns the number of affected rows
    @Override
    public int executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            int count = stmt.executeUpdate(sql);
            System.out.println("Executed update: " + sql + " | Rows affected: " + count);
            return count;
        }
    }

    // Executes queries that return data and handles the result set
    public <T> T executeQuery(String sql, Class<T> entityClass) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Executed query: " + sql);

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            T entity = entityClass.getDeclaredConstructor().newInstance();

            if (rs.next()) {
                Class<?> currentClass = entityClass;
                while (currentClass != null) {
                    for (Field field : currentClass.getDeclaredFields()) {
                        field.setAccessible(true);  // Make field accessible regardless of its visibility
                        String fieldName = field.getName().toLowerCase();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = rsmd.getColumnName(i).toLowerCase();
                            if (fieldName.equals(columnName)) {
                                Object value = rs.getObject(i);
                                field.set(entity, value);
                                break;
                            }
                        }
                    }
                    currentClass = currentClass.getSuperclass();  // Move to the superclass
                }
            }
            return entity;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Reflection operation failed", e);
        } catch (SQLException e) {
            throw new SQLException("Failed to execute query", e);
        }
    }


    // Executes a batch of SQL commands that do not return data
    public void executeBatch(List<String> sqlCommands) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            for (String sql : sqlCommands) {
                stmt.execute(sql);
                System.out.println("Executed: " + sql);
            }
        }
    }
}
