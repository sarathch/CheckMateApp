package com.example.syennamani.checkmate;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

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
        Log.v(TAG, "Outgoing Call Ended::" + number + " : " + start);
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Log.v(TAG, "Missed Call::" + number + " : " + start);
        /*Intent intent = new Intent(ctx, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);*/
        context = ctx;
        LocationManager mLocationManager = (LocationManager) ctx.getSystemService(ctx.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
    }
}
