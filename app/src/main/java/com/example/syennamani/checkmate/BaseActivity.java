package com.example.syennamani.checkmate;

/**
 * Created by syennamani on 4/5/2017.
 */

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class BaseActivity extends AppCompatActivity {

    @VisibleForTesting
    public ProgressDialog mProgressDialog;
    protected AlertDialog mAlertDialog;
    protected FirebaseAuth mAuth;
    protected DatabaseReference mDatabase;
    private final String TAG = getClass().getSimpleName();
    private Context context;

    // [START declare_auth_listener]
    protected FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

    }


    protected void signOut() {
        mAuth.signOut();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    protected void showAlertDialog(String title, String message, String action){
        new AlertDialog.Builder(this)
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

    protected void showAlertDialogEditText(String title, String message, final String action){
        //Alert Dialog Code Start
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title); //Set Alert dialog title here
        alert.setMessage(message); //Message here

        // Set an EditText view to get user input
        final EditText input = new EditText(context);
        alert.setView(input);

        alert.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //You will get as string input data in this variable.
                // here we convert the input to a string and show in a toast.
                String userEmail = input.getEditableText().toString();
                Toast.makeText(context,userEmail,Toast.LENGTH_LONG).show();
                insertFriendEntry(userEmail);
            } // End of onClick(DialogInterface dialog, int whichButton)
        }); //End of alert.setPositiveButton
        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                dialog.cancel();
            }
        }); //End of alert.setNegativeButton
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    protected void showCustomDialog(String action){
        final Dialog dialog = new Dialog(this);

        Window window = dialog.getWindow();
        window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_custom);
        dialog.show();
        TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_title);
        tvTitle.setText("ADD FRIEND");
        TextView tvBody = (TextView) dialog.findViewById(R.id.tv_body);
        tvBody.setText("Enter a valid Email");

        final EditText etBody = (EditText) dialog.findViewById(R.id.et_body);

        Button bt_yes = (Button)dialog.findViewById(R.id.btn_yes);
        Button bt_no = (Button)dialog.findViewById(R.id.btn_no);
        bt_yes.setText("ADD");
        bt_no.setText("CANCEL");

        bt_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = etBody.getEditableText().toString().trim();
                Toast.makeText(context,userEmail,Toast.LENGTH_LONG).show();
                insertFriendEntry(userEmail);
                dialog.dismiss();
            }
        });
        bt_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    protected void insertUserData(User mUser){
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child(userId).setValue(mUser);
    }

    protected void insertFriendData(Friend mFriend){
        DatabaseReference ref = mDatabase.child(mAuth.getCurrentUser().getUid()).child("friends");
        DatabaseReference newRef = ref.push();
        newRef.setValue(mFriend);
    }

    protected void readUserData(final User mUser){
        mDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    insertUserData(mUser);
                }else{
                    // Update instance Id token check
                    if(!dataSnapshot.child("token").getValue().equals(GlobalValues.getInstanceIdToken())){
                        dataSnapshot.getRef().child("token").setValue(GlobalValues.getInstanceIdToken());
                        Log.v(TAG, "FCM Token updated");
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

    protected void insertFriendEntry(final String userEmail){
        mDatabase.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "PARENT: " + childDataSnapshot.getKey());
                    Log.d(TAG, "" + childDataSnapshot.child("email").getValue());
                    Log.d(TAG, "" + childDataSnapshot.child("token").getValue());
                    String pUid = childDataSnapshot.getKey();
                    if(pUid.equals(mAuth.getCurrentUser().getUid())){
                        showAlertDialog("INVALID OPERATION","","");
                    }else{
                        Friend mFriend = new Friend(userEmail, pUid, false);
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

    // [START on_start_add_listener]
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    // [END on_start_add_listener]

    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        GlobalValues.setInForeground(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        GlobalValues.setInForeground(true);
    }

}
