package sORM.clients.UserProduct;

import sORM.impl.annotations.Column;
import sORM.impl.annotations.Entity;

// each entity class must be annotated with @Entity
// !!! each Entity must be in a separate file, otherwise Reflection breaks
// !!! each Entity must have a no-arg constructor

@Entity(tableName = "products")
public class Product {

    public Product() {
    } // MUST have a no-arg constructor
    public Product(String name, String description, double price) {
        this.id = idCounter++;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    private static int idCounter = 1;

    @Column(primaryKey = true)
    private int id;

    @Column
    private String name; // Uses default mapping

    @Column(type = "TEXT")
    private String description; // Overrides type, uses field name as column name

    @Column
    private double price; // Automatically infers type to DOUBLE

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getId() {
        return id;
    }
}

