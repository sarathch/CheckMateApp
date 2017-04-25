package com.example.syennamani.checkmate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by syennamani on 4/13/2017.
 */

public class NotificationActivity extends Activity{
    private final String TAG = getClass().getSimpleName();
    protected FirebaseAuth mAuth;
    protected DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        Intent intent  = getIntent();
        //Check for FR Extra
        if(intent.hasExtra("FR_MAP")){
            Log.v(TAG, "Friend Request received");
            HashMap<String, String> frMap = (HashMap<String, String>)intent.getSerializableExtra("FR_MAP");
            showFriendRequestDialog(frMap);
        }else {
            // Now finish, which will drop the user in to the activity that was at the top
            //  of the task stack
            finish();
        }
    }

    protected void showFriendRequestDialog(final HashMap<String,String> frMap){
        final Dialog dialog = new Dialog(this);
        //set dialog gravity
        Window window = dialog.getWindow();
        window.setGravity(Gravity.TOP);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        TextView tvBody = (TextView) dialog.findViewById(R.id.tv_body);
        tvBody.setText(frMap.get("senderEmail")+" sent you a friend request");

        EditText etBody = (EditText) dialog.findViewById(R.id.et_body);
        etBody.setVisibility(View.GONE);

        Button bt_yes = (Button)dialog.findViewById(R.id.btn_yes);
        Button bt_no = (Button)dialog.findViewById(R.id.btn_no);
        final String senderEmail = frMap.get("senderEmail");
        final String senderUID = frMap.get("senderUID");
        final String senderPhone = frMap.get("senderPhone");
        bt_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Request accepted");
                Friend mFriend = new Friend(senderEmail, senderUID, true,senderPhone,1);
                insertFriendData(mFriend);
                updateSenderFriendEntry(senderUID);
                dialog.dismiss();
                // Now finish, which will drop the user in to the activity that was at the top
                //  of the task stack
                finish();
            }
        });
        bt_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Log.v(TAG, "Request rejected");
                // Now finish, which will drop the user in to the activity that was at the top
                //  of the task stack
                deleteFriendEntry(senderUID,mAuth.getCurrentUser().getUid());
                finish();
            }
        });
    }

    protected void insertFriendData(Friend mFriend){
        DatabaseReference ref = mDatabase.child(mAuth.getCurrentUser().getUid()).child("friends");
        DatabaseReference newRef = ref.push();
        newRef.setValue(mFriend);
    }

    protected void updateSenderFriendEntry(final String friendUID){
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

    protected void deleteFriendEntry(final String userUID, final String friendUID){
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
}
