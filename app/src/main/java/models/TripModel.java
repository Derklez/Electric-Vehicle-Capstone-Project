package models;

import java.util.ArrayList;

public class TripModel {
    private double averageSpeed;
    private long carId;
    private double distanceTraveled;
    private double elapsedTime;
    private double endVolts;
    private int id;
    private double startVolts;
    private TripDateModel tripDate;
    private ArrayList<Double> speedMeasurements;

    public TripModel(double averageSpeed, long carId, double distanceTraveled, double elapsedTime,
     double endVolts, int id, double startVolts, TripDateModel tripDate, ArrayList<Double> speedMeasurements) {
        this.averageSpeed = averageSpeed;
        this.carId = carId;
        this.distanceTraveled = distanceTraveled;
        this.elapsedTime = elapsedTime;
        this.endVolts = endVolts;
        this.id = id;
        this.startVolts = startVolts;
        this.tripDate = tripDate;
        this.speedMeasurements = speedMeasurements;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
}
