package sORM.impl;

public class DatabaseAdapterFactory {
    public static DatabaseAdapter getDatabaseAdapter(String dbType) {
        switch (dbType.toLowerCase()) {
            case "sqlite":
                return new SQLiteAdapter();
            // other dbs are coming ;)
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
}

