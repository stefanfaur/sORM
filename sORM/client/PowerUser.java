package sORM.client;

import sORM.impl.annotations.Column;
import sORM.impl.annotations.Entity;

@Entity(tableName = "power_users")
public class PowerUser extends User{
    @Column
    private String powerLevel;

    public PowerUser() {
    }

    public PowerUser(int id, String name, String registrationDate, String powerLevel) {
        super(id, name, registrationDate);
        this.powerLevel = powerLevel;
    }

    public String getPowerLevel() {
        return powerLevel;
    }

    public void setPowerLevel(String powerLevel) {
        this.powerLevel = powerLevel;
    }
}