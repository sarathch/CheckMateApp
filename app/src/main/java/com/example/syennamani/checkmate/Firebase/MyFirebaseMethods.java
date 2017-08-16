package com.example.syennamani.checkmate.Firebase;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.syennamani.checkmate.ConnectToMateActivity;
import com.example.syennamani.checkmate.Database.Friend;
import com.example.syennamani.checkmate.GlobalValues;
import com.example.syennamani.checkmate.MapsActivity;
import com.example.syennamani.checkmate.Database.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by DenshiOtoko on 8/15/17.
 */

public class MyFirebaseMethods implements MyFirebaseImplementation {

    public static String TAG = "MyFirebaseMethods";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
    private Context context;
    public MyFirebaseMethods(Context context){
        this.context = context;
    }

    @Override
    public void insertUserData(User mUser) {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child(userId).setValue(mUser);
    }

    @Override
    public void insertFriendData(Friend mFriend) {
        DatabaseReference ref = mDatabase.child(mAuth.getCurrentUser().getUid()).child("friends");
        DatabaseReference newRef = ref.push();
        newRef.setValue(mFriend);
    }

    @Override
    public void readUserData(final User mUser) {
        mDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    insertUserData(mUser);
                    Intent intent = new Intent(context, ConnectToMateActivity.class);
                    context.startActivity(intent);
                }else{
                    // Update instance Id token check
                    if(!dataSnapshot.child("token").getValue().equals(GlobalValues.getInstanceIdToken())){
                        dataSnapshot.getRef().child("token").setValue(GlobalValues.getInstanceIdToken());
                        Log.v(TAG, "FCM Token updated");
                    }

                    Intent intent = new Intent(context, ConnectToMateActivity.class);
                    context.startActivity(intent);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void insertFriendEntry(final String userEmail) {
        mDatabase.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "PARENT: " + childDataSnapshot.getKey());
                    Log.d(TAG, "" + childDataSnapshot.child("email").getValue());
                    Log.d(TAG, "" + childDataSnapshot.child("token").getValue());
                    String pUid = childDataSnapshot.getKey();
                    String userPhone = childDataSnapshot.child("phone").getValue().toString();
                    if(pUid.equals(mAuth.getCurrentUser().getUid())){
                        showAlertDialog("INVALID OPERATION","","");
                    }else{
                        Friend mFriend = new Friend(userEmail, pUid, false, userPhone,1);
                        insertFriendData(mFriend);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void updateSenderFriendEntry(String friendUID) {
        DatabaseReference ref = mDatabase.child(friendUID).child("friends");
        ref.orderByChild("f_uid").equalTo(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "PARENT: " + childDataSnapshot.getKey());
                    Log.d(TAG, "" + childDataSnapshot.child("f_email").getValue());
                    Log.d(TAG, "" + childDataSnapshot.child("f_status").getValue());
                    childDataSnapshot.getRef().child("f_status").setValue(true);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void deleteFriendEntry(String userUID, String friendUID) {
        DatabaseReference ref = mDatabase.child(userUID).child("friends");
        ref.orderByChild("f_uid").equalTo(friendUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "PARENT: " + childDataSnapshot.getKey());
                    Log.d(TAG, "" + childDataSnapshot.child("f_email").getValue());
                    Log.d(TAG, "" + childDataSnapshot.child("f_status").getValue());
                    dataSnapshot.getRef().setValue(null);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void isAFriendCheck(final String number, final String callType) {
        DatabaseReference ref = mDatabase.child(mAuth.getCurrentUser().getUid()).child("friends");
        ref.orderByChild("f_phone").equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.v(TAG,dataSnapshot.getKey());
                    Log.v(TAG,String.valueOf(dataSnapshot));
                    //context.startService(new Intent(context,LocationService.class));
                    for (final DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        final Friend friend = postSnapshot.getValue(Friend.class);
                        if(callType.equals("incall"))
                            setMissedCallStatus(friend.getF_uid());
                        else {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    isMissedCall(postSnapshot.getKey(), friend.getF_uid());
                                }
                            }, 3000); // 3000 milliseconds delay
                        }
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

    @Override
    public void setMissedCallStatus(final String fUid) {
        Log.v(TAG, "fuid entry -"+fUid);
        DatabaseReference ref = mDatabase.child(fUid).child("friends");
        ref.orderByChild("f_uid").equalTo(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.v(TAG,dataSnapshot.getKey());
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        postSnapshot.getRef().child("f_call_status").setValue(1);
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

    @Override
    public void isMissedCall(String fKey, final String fUid) {
        DatabaseReference ref = mDatabase.child(mAuth.getCurrentUser().getUid()).child("friends").child(fKey).child("f_call_status");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get location object and use the values to update the UI
                if(dataSnapshot.exists()) {
                    Log.v(TAG,dataSnapshot.getKey());
                    int call_status = dataSnapshot.getValue(Integer.class);
                    if(call_status==1){
                        dataSnapshot.getRef().setValue(0);
                        addTracker(fUid);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    @Override
    public void addTracker(final String fUid) {
        Log.v(TAG, "fuid entry -"+fUid);
        DatabaseReference ref = mDatabase.child(fUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.v(TAG,dataSnapshot.getKey());
                    Log.v(TAG,String.valueOf(dataSnapshot));
                    int trackers = dataSnapshot.child("trackers").getValue(Integer.class);
                    Log.v(TAG,"trackers::"+trackers);
                    dataSnapshot.getRef().child("trackers").setValue(++trackers);
                    // Invoke Map Activity
                    Intent intent = new Intent(context, MapsActivity.class);
                    intent.putExtra("friendUid", fUid);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
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

    @Override
    public void removeTracker(String fUid) {
        Log.v(TAG, "fuid entry -"+fUid);
        DatabaseReference ref = mDatabase.child(fUid);
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
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void handleForgotPwd(String uEmail) {
        mAuth.sendPasswordResetEmail(uEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    @Override
    public void showAlertDialog(String title, String message, String action) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
