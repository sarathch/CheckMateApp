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
exports.sendFriendNotification = functions.database.ref('/users/{uid}/email').onWrite(event => {
  const uid = event.params.uid;
/*  var getDeviceTokensPromise='';
  var email = '';*/
  // If un-follow we exit the function.
/*  if (!event.data.val()) {
    return console.log('User ', uid, 'un-followed user', followedUid);
  }*/
  console.log('We have a new login:', uid);

  // Get the list of device notification tokens.
 /* var ref = admin.database().ref(`/users/${uid}`);
  ref.once('value').then(function(snapshot){
    console.log('key:',snapshot.key,'email:', snapshot.child("email").val(), 'token', snapshot.child("token").val());
    getDeviceTokensPromise = snapshot.child("token").val();
    email = snapshot.child("email").val();
  });*/
  const getDeviceTokensPromise = admin.database().ref(`/users/${uid}`).once('value');

  // Get the user profile.
  const getUserProfilePromise = admin.auth().getUser(uid);

  return Promise.all([getDeviceTokensPromise, getUserProfilePromise]).then(results => {
    const tokensSnapshot = results[0];
    const user = results[1];
    // Check if there are any device tokens.
    if (!tokensSnapshot.hasChildren()) {
      return console.log('There are no notification tokens to send to.');
    }
    console.log('There are', tokensSnapshot.numChildren(), 'tokens to send notifications to.');
    console.log('Fetched user profile', user);
    // Notification details.
    const payload = {
      notification: {
        title: 'You have a new login!',
        body: `${user.email} is now loggedin to app.`
      }
    };

    // Listing all tokens.
    const tokens = tokensSnapshot.child("token").val();

    if (!tokens) {
      return console.log('There are no notification tokens to send to.');
    }

    console.log('Fetched user token', tokens);
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
