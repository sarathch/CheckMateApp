package com.example.syennamani.checkmate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.syennamani.checkmate.Database.Friend;
import com.example.syennamani.checkmate.Firebase.MyFirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

/**
 * Created by syennamani on 4/13/2017.
 */

public class NotificationActivity extends Activity{
    private final String TAG = getClass().getSimpleName();
    protected FirebaseAuth mAuth;
    protected DatabaseReference mDatabase;
    private MyFirebaseMethods myFirebaseMethods;
    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        myFirebaseMethods = new MyFirebaseMethods(context);
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
                myFirebaseMethods.insertFriendData(mFriend);
                myFirebaseMethods.updateSenderFriendEntry(senderUID);
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
                myFirebaseMethods.deleteFriendEntry(senderUID,mAuth.getCurrentUser().getUid());
                finish();
            }
        });
    }
}
