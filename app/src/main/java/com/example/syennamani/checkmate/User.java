package com.example.syennamani.checkmate;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

/**
 * Created by syennamani on 4/6/2017.
 */
@IgnoreExtraProperties
public class User {

    public String email;
    public String token;
    public String phone;
    public UserLocation userLocation;
    public int trackers;



    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String email, String token, String phone, UserLocation userLocation, int trackers) {
        this.email = email;
        this.token = token;
        this.phone = phone;
        this.userLocation = userLocation;
        this.trackers = trackers;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }


    public int getTrackers() {
        return trackers;
    }

    public void setTrackers(int trackers) {
        this.trackers = trackers;
    }



}
