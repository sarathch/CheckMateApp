package com.example.syennamani.checkmate;

/**
 * Created by syennamani on 4/5/2017.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
       /* Alert Dialog Code Start*/
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
                fetchUserSpecificToken(userEmail);
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

    protected void insertUserData(User mUser){
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child(userId).setValue(mUser);
    }

    protected void readUserData(){
        mDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);

                Log.d(TAG, "User email: " + user.getEmail() + ", token " + user.getToken());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    protected void fetchUserSpecificToken(String userEmail){
        mDatabase.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "PARENT: " + childDataSnapshot.getKey());
                    Log.d(TAG, "" + childDataSnapshot.child("email").getValue());
                    Log.d(TAG, "" + childDataSnapshot.child("token").getValue());
                    if(childDataSnapshot.getKey().equals(mAuth.getCurrentUser().getUid())){
                        showAlertDialog("INVALID OPERATION","","");
                    }else{

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

}
