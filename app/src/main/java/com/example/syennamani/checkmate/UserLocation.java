package com.example.syennamani.checkmate;

/**
 * Created by syennamani on 4/24/2017.
 */

public class UserLocation {

    private double latitude;
    private double longitude;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public UserLocation() {
    }

    public UserLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
