package sORM.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface DatabaseAdapter {
    void connect(String connectionString);
    void disconnect();

    // Executes a single SQL command that does not return data
    void executeSQL(String sql) throws SQLException;

    // Executes queries that modify data and returns the number of affected rows
    int executeUpdate(String sql) throws SQLException;

    // Executes queries that return data and handles the result set
    public <T> T executeQuery(String sql, Class<T> entityClass) throws SQLException;

    public void executeBatch(List<String> sqlCommands) throws SQLException;
}
