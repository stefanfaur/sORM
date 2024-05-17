package sORM.clients.TruckShipments;

import sORM.clients.UserProduct.PowerUser;
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
        DatabaseAdapter adapter = DatabaseAdapterFactory.getDatabaseAdapter("sqlite");

        // get actual connection
        adapter.connect(connectionString);

        // create the tables if they're not already there
        try {
            // generate SQL schema
            DatabaseSchemaGenerator generator = new DatabaseSchemaGenerator();
            List<String> schemaCommands = generator.generateSchema(Truck.class, Shipment.class);

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

        // create some trucks
        Truck newTruck = new Truck("MAN", "AR07RPB", 3500);
        Truck newTruck2 = new Truck("Volvo", "AR08RPB", 4000);
        Truck newTruck3 = new Truck("Scania", "AR09RPB", 3800);
        printSplittingLine();


        Shipment newShipment = new Shipment("baterii", "Romania", "Italia", newTruck);
        Shipment newShipment2 = new Shipment("telefoane", "Romania", "Germania", newTruck2);
        Shipment newShipment3 = new Shipment("mancare", "Italia", "Romania", newTruck3);
        entityManager.save(newShipment);
        entityManager.save(newShipment2);
        entityManager.save(newShipment3);
        printSplittingLine();


        // find the truck by license plate
        Truck foundTruck = entityManager.find(Truck.class, "licensePlate", "AR07RPB");
        if (foundTruck != null) {
            System.out.println("Found Truck: " + foundTruck.getName() + " " + foundTruck.getLicensePlate());
        } else {
            System.out.println("Truck not found.");
        }
        printSplittingLine();


        // find the shipment by destination
        Shipment foundShipment = entityManager.find(Shipment.class, "destination", "Italia");
        if (foundShipment != null) {
            System.out.println("Found Shipment: " + foundShipment.getName() + " " + foundShipment.getDestination());
        } else {
            System.out.println("Shipment not found.");
        }
        printSplittingLine();


        // update the truck
        newTruck.setCapacity(4750);
        newTruck.setName("Mercedes");
        entityManager.update(newTruck);

        // update the shipment
        newShipment.setDestination("Spania");
        newShipment.setName("tastaturi");
        entityManager.update(newShipment);

        // delete the truck
        entityManager.delete(newTruck2);

        // delete the shipment
        entityManager.delete(newShipment2);


        adapter.disconnect();

    }
}

