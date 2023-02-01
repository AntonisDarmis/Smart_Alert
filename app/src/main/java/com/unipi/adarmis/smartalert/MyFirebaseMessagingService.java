package com.unipi.adarmis.smartalert;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN",s);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //String title = remoteMessage.getNotification().getTitle();
        //String body = remoteMessage.getNotification().getBody();

        Map<String,String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("content");

        MyNotificationManager.getInstance(getApplicationContext())
                .displayNotification(title,body);
    }

}
