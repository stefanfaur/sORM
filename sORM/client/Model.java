package sORM.client;

import sORM.impl.annotations.Column;
import sORM.impl.annotations.Entity;

@Entity(tableName = "users")
class User {
    @Column(primaryKey = true) // Automatically infers type and name, but explicitly set as primary key
    private int id;

    private String name; // Automatically infers type and uses field name as column name

    @Column(name = "registration_date", type = "DATE")
    private String registrationDate; // Overrides the type and name
}

@Entity(tableName = "products")
class Product {
    @Column(primaryKey = true)
    private int id;

    private String name; // Uses default mapping

    @Column(type = "TEXT")
    private String description; // Overrides type, uses field name as column name

    private double price; // Automatically infers type to DOUBLE
}

