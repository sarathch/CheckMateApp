package com.example.syennamani.checkmate;

/**
 * Created by DenshiOtoko on 4/11/17.
 */

public class Friend {
    public String email;
    public String ftoken;

    public Friend(){

    }

    public Friend(String email, String ftoken) {
        this.email = email;
        this.ftoken = ftoken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFtoken() {
        return ftoken;
    }

    public void setFtoken(String ftoken) {
        this.ftoken = ftoken;
    }

}
