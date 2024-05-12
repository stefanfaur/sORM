package sORM.impl;

import java.sql.SQLException;

public interface DatabaseAdapter {
    void connect(String connectionString);
    void disconnect();
    void executeSQL(String sql) throws SQLException;
}
