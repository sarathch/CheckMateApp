package com.example.syennamani.checkmate;

/**
 * Created by DenshiOtoko on 4/11/17.
 */

public class Friend {

    public String f_email;
    public String f_uid;
    public boolean f_status;
    public String f_phone;
    public int f_call_status;

    public Friend(){

    }

    public Friend(String f_email, String f_uid, boolean f_status, String f_phone, int f_call_status) {
        this.f_email = f_email;
        this.f_uid = f_uid;
        this.f_status = f_status;
        this.f_phone = f_phone;
        this.f_call_status = f_call_status;
    }

    public String getF_email() {
        return f_email;
    }

    public void setF_email(String f_email) {
        this.f_email = f_email;
    }

    public String getF_uid() {
        return f_uid;
    }

    public void setF_uid(String f_uid) {
        this.f_uid = f_uid;
    }

    public boolean isF_status() {
        return f_status;
    }

    public void setF_status(boolean f_status) {
        this.f_status = f_status;
    }


    public String getF_phone() {
        return f_phone;
    }

    public void setF_phone(String f_phone) {
        this.f_phone = f_phone;
    }

    public int getF_call_status() {
        return f_call_status;
    }

    public void setF_call_status(int f_call_status) {
        this.f_call_status = f_call_status;
    }

}
