package com.ashish.dalaapmusicplayer;

import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import androidx.core.app.NotificationCompat;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String channel_id="Alaap_Music_Player";

        @Override
        public void onMessageReceived(RemoteMessage remoteMessage){
            super.onMessageReceived(remoteMessage);


            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channel_id)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setStyle(new NotificationCompat.BigTextStyle())
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setSmallIcon(R.drawable.ic_music_note_black_24dp)
                    .setAutoCancel(true);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0, notificationBuilder.build());
        }




}



