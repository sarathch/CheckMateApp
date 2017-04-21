/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

/**
 * Triggers when a user gets a new follower and sends a notification.
 *
 * Followers add a flag to `/followers/{followedUid}/{followerUid}`.
 * Users save their device notification tokens to `/users/{followedUid}/notificationTokens/{notificationToken}`.
 */
exports.sendFriendNotification = functions.database.ref('/users/{uid}/friends/{fid}/f_status').onWrite(event => {
  const uid = event.params.uid;
  const fid = event.params.fid;
  var fUid = '';
  // If un-follow we exit the function.
/*  if (!event.data.val()) {
    return console.log('User ', uid, 'un-followed user', followedUid);
  }*/
  console.log('Friend request check:', uid);

  // Get the list of device notification tokens.
  var ref = admin.database().ref(`/users/${uid}/friends/${fid}`);
  ref.once('value').then(function(snapshot){
    console.log('key:',snapshot.key,'f_email:', snapshot.child("f_email").val(), 'f_uid:', snapshot.child("f_uid").val());
    //getDeviceTokensPromise = snapshot.child("token").val();
    fUid = snapshot.child("f_uid").val();
    var fStatus = snapshot.child("f_status").val();

    // return if friend status flag is set to true
    if(fStatus)
      return console.log('Ignore Friend Request - friend status true');

    const getFriendEntryPromise = admin.database().ref(`/users/${uid}/friends/${fid}`).once('value');
    console.log('Friend request check FUID:', fUid);
    // Get the user profile.
    const getUserDetailsPromise = admin.database().ref(`/users`).once('value');

    return Promise.all([getFriendEntryPromise, getUserDetailsPromise]).then(results => {
      const friendEntrySnapshot = results[0];
      const friendDetailsSnapshot = results[1].child(fUid).val();
      const senderDetailsSnapshot = results[1].child(uid).val();
      // Check if there are any device tokens.
      if (!friendEntrySnapshot.hasChildren()) {
        return console.log('Ignore Friend request');
      }
      console.log('Fetched friend details', friendDetailsSnapshot);
      console.log('Fetched user details', senderDetailsSnapshot);
      // Notification details.
      const payload = {
        data: {
          code: 'FR',
          senderUID: uid,
          senderEmail: senderDetailsSnapshot.email,
          senderPhone: senderDetailsSnapshot.phone
        }
      };

      // Listing all tokens.
      const tokens = friendDetailsSnapshot.token;
      console.log('Fetched friend fcm token', tokens);

      if (!tokens) {
        return console.log('There are no notification tokens to send to.');
      }

      // Send notifications to all tokens.
      // Send a message to the device corresponding to the provided
      // registration token.
      admin.messaging().sendToDevice(tokens, payload)
        .then(function(response) {
        // See the MessagingDevicesResponse reference documentation for
        // the contents of response.
        console.log("Successfully sent message:", response);
      })
      .catch(function(error) {
        console.log("Error sending message:", error);
      });

/*    return admin.messaging().sendToDevice(tokens, payload).then(response => {
      // For each message check if there was an error.
      const tokensToRemove = [];
      response.results.forEach((result, index) => {
        const error = result.error;
        if (error) {
          console.error('Failure sending notification to', tokens[index], error);
          // Cleanup the tokens who are not registered anymore.
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            tokensToRemove.push(tokensSnapshot.ref.child(tokens[index]).remove());
          }
        }
      });
      return Promise.all(tokensToRemove);
    });*/
    });
  });
});
