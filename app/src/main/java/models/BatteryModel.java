package models;

public class BatteryModel {
    private long id;
    private String batteryName;
    private String batteryDescription;
    private double maxVoltage;
    private double cutoffVoltage; // Minimum voltage for operation. Effectively an "empty" battery
    private double capacity;

    public BatteryModel() {

    }

    public BatteryModel(long id, String batteryName, String batteryDescription, double maxVoltage,
                 double cutoffVoltage, double capacity) {
        this.id = id;
        this.batteryName = batteryName;
        this.batteryDescription = batteryDescription;
        this.maxVoltage = maxVoltage;
        this.cutoffVoltage = cutoffVoltage;
        this.capacity = capacity;
    }

    public long getId() {
        return id;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getCutoffVoltage() {
        return cutoffVoltage;
    }

    public double getMaxVoltage() {
        return maxVoltage;
    }

    public String getBatteryDescription() {
        return batteryDescription;
    }

    public String getBatteryName() {
        return batteryName;
    }
}
