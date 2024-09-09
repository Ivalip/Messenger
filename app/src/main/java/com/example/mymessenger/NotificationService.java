package com.example.mymessenger;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class NotificationService extends Service {
    public static final String ChannelID = "ChannelID";

    private NotificationManagerCompat notificationManagerCompat;

    @SuppressLint("ServiceCast")
    @Override
    public void onCreate() {
        super.onCreate();
        startMyOwnForeground();
        notificationManagerCompat = NotificationManagerCompat.from(this);
        createNotificationChannel();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, ChannelID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Notification from onStartCommand")
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManagerCompat.notify(1, mBuilder.build());
        return START_STICKY;
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ChannelID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Notification from startMyOwnForeground")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(ChannelID,
                    "Уведомления", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Уведомления о входящих сообщениях");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }


}
