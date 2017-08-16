package com.example.syennamani.checkmate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

/**
 * Created by syennamani on 4/21/2017.
 */

public class CallReceiver extends PhonecallReceiver {
    public static String TAG = "CallReceiver";
    private Context context;

    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        Log.v(TAG, "Incoming Call::" + number + " : " + start);
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        Log.v(TAG, "Incoming Call Answered::" + number + " : " + start);
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.v(TAG, "Incoming Call Ended::" + number + " : " + start);
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.v(TAG, "Outgoing Call::" + number + " : " + start);
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.v(TAG, "Outgoing Call Ended::" + number + " : " + start+" : "+end);
        context = ctx;
        new MyFirebaseMethods(context).isAFriendCheck(number, "outcall");
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Log.v(TAG, "Missed Call::" + number + " : " + start);
        context =ctx;
        new MyFirebaseMethods(context).isAFriendCheck(number, "incall");
    }
}
