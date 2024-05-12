package sORM.client;

import sORM.impl.DatabaseAdapter;
import sORM.impl.DatabaseSchemaGenerator;
import sORM.impl.SQLiteAdapter;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // setup the connection
        String connectionString = "jdbc:sqlite:sorm.db"; // local db file name
        DatabaseAdapter adapter = new SQLiteAdapter();

        // get actual connection
        adapter.connect(connectionString);

        try {
            // generate SQL schema
            DatabaseSchemaGenerator generator = new DatabaseSchemaGenerator();
            List<String> schemaCommands = generator.generateSchema(User.class, Product.class);

            // execute the generated SQL
            ((SQLiteAdapter) adapter).executeBatch(schemaCommands);
        } catch (SQLException e) {
            System.out.println("Error during SQL execution: " + e.getMessage());
        } finally {
            // disconnect from the database
            adapter.disconnect();
        }
    }
}

