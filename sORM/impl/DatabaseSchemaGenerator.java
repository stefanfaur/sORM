package sORM.impl;

import sORM.impl.annotations.Column;
import sORM.impl.annotations.Entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSchemaGenerator {

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
        }
        throw new IllegalArgumentException("Unsupported Java type: " + javaType.getSimpleName());
    }

    public List<String> generateSchema(Class<?>... classes) {
        List<String> schemaCommands = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Entity.class)) {
                Entity entity = clazz.getAnnotation(Entity.class);
                StringBuilder createTable = new StringBuilder("CREATE TABLE ");
                createTable.append(entity.tableName()).append(" (");

                List<String> columns = new ArrayList<>();
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    String columnName = field.getName();
                    String columnType = defaultSqlType(field.getType());
                    boolean primaryKey = false;

                    if (field.isAnnotationPresent(Column.class)) {
                        Column column = field.getAnnotation(Column.class);
                        columnName = column.name().isEmpty() ? columnName : column.name();
                        columnType = column.type().isEmpty() ? columnType : column.type();
                        primaryKey = column.primaryKey();
                    }

                    String columnDefinition = columnName + " " + columnType;
                    if (primaryKey) {
                        columnDefinition += " PRIMARY KEY";
                    }
                    columns.add(columnDefinition);
                }
                createTable.append(String.join(", ", columns));
                createTable.append(");");
                schemaCommands.add(createTable.toString());
            }
        }
        return schemaCommands;
    }
}

