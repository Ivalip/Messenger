package com.example.mymessenger;

import static com.example.mymessenger.MainActivity.APP_PREFERENCES;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mymessenger.Database.Entity.ChatMessage;

import java.io.IOException;

public class NotificationService extends Service {

    public static final String ChannelID = "ChannelID";
    String MyUuid;
    Repository repository;

    private NotificationManagerCompat notificationManagerCompat;
    MessageHandler messageHandler;

    public MyBinder binder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "CREATED");

        SharedPreferences sharedPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        MyUuid = sharedPref.getString("uuid_key", "");
        repository = new Repository(this);

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        messageHandler = new MessageHandler(this, this, usbManager);

        notificationManagerCompat = NotificationManagerCompat.from(this);
        createNotificationChannel();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        }
        Log.d("Service", "ON_START_COMMAND");
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, ChannelID)
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setContentTitle("Notification from onStartCommand")
//                .setContentText("Much longer text that cannot fit one line...")
//                .setStyle(new NotificationCompat.BigTextStyle()
//                .bigText("Much longer text that cannot fit one line..."))
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//        notificationManagerCompat.notify(1, mBuilder.build());

        return START_STICKY;
    }

    public String getMyUuid() {
        return MyUuid;
    }

    public Repository getRepository() {
        return repository;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }
    //    private void startMyOwnForeground()
//    {
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ChannelID);
//        Notification notification = notificationBuilder.setOngoing(true)
//                .setContentTitle("Notification from startMyOwnForeground")
//                .setPriority(NotificationManager.IMPORTANCE_MIN)
//                .setCategory(Notification.CATEGORY_SERVICE)
//                .build();
//        startForeground(2, notification);
//    }

    public boolean isConnection () {
        Log.d("Handler", "CALLED");
        if (messageHandler.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public class MyBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
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

    public void sendMessage (String msg, String receiver,
                             String MyUuid) throws IOException {
        messageHandler.SendMessage(msg, receiver, MyUuid);
    }

    public void insertMessage(ChatMessage message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                repository.insert(message);
            };
        }).start();
    }

}
