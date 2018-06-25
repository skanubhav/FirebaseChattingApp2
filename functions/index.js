// imports firebase-functions module
const functions = require('firebase-functions');
// imports firebase-admin module
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.pushNotification = functions.database.ref('/Notifications/{pushId}').onWrite( ( change,context) => {
console.log('Push notification event triggered');
/* Grab the current value of what was written to the Realtime Database */
    const Data = change.after.val();
    console.log(Data.uname);
    console.log(Data.message);
/* Create a notification and data payload. They contain the notification information, and message to be sent respectively */
    const payload = {
        notification: {
            title: "New Message from " + Data.uname,
            body: Data.message,
            sound: "default"
        },
        data: {
            title: Data.uname,
            message: Data.uid
        }
    };
/* Create an options object that contains the time to live for the notification and the priority. */
    const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24 //24 hours
    };
return admin.messaging().sendToTopic("/topics/user_" + Data.uid, payload, options);
});
