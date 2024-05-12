package sORM.clients.TruckShipments;

import sORM.impl.annotations.Column;
import sORM.impl.annotations.Entity;

// each entity class must be annotated with @Entity
// !!! each Entity must be in a separate file, otherwise Reflection breaks
// !!! each Entity must have a no-arg constructor

@Entity(tableName = "shipments")
public class Shipment {
    private static int idCounter = 1;

    @Column(primaryKey = true)
    private Integer id;

    @Column
    private String name;

    @Column
    private String origin;

    @Column
    private String destination;

    @Column
    private Truck truck;

    public Shipment() {
    } // MUST have a no-arg constructor

    public Shipment(String name, String origin, String destination, Truck truck) {
        this.id = idCounter++;
        this.name = name;
        this.origin = origin;
        this.destination = destination;
        this.truck = truck;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }
}
