package models;

public class TripDateModel {
    private long tripStart;
    private long tripEnd;
    private String tripString;

    public TripDateModel() {}

    public TripDateModel(long tripStart, long tripEnd, String tripString) {
        this.tripStart = tripStart;
        this.tripEnd = tripEnd;
        this.tripString = tripString;
    }

    public long getTripStart() {
        return tripStart;
    }

    public void setTripStart(long tripStart) {
        this.tripStart = tripStart;
    }

    public long getTripEnd() {
        return tripEnd;
    }

    public void setTripEnd(long tripEnd) {
        this.tripEnd = tripEnd;
    }

    public String getTripString() {
        return tripString;
    }

    public void setTripString(String tripString) {
        this.tripString = tripString;
    }
}
