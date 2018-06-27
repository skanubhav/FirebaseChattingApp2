// imports firebase-functions module
const functions = require('firebase-functions');
// imports firebase-admin module
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.pushNotification = functions.database.ref('/Notifications/{pushId}').onWrite( (change, context) => {
console.log('Push notification event triggered');

/* Grab the current value of what was written to the Realtime Database */
    var Data = change.after.val();
    console.log(Data.sender.user);
    console.log(Data.reciever.user)
    console.log(Data.chatMessage);
/* Create a notification and data payload. They contain the notification information, and message to be sent respectively */
    const payload = {
        notification: {
            title: "New Message from " + Data.sender.user,
            body: Data.chatMessage,
            sound: "default"
        },
        data: {
            RecieverID: Data.reciever.uid,
            SenderID: Data.sender.uid,
            RecieverName: Data.reciever.user,
            SenderName: Data.sender.user,
            RecieverDP: Data.reciever.profilePictureURL,
            SenderDP: Data.sender.profilePictureURL
        }
    };
/* Create an options object that contains the time to live for the notification and the priority. */
    const options = {
        priority: "high",
        timeToLive: 60 * 60 * 24 //24 hours
    };
return admin.messaging().sendToTopic("/topics/user_" + Data.reciever.uid, payload, options);
});
