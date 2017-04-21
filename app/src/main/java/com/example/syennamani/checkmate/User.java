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

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String email, String token, String phone) {
        this.email = email;
        this.token = token;
        this.phone = phone;
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

}
