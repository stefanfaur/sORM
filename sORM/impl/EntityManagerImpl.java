package sORM.impl;

import sORM.impl.annotations.Column;
import sORM.impl.annotations.Entity;

import java.lang.reflect.Field;
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

        StringBuilder values = new StringBuilder("VALUES (");
        List<Field> fields = getAllFields(entity.getClass());
        boolean first = true;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                if (!first) {
                    sql.append(", ");
                    values.append(", ");
                }
                first = false;
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name().isEmpty() ? field.getName() : column.name();

                sql.append(columnName);
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(entity);
                    String valueString = fieldValue == null ? "NULL" : "'" + fieldValue.toString().replace("'", "''") + "'";
                    values.append(valueString);
                } catch (IllegalAccessException e) {
                    System.out.println("Illegal access to field: " + field.getName());
                    e.printStackTrace();
                }
            }
        }
        sql.append(") ").append(values).append(");");

        try {
            System.out.println("Executing save: " + sql.toString());
            adapter.executeSQL(sql.toString());
        } catch (SQLException e) {
            System.out.println("SQL Exception on save");
            e.printStackTrace();
        }
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
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            return null;
        }

        Entity tableAnnotation = entityClass.getAnnotation(Entity.class);
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(tableAnnotation.tableName());
        sql.append(" WHERE ");

        Field[] fields = entityClass.getDeclaredFields();
        boolean primaryKeyFound = false;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.primaryKey()) {
                    String columnName = column.name().isEmpty() ? field.getName() : column.name();
                    sql.append(columnName).append(" = '").append(primaryKey.toString().replace("'", "''")).append("';");
                    primaryKeyFound = true;
                    break;
                }
            }
        }

        if (primaryKeyFound) {
            try {
                System.out.println("Executing find: " + sql.toString());
                return adapter.executeQuery(sql.toString(), entityClass);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Primary key not found for class: " + entityClass.getSimpleName());
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
                    if (column.primaryKey()) {
                        primaryKeyField = column.name().isEmpty() ? field.getName() : column.name();
                        primaryKeyValue = fieldValue;
                    } else {
                        String columnName = column.name().isEmpty() ? field.getName() : column.name();
                        String valueString = fieldValue == null ? "NULL" : "'" + fieldValue.toString().replace("'", "''") + "'";
                        updates.add(columnName + " = " + valueString);
                    }
                } catch (IllegalAccessException e) {
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


}
