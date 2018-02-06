package com.example.syennamani.checkmate;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import com.example.syennamani.checkmate.Database.User;
import com.example.syennamani.checkmate.Database.UserLocation;
import com.example.syennamani.checkmate.Firebase.MyFirebaseMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.logging.Logger;

public class EmailPasswordActivity extends BaseActivity implements
        View.OnClickListener {

    private EditText mEmailField;
    private EditText mPasswordField;
    private TextView tvForgotPwd;
    private String mPhoneNumber = "";
    private ArrayList<String> permissionsItems = new ArrayList<>();
    private Context context;
    private static final int REQUEST_PHONE_LOCATION = 0;
    private final String TAG = getClass().getSimpleName();
    private boolean hasPermissions = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);
        context = this;
        // Views
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        tvForgotPwd = (TextView) findViewById(R.id.forgot_pwd);
        tvForgotPwd.setPaintFlags(tvForgotPwd.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_create_account_button).setOnClickListener(this);
        tvForgotPwd.setOnClickListener(this);
        checkPermissionsAndInit();

    }

    private void fetchPhoneNumber() {
        try {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                hasPermissions = false;
                return;
            }
            mPhoneNumber = tMgr.getLine1Number();
        }catch (Exception e){
            Log.e("Exception",e.toString());
        }
        /** TODO
         *  Check for validity of phone number and handle accordingly
         */
        if(mPhoneNumber.length()>10){
            mPhoneNumber = mPhoneNumber.substring(mPhoneNumber.length()-10);
        }
        if(mPhoneNumber==null) mPhoneNumber ="9999999999";
        Log.v(TAG+" phone number ", ""+mPhoneNumber);
    }

    private void checkPermissionsAndInit() {

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        boolean simcardAvailable = tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
        if(!simcardAvailable)
            hasPermissions = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            // handling read sms permission
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissionsItems.add(Manifest.permission.READ_SMS);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)){
                    // Provide rationale in Snackbar with button to request permission
                    Snackbar.make(findViewById(android.R.id.content), R.string.permission_read_sms_rationale, Snackbar.LENGTH_INDEFINITE).show();
                }
            }
            // handling read phone state permission
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                permissionsItems.add(Manifest.permission.READ_PHONE_STATE);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)){
                    // Provide rationale in Snackbar with button to request permission
                    Snackbar.make(findViewById(android.R.id.content), R.string.permission_phone_state_rationale, Snackbar.LENGTH_INDEFINITE).show();
                }
            }
            // handling outgoing calls permission
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.PROCESS_OUTGOING_CALLS) != PackageManager.PERMISSION_GRANTED) {
                permissionsItems.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.PROCESS_OUTGOING_CALLS)){
                    // Provide rationale in Snackbar with button to request permission
                    Snackbar.make(findViewById(android.R.id.content), R.string.permission_read_sms_rationale, Snackbar.LENGTH_INDEFINITE).show();
                }
            }
            // handling location permission
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsItems.add(Manifest.permission.ACCESS_FINE_LOCATION);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)){
                    // Provide rationale in Snackbar with button to request permission
                    Snackbar.make(findViewById(android.R.id.content), R.string.permission_location_rationale, Snackbar.LENGTH_INDEFINITE).show();
                }
            }
            if (!permissionsItems.isEmpty()) {
                String[] params = permissionsItems.toArray(new String[permissionsItems.size()]);
                ActivityCompat.requestPermissions(this, params, REQUEST_PHONE_LOCATION);
            }
        }else
            fetchPhoneNumber();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PHONE_LOCATION: {

                if(myFirebaseMethods.verifyPermissions(grantResults)) {
                    // On granted
                    fetchPhoneNumber();
                    hasPermissions = true;
                }else{
                    hasPermissions = false;
                }
                break;
            }
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            //Send email to verify
                            sendEmailVerification();

                        }
                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password) {
        Log.d(
                TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            //mStatusTextView.setText(R.string.auth_failed);
                        }else
                        {
                            checkIfEmailVerified();
                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void sendEmailVerification() {

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(EmailPasswordActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            myFirebaseMethods.showAlertDialog("VERIFY EMAIL", "A verification email has been sent to you. Please authenticate to login.","");
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(EmailPasswordActivity.this,
                                    "Failed to send verification email. Please provide valid email",
                                    Toast.LENGTH_SHORT).show();
                            myFirebaseMethods.showAlertDialog("VERIFY EMAIL", "Email verification failed. Please enter valid email address","");
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    private void checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified())
        {
            // user is verified, so you can finish this activity or send user to activity which you want.
            //finish();
            Toast.makeText(EmailPasswordActivity.this, "Successfully logged in", Toast.LENGTH_SHORT).show();
            User mUser = new User(mEmailField.getText().toString(),GlobalValues.getInstanceIdToken(),mPhoneNumber, new UserLocation(40.71f,70.00f),0);
            myFirebaseMethods.readUserData(mUser);
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            Toast.makeText(EmailPasswordActivity.this, "Please Verify Your Email", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();

            //restart this activity

        }
    }


    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button) {
            if(!hasPermissions)
                retryPermissionsRequest();
            else
                createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.email_sign_in_button) {
            if(!hasPermissions)
                retryPermissionsRequest();
            else
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.forgot_pwd) {
            if(!hasPermissions)
                retryPermissionsRequest();
            else
                showCustomDialog("FORGOT PASSWORD", "ForgotPwd");
        }
    }

    private void retryPermissionsRequest(){
        // Rerequest permissions on button clicks
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("NEED PERMISSION");
            builder.setMessage("You have to allow app to read phone state and location details to use it's features");
            builder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // continue with delete
                    if (!permissionsItems.isEmpty()) {
                        String[] params = permissionsItems.toArray(new String[permissionsItems.size()]);
                        ActivityCompat.requestPermissions(EmailPasswordActivity.this, params, REQUEST_PHONE_LOCATION);
                    }
                    dialog.dismiss();
                }
            });
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.show();
    }

}
