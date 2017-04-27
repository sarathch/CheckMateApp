package com.example.syennamani.checkmate;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    // The minimum distance to change Updates in meters
    private static final long LOCATION_REFRESH_DISTANCE = 1; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long LOCATION_REFRESH_TIME = 1; // 1 minute
    private Context context;
    CountDownTimer timer;
    private static final long timerNum = 60;
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            Intent intent = new Intent(context, MapsActivity.class);
            intent.putExtra("Latitude",location.getLatitude());
            intent.putExtra("Longitude", location.getLongitude());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

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
        isAFriendCheck(number, "outcall");
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Log.v(TAG, "Missed Call::" + number + " : " + start);
        context =ctx;
        isAFriendCheck(number, "incall");
    }

    protected void isAFriendCheck(final String number, final String callType){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends");
        ref.orderByChild("f_phone").equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.v(TAG,dataSnapshot.getKey());
                    Log.v(TAG,String.valueOf(dataSnapshot));
                    //context.startService(new Intent(context,LocationService.class));
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        Friend friend = postSnapshot.getValue(Friend.class);
                        if(callType.equals("incall"))
                            addTracker(friend.getF_uid());
                        else
                            isMissedCall(postSnapshot.getKey(),friend.getF_uid());
                    }
                }else
                    Log.v(TAG, "Not a friend -"+number);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    protected void startLocationService(final String fUid){
        Log.v(TAG, "fuid entry -"+fUid);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(fUid).child("friends");
        ref.orderByChild("f_uid").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.v(TAG,dataSnapshot.getKey());
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        postSnapshot.getRef().child("f_call_status").setValue(0);
                        context.startService(new Intent(context, LocationService.class));
                    }
                }else
                    Log.v(TAG, "No entry -"+fUid);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    protected void isMissedCall(String fKey, final String fUid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends").child(fKey).child("f_call_status");
        final ValueEventListener missedCallListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get location object and use the values to update the UI
                if(dataSnapshot.exists()) {
                    Log.v(TAG,dataSnapshot.getKey());
                    int call_status = dataSnapshot.getValue(Integer.class);
                    if(call_status==0){
                        Intent intent = new Intent(context, MapsActivity.class);
                        intent.putExtra("friendUid", fUid);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        ref.addValueEventListener(missedCallListener);
        startTimer(ref, missedCallListener);
    }

    protected void startTimer(final DatabaseReference ref, final ValueEventListener missedCallListener){
        timer = new CountDownTimer(timerNum*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                ref.removeEventListener(missedCallListener);
            }
        };
    }

    protected void addTracker(final String fUid){
        Log.v(TAG, "fuid entry -"+fUid);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.v(TAG,dataSnapshot.getKey());
                    Log.v(TAG,String.valueOf(dataSnapshot));
                    int trackers = dataSnapshot.child("trackers").getValue(Integer.class);
                    Log.v(TAG,"trackers::"+trackers);
                    dataSnapshot.getRef().child("trackers").setValue(++trackers);
                    startLocationService(fUid);
                }else
                    Log.v(TAG, "No entry -");
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
