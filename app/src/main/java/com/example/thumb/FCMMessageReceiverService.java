package com.example.thumb;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class FCMMessageReceiverService extends FirebaseMessagingService {
    public static final String TAG="MyTag";
    private static final String FCM_CHANNEL_ID ="gal" ;

     @SuppressLint("SetTextI18n")
     @Override
    public void onMessageReceived(@NotNull RemoteMessage remoteMessage){
         super.onMessageReceived(remoteMessage);


        Log.d(TAG, "onMessageReceived: called");
        Log.d(TAG, "onMessageReceived: Message received from: "+ remoteMessage.getFrom());

        if(remoteMessage.getNotification() != null){
            String title=remoteMessage.getNotification().getTitle();
            String body= remoteMessage.getNotification().getBody();

            Notification notification= new NotificationCompat.Builder(this,FCM_CHANNEL_ID).
                    setSmallIcon(R.drawable.ic_chat_black)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setColor(Color.RED)
                    .build();

            NotificationManager manager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(1002,notification);
        }

        if(remoteMessage.getData().size()>0){
            Log.d(TAG, "onMessageRecieved: Data size: "+remoteMessage.getData().size());
            for(String key: remoteMessage.getData().keySet()){
                Log.d(TAG, "onMessageRecieved key:  "+key + "Data" + remoteMessage.getData().get(key));

            }
            Log.d(TAG, "onMessageRecieved: Data:  "+remoteMessage.getData().toString());
        }


    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

}
