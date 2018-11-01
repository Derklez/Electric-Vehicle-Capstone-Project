package models;

public class VehicleModel {
    private long id;
    private String vehicleName;
    private String vehicleDescription;

    public VehicleModel() {

    }

    public VehicleModel(long id, String vehicleName, String vehicleDescription) {
        this.id = id;
        this.vehicleName = vehicleName;
        this.vehicleDescription = vehicleDescription;
    }

    public long getId() {
        return id;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public String getVehicleDescription() {
        return vehicleDescription;
    }
}
