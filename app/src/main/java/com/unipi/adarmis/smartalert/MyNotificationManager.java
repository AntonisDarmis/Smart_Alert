package com.unipi.adarmis.smartalert;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MyNotificationManager {

    private Context mContext;
    private static MyNotificationManager mInstance;

    private MyNotificationManager(Context context) {
        mContext = context;
    }

    public static synchronized MyNotificationManager getInstance(Context context) { //singleton pattern
        if(mInstance==null) {
            mInstance = new MyNotificationManager(context);
        }
        return mInstance;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void displayNotification(String title, String body) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext,Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(body);

        Intent intent = new Intent(mContext, UserPage.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,0,intent,PendingIntent.FLAG_IMMUTABLE);

        mBuilder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager!=null) {
            notificationManager.notify(1,mBuilder.build());
        }
    }

}
