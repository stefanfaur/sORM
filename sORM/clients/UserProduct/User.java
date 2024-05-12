package sORM.clients.UserProduct;

import sORM.impl.annotations.Column;
import sORM.impl.annotations.Entity;

// each entity class must be annotated with @Entity
// !!! each Entity must be in a separate file, otherwise Reflection breaks
// !!! each Entity must have a no-arg constructor

@Entity(tableName = "users")
public class User {

    public User() {
    } // MUST have a no-arg constructor

    public User(String name, String registrationDate) {
        this.id = idCounter++;
        this.name = name;
        this.registrationDate = registrationDate;
    }

    private static int idCounter = 1;

    @Column(primaryKey = true) // Automatically infers type and name, but explicitly set as primary key
    private int id;

    @Column
    private String name; // Automatically infers type and uses field name as column name

    @Column(name = "registration_date", type = "DATE")
    private String registrationDate; // Overrides the type and name

    @Column
    private Product product;

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
