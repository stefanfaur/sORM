package sORM.clients.TruckShipments;

import sORM.impl.annotations.Column;
import sORM.impl.annotations.Entity;

// each entity class must be annotated with @Entity
// !!! each Entity must be in a separate file, otherwise Reflection breaks
// !!! each Entity must have a no-arg constructor

@Entity(tableName = "trucks")
public class Truck {
    private static int idCounter = 1;

    @Column(primaryKey = true)
    private Integer id;

    @Column
    private String name;

    @Column
    private String licensePlate;

    @Column
    private int capacity;

    public Truck() {
    } // MUST have a no-arg constructor

    public Truck(String name, String licensePlate, int capacity) {
        this.id = idCounter++;
        this.name = name;
        this.licensePlate = licensePlate;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

}

