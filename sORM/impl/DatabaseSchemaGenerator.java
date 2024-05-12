package sORM.impl;

import sORM.impl.annotations.Column;
import sORM.impl.annotations.Entity;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseSchemaGenerator {


    private DatabaseAdapter adapter;

    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }


    private String defaultSqlType(Class<?> javaType) {
        if (javaType == int.class || javaType == Integer.class) {
            return "INTEGER";
        } else if (javaType == long.class || javaType == Long.class) {
            return "BIGINT";
        } else if (javaType == double.class || javaType == Double.class) {
            return "DOUBLE";
        } else if (javaType == boolean.class || javaType == Boolean.class) {
            return "BOOLEAN";
        } else if (javaType == String.class) {
            return "VARCHAR(255)";
        } else if (javaType.isAnnotationPresent(Entity.class)) {
            return null; // meaning we need to handle this as a foreign key relationship
        }
        throw new IllegalArgumentException("Unsupported Java type: " + javaType.getSimpleName());
    }


    public List<String> generateSchema(Class<?>... classes) {
        List<String> schemaCommands = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Entity.class)) {
                Entity entity = clazz.getAnnotation(Entity.class);
                StringBuilder createTable = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
                createTable.append(entity.tableName()).append(" (");

                List<String> columns = new ArrayList<>();
                List<Field> fields = getAllFields(clazz);
                List<String> foreignKeys = new ArrayList<>();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Column column = field.getAnnotation(Column.class);
                    if (column != null) {
                        String columnName = column.name().isEmpty() ? field.getName() : column.name();
                        String columnType = defaultSqlType(field.getType());
                        if (columnType == null) { // This is an entity relationship
                            Class<?> fieldType = field.getType();
                            Entity referencedEntity = fieldType.getAnnotation(Entity.class);
                            columnType = "INTEGER"; // id field of the referenced entity
                            foreignKeys.add("FOREIGN KEY (" + columnName + ") REFERENCES " + referencedEntity.tableName() + " (id)");
                        }

                        String columnDefinition = columnName + " " + columnType;
                        if (column.primaryKey()) {
                            columnDefinition += " PRIMARY KEY";
                        }
                        columns.add(columnDefinition);
                    }
                }
                createTable.append(String.join(", ", columns));
                if (!foreignKeys.isEmpty()) {
                    createTable.append(", ");
                    createTable.append(String.join(", ", foreignKeys));
                }
                createTable.append(");");
                schemaCommands.add(createTable.toString());
            }
        }
        return schemaCommands;
    }


}

