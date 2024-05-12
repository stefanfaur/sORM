package sORM.impl;

import sORM.impl.annotations.Entity;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
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

    @Override
    public int executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            int count = stmt.executeUpdate(sql);
            System.out.println("Executed update: " + sql + " | Rows affected: " + count);
            return count;
        }
    }

    public <T> T executeQuery(String sql, Class<T> entityClass) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("Executed query: " + sql);

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            T entity = entityClass.getDeclaredConstructor().newInstance();

            if (rs.next()) {
                for (Field field : getAllFields(entityClass)) {
                    field.setAccessible(true);  // Make field accessible regardless of its visibility
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rsmd.getColumnName(i).toLowerCase();
                        if (field.getName().equalsIgnoreCase(columnName)) {
                            Object value = rs.getObject(i);
                            setField(field, entity, value);
                            break;
                        }
                    }
                }
            }
            return entity;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Reflection operation failed", e);
        } catch (SQLException e) {
            throw new SQLException("Failed to execute query", e);
        }
    }

    // field value parsing and setting
    private void setField(Field field, Object entity, Object value) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType.isAnnotationPresent(Entity.class) && value != null) {
            field.set(entity, findReferencedEntity(fieldType, Integer.parseInt(value.toString())));
        } else if (value != null) {
            if (fieldType == int.class || fieldType == Integer.class) {
                field.set(entity, Integer.parseInt(value.toString()));
            } else if (fieldType == double.class || fieldType == Double.class) {
                field.set(entity, Double.parseDouble(value.toString()));
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                field.set(entity, Boolean.parseBoolean(value.toString()));
            } else if (fieldType == String.class) {
                field.set(entity, value.toString());
            } else {
                field.set(entity, value);
            }
        }
    }

    // Find and return the entity with the specified ID
    private <T> T findReferencedEntity(Class<T> entityType, int id) {
        Entity entityInfo = entityType.getAnnotation(Entity.class);
        if (entityInfo == null) {
            throw new IllegalArgumentException("Class " + entityType.getSimpleName() + " is not an entity class.");
        }
        String tableName = entityInfo.tableName();
        String sql = "SELECT * FROM " + tableName + " WHERE id = " + id;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                T entity = entityType.getDeclaredConstructor().newInstance();
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rsmd.getColumnName(i);
                    Field field = findFieldIgnoreCase(entityType, columnName);
                    if (field != null) {
                        field.setAccessible(true);
                        Object value = rs.getObject(i);
                        setField(field, entity, value);
                    }
                }
                return entity;
            }
        } catch (SQLException | ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    // find field by name ignoring case, normalizes field names
    private Field findFieldIgnoreCase(Class<?> clazz, String columnName) {
        for (Field field : getAllFields(clazz)) {
            if (field.getName().equalsIgnoreCase(columnName)) {
                return field;
            }
        }
        return null;
    }

    // Get all fields of a class, including inherited fields
    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null) {
            fields.addAll(List.of(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
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
