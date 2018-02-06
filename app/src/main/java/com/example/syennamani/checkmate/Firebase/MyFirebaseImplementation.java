package com.example.syennamani.checkmate.Firebase;

import com.example.syennamani.checkmate.Database.Friend;
import com.example.syennamani.checkmate.Database.User;

interface MyFirebaseImplementation{

    /******************************
     *  Creating Firebase database
     *******************************/

    /**
     * Insert User data
     * @param mUser - user details object
     */
    public void insertUserData(User mUser);

    /**
     * Insert Friend Data
     * @param mFriend - freind details object
     */
    public void insertFriendData(Friend mFriend);

    /**
     * Read User Data
     * @param mUser - user details object
     */
    public void readUserData(final User mUser);

    /**
     * Insert Friend Email
     * @param userEmail - user email to update
     */
    public void insertFriendEntry(final String userEmail);


    /********************************************************
     * Handling Friend requests through Firebase Messaging
     *******************************************************/

    /**
     * Update Friend entries on accepting friend request
     * @param friendUID
     */
    public void updateSenderFriendEntry(String friendUID);

    /**
     * Delete Friend Entry on rejecting friend request
     * @param userUID
     * @param friendUID
     */
    public void deleteFriendEntry(String userUID, String friendUID);



    /***************************
     *  Handling Map Events
     ****************************/

    /**
     * Check If Incoming call or Outgoing call is from a friend
     * @param number    - Phone number
     * @param callType  - Incoming or Outgoing
     */
    public void isAFriendCheck(final String number, final String callType);

    /**
     * Set status flag if incoming call is a missed call
     * @param fUid - caller friend id
     */
    public void setMissedCallStatus(final String fUid);

    /**
     * Check if outgoing call was a missed call
     * @param fKey - Friend entry key
     * @param fUid - Friend Uid entry - f_uid
     */
    public void isMissedCall(String fKey, final String fUid);

    /**
     * Update Tracker entry to track number of location requests to that device
     * @param fUid  -   friend id
     */
    public void addTracker(final String fUid);

    /**
     * Remove Tracker entry on exiting map activity
     */
    public void removeTracker(final String fUid);


    /**************************
     * Forgot Password
     ************************/

    /**
     * Method to handle forgot password
     */
    public void handleForgotPwd(String uEmail);

    /***************************
     *  Miscellaneous Utilities
     ****************************/

    /**
     * Display Alert Box
     * @param title
     * @param message
     * @param action
     */
    public void showAlertDialog(String title, String message, String action);

    /**
     * Method to verify if user granted permissions
     * @param grantResults
     * @return
     */
    public boolean verifyPermissions(int[] grantResults);
}