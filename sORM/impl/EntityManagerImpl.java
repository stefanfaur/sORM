package sORM.impl;

import sORM.impl.annotations.Column;
import sORM.impl.annotations.Entity;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityManagerImpl implements EntityManager {
    private DatabaseAdapter adapter;

    public EntityManagerImpl(DatabaseAdapter adapter) {
        this.adapter = adapter;
    }

    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }


    @Override
    public <T> void save(T entity) {

        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            System.out.println("No Entity annotation present.");
            return;
        }

        Entity tableAnnotation = entity.getClass().getAnnotation(Entity.class);
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableAnnotation.tableName()).append(" (");

        List<Field> fields = getAllFields(entity.getClass());
        List<String> columnNames = new ArrayList<>();
        List<String> valueStrings = new ArrayList<>();

        // Process each field to construct SQL query
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                field.setAccessible(true);
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name().isEmpty() ? field.getName() : column.name();

                try {
                    Object fieldValue = field.get(entity);
                    if (field.getType().isAnnotationPresent(Entity.class) && fieldValue != null) {
                        // Recursive save if the field is another entity (for nested objects)
                        save(fieldValue); // Ensure nested object is saved first
                        Field idField = fieldValue.getClass().getDeclaredField("id"); // Assuming 'id' is the PK field
                        idField.setAccessible(true);
                        fieldValue = idField.get(fieldValue); // Use the nested entity's id as the FK
                    }

                    // Convert fieldValue to a string suitable for SQL statement
                    String valueString = convertValueForSQL(fieldValue);
                    if (valueString != null) {
                        columnNames.add(columnName);
                        valueStrings.add(valueString);
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    System.out.println("Error accessing field: " + field.getName());
                    e.printStackTrace();
                }
            }
        }

        if (!columnNames.isEmpty()) {
            sql.append(String.join(", ", columnNames)).append(") VALUES (");
            sql.append(String.join(", ", valueStrings)).append(");");

            // Execute the constructed SQL query
            try {
                System.out.println("Executing save: " + sql.toString());
                adapter.executeSQL(sql.toString());
            } catch (SQLException e) {
                System.out.println("SQL Exception on save");
                e.printStackTrace();
            }
        } else {
            System.out.println("No data to insert.");
        }
    }

    private String convertValueForSQL(Object value) {
        if (value == null) {
            return "NULL";
        } else if (value instanceof String) {
            return "'" + value.toString().replace("'", "''") + "'"; // Handle SQL injection
        } else if (value instanceof Number) {
            return value.toString(); // Numbers are inserted directly
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? "1" : "0"; // Convert Boolean to integer for SQL
        }
        return null; // Return null if the type is not handled
    }

    @Override
    public <T> void delete(T entity) {

        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            return;
        }

        Entity tableAnnotation = entity.getClass().getAnnotation(Entity.class);
        StringBuilder sql = new StringBuilder("DELETE FROM ");
        sql.append(tableAnnotation.tableName());
        sql.append(" WHERE ");

        Field[] fields = entity.getClass().getDeclaredFields();
        boolean hasPrimaryKey = false;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.primaryKey()) {
                    field.setAccessible(true);
                    try {
                        String columnName = column.name().isEmpty() ? field.getName() : column.name();
                        Object value = field.get(entity);
                        sql.append(columnName).append(" = '").append(value.toString().replace("'", "''")).append("';");
                        hasPrimaryKey = true;
                        break;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (hasPrimaryKey) {
            try {
                System.out.println("Executing delete: " + sql.toString());
                adapter.executeSQL(sql.toString());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No primary key defined. Delete operation aborted.");
        }
    }



    @Override
    public <T> T find(Class<T> entityClass, String fieldName, Object fieldValue) {

        if (!entityClass.isAnnotationPresent(Entity.class)) {
            System.out.println("Entity annotation missing for class: " + entityClass.getSimpleName());
            return null;
        }

        Entity tableAnnotation = entityClass.getAnnotation(Entity.class);
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableAnnotation.tableName());
        sql.append(" WHERE ");

        Field field;
        try {
            field = entityClass.getDeclaredField(fieldName);
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name().isEmpty() ? field.getName() : column.name();
                String valueString = fieldValue.toString().replace("'", "''");  // Handle potential SQL injection
                sql.append(columnName).append(" = '").append(valueString).append("'");
            } else {
                System.out.println("Field '" + fieldName + "' in class '" + entityClass.getSimpleName() + "' is not annotated as a column.");
                return null;
            }
        } catch (NoSuchFieldException e) {
            System.out.println("Field '" + fieldName + "' not found in class '" + entityClass.getSimpleName() + "'");
            return null;
        }

        try {
            System.out.println("Executing find: " + sql.toString());
            return adapter.executeQuery(sql.toString(), entityClass);
        } catch (SQLException e) {
            System.out.println("SQL Exception on find");
            e.printStackTrace();
            return null;
        }
    }




    @Override
    public <T> void update(T entity) {

        if (!entity.getClass().isAnnotationPresent(Entity.class)) {
            System.out.println("Entity annotation missing.");
            return;
        }

        Entity tableAnnotation = entity.getClass().getAnnotation(Entity.class);
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(tableAnnotation.tableName()).append(" SET ");

        List<Field> fields = getAllFields(entity.getClass());
        List<String> updates = new ArrayList<>();
        String primaryKeyField = "";
        Object primaryKeyValue = null;

        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(entity);
                    if (field.getType().isAnnotationPresent(Entity.class) && fieldValue != null) {
                        // Assuming the nested entity also needs to be updated
                        saveOrUpdate(fieldValue); // Save or update the nested entity
                        Field idField = fieldValue.getClass().getDeclaredField("id"); // Assuming 'id' is the primary key
                        idField.setAccessible(true);
                        fieldValue = idField.get(fieldValue); // Use the nested entity's id as the FK
                    }

                    if (column.primaryKey()) {
                        primaryKeyField = column.name().isEmpty() ? field.getName() : column.name();
                        primaryKeyValue = fieldValue;
                    } else if (fieldValue != null) {
                        String columnName = column.name().isEmpty() ? field.getName() : column.name();
                        String valueString = "'" + fieldValue.toString().replace("'", "''") + "'";
                        updates.add(columnName + " = " + valueString);
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    System.out.println("Illegal access to field: " + field.getName());
                    e.printStackTrace();
                }
            }
        }

        if (!updates.isEmpty() && !primaryKeyField.isEmpty() && primaryKeyValue != null) {
            sql.append(String.join(", ", updates));
            sql.append(" WHERE ").append(primaryKeyField).append(" = '").append(primaryKeyValue.toString().replace("'", "''")).append("';");

            try {
                System.out.println("Executing update: " + sql.toString());
                adapter.executeUpdate(sql.toString());
            } catch (SQLException e) {
                System.out.println("SQL Exception on update");
                e.printStackTrace();
            }
        } else {
            System.out.println("No updates to perform or primary key not set.");
        }
    }

    private <T> void saveOrUpdate(T entity) {
        try {
            if (entity.getClass().isAnnotationPresent(Entity.class)) {
                Field idField = findPrimaryKeyField(entity.getClass());
                if (idField != null) {
                    idField.setAccessible(true);
                    Object idValue = idField.get(entity);
                    // Assuming ID is of type Integer and a non-null and non-zero value indicates an existing entity
                    if (idValue != null && ((Integer) idValue) > 0) {
                        update(entity);
                    } else {
                        save(entity);
                    }
                } else {
                    System.out.println("No primary key field found for class: " + entity.getClass().getSimpleName());
                }
            } else {
                System.out.println("No Entity annotation present on class: " + entity.getClass().getSimpleName());
            }
        } catch (IllegalAccessException e) {
            System.out.println("Access error on primary key field: " + e.getMessage());
            e.printStackTrace();
        }
    }





    private Field findPrimaryKeyField(Class<?> clazz) {
        // Search for the primary key field annotated with @Column(primaryKey = true)
        List<Field> fields = getAllFields(clazz);
        for (Field field : fields) {
            Column column = field.getAnnotation(Column.class);
            if (column != null && column.primaryKey()) {
                return field;
            }
        }
        return null;
    }

}
