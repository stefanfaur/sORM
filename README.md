# sORM: Simple Object-Relational Mapper

sORM is a lightweight, naive Object-Relational Mapper (ORM) I built for my `Design and Architecture of Complex Software Systems` course. It aims to simplify database interactions by allowing developers to work with Java objects instead of SQL statements.

## Features

- **Annotations-Based Mapping**: Annotate your Java classes to define database entities and their relationships.
- **Schema Generation**: Automatically generate SQL schema from annotated classes.
- **CRUD Operations**: Perform Create, Read, Update, and Delete operations on database entities.
- **Supports Relationships**: Handle relationships between entities, including nested objects.
- **SQLite Support**: Built-in support for SQLite with an easy extension mechanism for other databases.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher.
- SQLite JDBC Driver (if using SQLite).

### Installation

Clone the repository:

```bash
git clone https://github.com/your-username/sORM.git
cd sORM
```

### Usage

#### 1. Define Your Entities

Annotate your Java classes with `@Entity` and fields with `@Column` to define your database schema. Here is an example with `User` and `Product` entities where a `User` can have a nested `Product`.

```java
import sORM.impl.annotations.Column;
import sORM.impl.annotations.Entity;

@Entity(tableName = "users")
public class User {

    @Column(primaryKey = true)
    private int id;

    @Column
    private String name;

    @Column(name = "registration_date", type = "DATE")
    private String registrationDate;

    @Column
    private Product product;  // Nested entity

    // No-arg constructor
    public User() { }

    public User(String name, String registrationDate, Product product) {
        this.name = name;
        this.registrationDate = registrationDate;
        this.product = product;
    }
}

@Entity(tableName = "products")
public class Product {

    @Column(primaryKey = true)
    private int id;

    @Column
    private String name;

    @Column(type = "TEXT")
    private String description;

    @Column
    private double price;

    // No-arg constructor
    public Product() { }

    public Product(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
```

#### 2. Configure the Database Connection

Use `DatabaseAdapterFactory` to get a `DatabaseAdapter` for your preferred database.

```java
DatabaseAdapter adapter = DatabaseAdapterFactory.getDatabaseAdapter("sqlite");
adapter.connect("jdbc:sqlite:sorm.db");
```

#### 3. Generate Database Schema

Generate and execute the SQL schema based on your annotated classes.

```java
DatabaseSchemaGenerator generator = new DatabaseSchemaGenerator();
List<String> schemaCommands = generator.generateSchema(User.class, Product.class);
adapter.executeBatch(schemaCommands);
```

#### 4. Perform CRUD Operations

Create an `EntityManager` to perform CRUD operations.

```java
EntityManager entityManager = new EntityManagerImpl(adapter);

// Create a new product
Product product = new Product("Laptop", "High performance laptop", 1200.00);

// Create a new user with a nested product
User user = new User("John Doe", "1990-12-10", product);
entityManager.save(user);

// Find a user by name
User foundUser = entityManager.find(User.class, "name", "John Doe");
System.out.println("Found User: " + foundUser.getName() + ", Product: " + foundUser.getProduct().getName());

// Update the user's name and product price
user.setName("John Smith");
user.getProduct().setPrice(1100.00);
entityManager.update(user);

// Delete the user
entityManager.delete(user);
```

---

### Running the Examples

Examples are provided in the `clients` package. To run an example, execute the `Main` class in it. Make sure you have SQLite JDBC configured, when running in IntelliJ or in terminal:

```bash
cd clients/TruckShipments
javac sORM/clients/TruckShipments/Main.java 
java sORM.clients.TruckShipments.Main
```

### Extending sORM

To add support for a new database, implement the `DatabaseAdapter` interface and update `DatabaseAdapterFactory`.

