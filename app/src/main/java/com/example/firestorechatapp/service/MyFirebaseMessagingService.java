package com.example.firestorechatapp.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.firestorechatapp.AppConstants;
import com.example.firestorechatapp.ChatActivity;
import com.example.firestorechatapp.MainActivity;
import com.example.firestorechatapp.R;
import com.example.firestorechatapp.callback.FirebaseCallback;
import com.example.firestorechatapp.util.FirestoreUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService implements FirebaseCallback {
    public String userName,userId,notificationMsg,chatChannel;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage.getNotification()!= null){
            Log.d("FCM", remoteMessage.getData().toString());
            userName=remoteMessage.getData().getOrDefault(AppConstants.USER_NAME,"");
            userId=remoteMessage.getData().getOrDefault(AppConstants.USER_ID,"");
            notificationMsg=remoteMessage.getData().getOrDefault("Message","");
            chatChannel=remoteMessage.getData().getOrDefault("Parent","");
            FirestoreUtil.getChatChannelActiveStatus(remoteMessage.getData().getOrDefault("Parent",""),this);
           // Toast.makeText(getApplicationContext(), "message received", Toast.LENGTH_SHORT).show();
            //sendNotification("hello","new message received");
        }
    }

    @Override
    public void getChatChannelStatus(boolean active) {
        Log.d("FCM", "status received");
        if (!active){
            //show notification
            Intent intent=new Intent(this,ChatActivity.class);
            intent.putExtra(AppConstants.USER_NAME,userName);
            intent.putExtra(AppConstants.USER_ID,userId);
            showNotification(this,userName+" sent you a message.",notificationMsg,intent);
        }
    }

    public void showNotification(Context context, String title, String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_camera)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(body);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, mBuilder.build());
    }
}
