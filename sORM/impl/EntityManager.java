package sORM.impl;

public interface EntityManager {
    <T> void save(T entity);
    <T> void delete(T entity);
    <T> T find(Class<T> entityClass, String fieldName, Object fieldValue);

    <T> void update(T entity);
}

