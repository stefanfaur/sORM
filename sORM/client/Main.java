package sORM.client;

import sORM.impl.*;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // setup the connection
        String connectionString = "jdbc:sqlite:sorm.db"; // local db file name
        DatabaseAdapter adapter = new SQLiteAdapter();

        // get actual connection
        adapter.connect(connectionString);

        // create the tables if they're not already there
        try {
            // generate SQL schema
            DatabaseSchemaGenerator generator = new DatabaseSchemaGenerator();
            List<String> schemaCommands = generator.generateSchema(User.class, Product.class, PowerUser.class);

            // execute the generated SQL
            adapter.executeBatch(schemaCommands);
        } catch (SQLException e) {
            System.out.println("Error during SQL execution: " + e.getMessage());
        } finally {
            // disconnect from the database
            adapter.disconnect();
        }


        // test CRUD operations
        adapter.connect(connectionString);

        EntityManager entityManager = new EntityManagerImpl(adapter);
        // Create a new user
        User newUser = new User(1, "John Doe", "10/12/1990");
        entityManager.save(newUser);  // Save the new user

        // Update the user
        newUser.setName("John Smith");
        entityManager.update(newUser);  // Update the user

        // Find the user
        User foundUser = entityManager.find(User.class, 1);
        if (foundUser != null) {
            System.out.println("Found User: " + foundUser.getName());
        } else {
            System.out.println("User not found.");
        }

        Product laptop = new Product(1, "Laptop", "good looking", 999.99);
        entityManager.save(laptop);  // Save the new product
        laptop.setPrice(899.99);
        entityManager.update(laptop);  // Update the product
        Product temp = entityManager.find(laptop.getClass(), laptop.getId());
        if (temp != null) {
            System.out.println("Found Product: " + temp.getName() + " Price: " + temp.getPrice() + " Description: " + temp.getDescription() + " ID: " + temp.getId());
        } else {
            System.out.println("Product not found.");
        }


        // Delete the user
        entityManager.delete(newUser);

        // Create a new poweruser, which inherits from user
        PowerUser powerUser = new PowerUser(2, "Jane Doe", "10/12/1990", "admin");
        entityManager.save(powerUser);  // Save the new power user
        powerUser.setPowerLevel("superadmin");
        entityManager.update(powerUser);  // Update the power user


        // Close the database connection
        adapter.disconnect();

    }
}

