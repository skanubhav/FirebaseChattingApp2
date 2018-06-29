package com.anubhav.firebasechattingapp2;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.anubhav.firebasechattingapp2.ChatActivityPackage.ChatActivity;
import com.anubhav.firebasechattingapp2.UserActivityPackage.MainActivity;
import com.anubhav.firebasechattingapp2.UserActivityPackage.User;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        /* There are two types of messages data messages and notification messages.
        Data messages are handled here in onMessageReceived whether the app is in the
        foreground or background. Data messages are the type traditionally used with GCM.
        Notification messages are only received here in onMessageReceived when the app is
        in the foreground. When the app is in the background an automatically generated notification is displayed. */
        String notificationTitle = null, notificationBody = null;
        User Sender = null, Reciever = null;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Sender = new User(
                    remoteMessage.getData().get("SenderName"),
                    remoteMessage.getData().get("SenderID"),
                    "",
                    "",
                    remoteMessage.getData().get("SenderDP")
            );

            Reciever = new User(
                    remoteMessage.getData().get("RecieverName"),
                    remoteMessage.getData().get("RecieverID"),
                    "",
                    "",
                    remoteMessage.getData().get("RecieverDP")
            );
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("FCM Recieved", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        if(ChatActivity.Reciever!=null) {
            if(!Sender.getUid().equals(ChatActivity.Reciever.getUid()))
                sendNotification(notificationTitle, notificationBody, Sender, Reciever);
        }
        else {
            sendNotification(notificationTitle, notificationBody, Sender, Reciever);
        }
    }

    /**
     //     * Create and show a simple notification containing the received FCM message.
     //     */
    private void sendNotification(String notificationTitle, String notificationBody, User Reciever, User Sender) {

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Log.d("FCM Recieve",Sender.toString());
        Log.d("FCM Recieve",Reciever.toString());

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("SenderID", Sender.getUid());
        intent.putExtra("SenderName", Sender.getUser());
        intent.putExtra("SenderPhoto", Sender.getProfilePictureURL());
        intent.putExtra("RecieverID", Reciever.getUid());
        intent.putExtra("RecieverName", Reciever.getUser());
        intent.putExtra("RecieverPhoto", Reciever.getProfilePictureURL());

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);


        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setSmallIcon(R.drawable.ic_notification_profile)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
