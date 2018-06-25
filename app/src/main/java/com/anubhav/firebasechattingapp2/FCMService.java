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
        String dataTitle = null, dataMessage = null;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("FCM recieved", "Message data payload: " + remoteMessage.getData().get("message"));
            dataTitle = remoteMessage.getData().get("title");
            dataMessage = remoteMessage.getData().get("message");
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("FCM Recieved", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
       // sendNotification(notificationTitle, notificationBody, dataTitle, dataMessage);
    }

    /**
     //     * Create and show a simple notification containing the received FCM message.
     //     */
    private void sendNotification(String notificationTitle, String notificationBody, String dataTitle, String dataMessage) {

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setSmallIcon(R.mipmap.default_profile_image)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
