package models;

import java.util.ArrayList;

public class TripModel {
    private double averageSpeed;
    private long carId;
    private long batteryId;
    private double distanceTraveled;
    private double elapsedTime;
    private double endVolts;
    private double startVolts;
    private TripDateModel tripDate;
    private ArrayList<Double> speedMeasurements;
    private ArrayList<ArrayList<Double>> accelMeasurements;

    public TripModel() {}

    public TripModel(double averageSpeed, long carId, long batteryId, double distanceTraveled, double elapsedTime,
     double endVolts, double startVolts, TripDateModel tripDate, ArrayList<Double> speedMeasurements,
     ArrayList<ArrayList<Double>> accelMeasurements) {
        this.averageSpeed = averageSpeed;
        this.carId = carId;
        this.setBatteryId(batteryId);
        this.distanceTraveled = distanceTraveled;
        this.elapsedTime = elapsedTime;
        this.endVolts = endVolts;
        this.startVolts = startVolts;
        this.tripDate = tripDate;
        this.speedMeasurements = speedMeasurements;
        this.setAccelMeasurements(accelMeasurements);
    }


    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(long carId) {
        this.carId = carId;
    }

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public void setDistanceTraveled(double distanceTraveled) {
        this.distanceTraveled = distanceTraveled;
    }

    public double getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(double elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public double getEndVolts() {
        return endVolts;
    }

    public void setEndVolts(double endVolts) {
        this.endVolts = endVolts;
    }

    public double getStartVolts() {
        return startVolts;
    }

    public void setStartVolts(double startVolts) {
        this.startVolts = startVolts;
    }

    public TripDateModel getTripDate() {
        return tripDate;
    }

    public void setTripDate(TripDateModel tripDate) {
        this.tripDate = tripDate;
    }

    public ArrayList<Double> getSpeedMeasurements() {
        return speedMeasurements;
    }

    public ArrayList<ArrayList<Double>> getAccelMeasurements() {
        return accelMeasurements;
    }

    public void setAccelMeasurements(ArrayList<ArrayList<Double>> accelMeasurements) {
        this.accelMeasurements = accelMeasurements;
    }

    public long getBatteryId() {
        return batteryId;
    }

    public void setBatteryId(long batteryId) {
        this.batteryId = batteryId;
    }
}
