package com.example.mymessenger;

import static com.example.mymessenger.MainActivity.APP_PREFERENCES;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.mymessenger.Database.Entity.ChatMessage;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

    public static final String ChannelID = "ChannelID";
    String MyUuid;
    Short NetStatus = 0;
    Short DelayStatus = 0;
    ArrayList<String> recievers;
    Net net;
    Repository repository;
    FusedLocationProviderClient mFusedLocationClient;
    private NotificationManagerCompat notificationManagerCompat;

//    FusedLocationProviderClient mFusedLocationClient;
    NotificationService notificationService = this;
    MessageHandler messageHandler;
    int PERMISSION_ID = 44;
    public MyBinder binder = new MyBinder();
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if(NetStatus == 0){
                net = new Net(notificationService, MyUuid);
                try {
                    NetStatus = 1;
                    net.build(); //start forming net
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                NetStatus = 0;
            }
            Log.d("TIMER", System.currentTimeMillis()+"");
            timerHandler.postDelayed(this, 600000); // time between net checks
        }
    };


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
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        timerHandler.postDelayed(timerRunnable, 10000);
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
                             String MyUuid, String type) throws IOException {
        if (messageHandler.isConnected()) {
            //Log.d("SEND", msg + "<RECIEVER>" + receiver + "<SENDER>" + MyUuid);
            messageHandler.SendMessage(msg, receiver, MyUuid, type);
        }
    }

    public void insertMessage(ChatMessage message) {
        //if(message.receiver != MyUuid)
        switch (message.type){
            case "USER":
            case "GEO":
                recievers = new ArrayList<>(Arrays.asList(message.receiver.split(",")));
                if (recievers.get(0).equals(MyUuid)){
                    if (recievers.size() > 1){
                        try {
                            this.sendMessage(message.content, message.receiver.substring(message.receiver.indexOf(",") + 1), message.sender, message.type);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                repository.insert(message);
                            };
                        }).start();
                    }
                } else if (recievers.get(0).equals("0") && !recievers.contains(MyUuid)) {
                    try {
                        this.sendMessage(message.content, message.receiver + "," + MyUuid, message.sender, message.type);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            repository.insert(new ChatMessage(message.content, message.time, message.sender, "0", message.type));
                        };
                    }).start();
                }
                break;
            case "SYS":
                if (message.receiver.isEmpty() && !message.content.startsWith("GRAPH")){ // netbuilding message
                    if(!message.content.contains(MyUuid)) {
                        try {
                            this.sendMessage("DELAY" + System.currentTimeMillis(), message.sender, MyUuid, "SYS");
                            DelayStatus = 0;
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() { // wait for furter nodes
                                    Log.d("DELAYSTATUS", DelayStatus+"");
                                    try {
                                        if (DelayStatus == 0) {
                                            notificationService.sendMessage("REBOUND" + message.content + "," + MyUuid, message.sender, MyUuid, "SYS");
                                        }
                                        DelayStatus = 0;
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }, 2000);
                            if (NetStatus == 0) {
                                net = new Net(this, MyUuid);
                                if (message.content.isEmpty()) {
                                    this.sendMessage(MyUuid, "", MyUuid, "SYS");
                                } else {
                                    this.sendMessage(message.content + "," + MyUuid, "", MyUuid, "SYS");
                                }
                                net.sender = message.sender; // remember sender
                                NetStatus = 1;
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else if (message.content.startsWith("DELAY") && message.receiver.equals(MyUuid)) { // delay return signal
                    DelayStatus = 1;
                    Long myTime = System.currentTimeMillis();
                    Long sentTime = Long.parseLong(message.content.substring(5));
                    Long delay =  myTime - sentTime;
                    Log.d("DELAY", "My: " + System.currentTimeMillis() + "\nSent: " + sentTime + "\nDelay: " + delay);
                    net.cells.put(message.sender, delay); // write down delay
                } else if (message.content.startsWith("REBOUND") && message.receiver.equals(MyUuid)){ // netbuilding back signal
                    if (net.sender != null && !net.sender.equals(message.sender)){ // passing threw
                        Long delay = net.cells.get(message.sender);
                        if(delay != null) {
                            try {
                                this.sendMessage(message.content + "|" + delay, message.sender, MyUuid, "SYS");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } // send back
                    } else { // initial reached

                        net.construct(message.content + "|" + net.cells.get(message.sender));
                    }
                } else if (message.content.startsWith("GRAPH")){
                    recievers = new ArrayList<>(Arrays.asList(message.receiver.split(",")));
                    if (recievers.get(0).equals(MyUuid)){
                        if (recievers.size() > 1){
                            try {
                                this.sendMessage(message.content, message.receiver.substring(message.receiver.indexOf(",") + 1), message.sender, "SYS");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } else {

                            net = new Net(this, MyUuid, message.content.substring(5));
                        }
                    }
                }
        }
    }



}
