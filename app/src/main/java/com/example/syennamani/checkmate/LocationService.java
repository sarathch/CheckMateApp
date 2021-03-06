package com.example.syennamani.checkmate;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by syennamani on 4/24/2017.
 */

public class LocationService extends Service {

    protected DatabaseReference mDatabase;
    protected FirebaseAuth mAuth;
    private Context context;
    private final String TAG = getClass().getSimpleName();

    // The minimum distance to change Updates in meters
    private static final long LOCATION_REFRESH_DISTANCE = 1; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long LOCATION_REFRESH_TIME = 1; // 1 minute

    LocationManager mLocationManager;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {

            Log.v("Lcoation", "Changed::" + "Latitude::" + location.getLatitude() + "Longitude::" + location.getLongitude());

            //your code here
            updateLocation(location.getLatitude(), location.getLongitude());
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

    /**
     * Update location details in user db
     * @param latitude
     * @param longitude
     */
    private void updateLocation(final double latitude, final double longitude) {
        mDatabase.child(mAuth.getCurrentUser().getUid()).child("userLocation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.getRef().child("latitude").setValue(latitude);
                    dataSnapshot.getRef().child("longitude").setValue(longitude);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }


    private void trackLocationService() {
        DatabaseReference ref = mDatabase.child(mAuth.getCurrentUser().getUid()).child("trackers");
        ValueEventListener trackerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get location object and use the values to update the UI
                if (dataSnapshot.exists()) {
                    Log.v(TAG, dataSnapshot.getKey());
                    int trackers = dataSnapshot.getValue(Integer.class);
                    Log.v(TAG, "trackers::" + trackers);
                    if (trackers == 0) {
                        mLocationManager.removeUpdates(mLocationListener);
                    } else {
                        Log.v("Request Location", "here");
                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Log.v("Permissions", "Not granted");
                            stopSelf();
                            return;
                        }
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME,
                                LOCATION_REFRESH_DISTANCE, mLocationListener);
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
        ref.addValueEventListener(trackerListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate(){
        super.onCreate();
        Log.v(TAG, "Service Created");
        context = this;
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        trackLocationService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service Started");
        /**
         *  Temp location code
         */
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.v("Permissions", "Not granted");
            stopSelf();
        }
        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        updateLocation(latitude,longitude);
        return super.onStartCommand(intent, flags, startId);
    }
}
