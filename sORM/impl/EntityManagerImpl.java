package sORM.impl;

public class EntityManagerImpl implements EntityManager {
    private DatabaseAdapter adapter;

    public EntityManagerImpl(DatabaseAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public <T> void save(T entity) {
        // This method should convert the entity to SQL INSERT statements and execute them.
    }

    @Override
    public <T> void delete(T entity) {
        // This method should convert the entity to SQL DELETE statements and execute them.
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        // This method should execute SQL SELECT statements and map the result back to an entity.
        return null; // Placeholder
    }

    @Override
    public <T> void update(T entity) {
        // This method should convert the entity to SQL UPDATE statements and execute them.
    }
}
