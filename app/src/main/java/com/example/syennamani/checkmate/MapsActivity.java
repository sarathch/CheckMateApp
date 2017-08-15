package com.example.syennamani.checkmate;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double mLatitude;
    private double mLongitude;
    private final String TAG = getClass().getSimpleName();
    protected DatabaseReference mDatabase;
    protected FirebaseAuth mAuth;
    protected String fUid="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        if(intent.hasExtra("friendUid"))
            fUid = intent.getStringExtra("friendUid");
        if(fUid.isEmpty()){
            Toast.makeText(this, "Cannot Retrieve Location",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(fUid).child("userLocation");
        ValueEventListener locationListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get location object and use the values to update the UI
                if(dataSnapshot.exists()) {
                    Log.v(TAG,dataSnapshot.getKey());
                    UserLocation userLocation = dataSnapshot.getValue(UserLocation.class);
                    mLatitude = userLocation.getLatitude();
                    mLongitude = userLocation.getLongitude();
                    Log.v(TAG, "Latitude::"+mLatitude+"Longitude::"+mLongitude);
                    if (mMap != null) {
                        LatLng location = new LatLng(mLatitude, mLongitude);
                        mMap.addMarker(new MarkerOptions().position(location).title("Current UserLocation"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f));
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
        ref.addValueEventListener(locationListener);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng location = new LatLng(40.71f,70.00f);
        mMap.addMarker(new MarkerOptions().position(location).title("Current UserLocation"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14.0f));
        //mMap.setMaxZoomPreference(17.0f);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exit Tracking")
                .setMessage("Are you sure you want to stop tracking..?")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeTracker(fUid);
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    protected void removeTracker(final String fUid){
        Log.v(TAG, "fuid entry -"+fUid);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(fUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.v(TAG,dataSnapshot.getKey());
                    Log.v(TAG,String.valueOf(dataSnapshot));
                    int trackers = dataSnapshot.child("trackers").getValue(Integer.class);
                    Log.v(TAG,"trackers::"+trackers);
                    dataSnapshot.getRef().child("trackers").setValue(--trackers);
                }else
                    Log.v(TAG, "No entry -");
                finish();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                finish();
            }
        });
    }
}
