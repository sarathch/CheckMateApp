package com.example.syennamani.checkmate;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by syennamani on 4/6/2017.
 */
@IgnoreExtraProperties
public class User {

    public String email;
    public String token;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String email, String token) {
        this.email = email;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }
}
