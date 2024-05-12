package sORM.clients.UserProduct;

import sORM.impl.*;

import java.sql.SQLException;
import java.util.List;

public class Main {

    private static void printSplittingLine() {
        System.out.println("--------------------------------------------------");
    }

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

        printSplittingLine();

        // reconnect to the database
        adapter.connect(connectionString);

        // create an entity manager
        EntityManager entityManager = new EntityManagerImpl(adapter);

        // create a new user
        User newUser = new User("John Doe", "10/12/1990");
        newUser.setProduct(new Product("phone", "android", 1999.99));
        entityManager.save(newUser);
        newUser.setName("John Smith");
        entityManager.update(newUser); // update the user
        // find the user by name
        User foundUser = entityManager.find(User.class, "name", "John Smith");
        if (foundUser != null) {
            System.out.println("Found User: " + foundUser.getName());
        } else {
            System.out.println("User not found.");
        }
        printSplittingLine();


        // create a new product
        Product laptop = new Product("Laptop", "good looking", 999.99);
        entityManager.save(laptop);
        laptop.setPrice(899.99);
        entityManager.update(laptop);
        // find the product by name
        Product temp = entityManager.find(laptop.getClass(), "name", "Laptop");
        if (temp != null) {
            System.out.println("Found Product: " + temp.getName() + " Price: " + temp.getPrice() + " Description: " + temp.getDescription() + " ID: " + temp.getId());
        } else {
            System.out.println("Product not found.");
        }
        printSplittingLine();

        // create a new Poweruser, which is a subclass of User
        PowerUser powerUser = new PowerUser("Jane Doe", "10/12/1990", "admin");
        entityManager.save(powerUser);
        powerUser.setPowerLevel("superadmin");
        entityManager.update(powerUser);
        printSplittingLine();

        adapter.disconnect();

    }
}

