package com.example.syennamani.checkmate;

import android.content.Context;
import android.util.Log;

import com.example.syennamani.checkmate.Firebase.MyFirebaseMethods;

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
