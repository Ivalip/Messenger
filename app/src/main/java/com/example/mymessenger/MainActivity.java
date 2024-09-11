package com.example.mymessenger;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Intent mServiceIntent;
    private NotificationService notificationService;
    public static final String APP_PREFERENCES = "mysettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        String MyUuid = sharedPref.getString("uuid_key", "");

        if (TextUtils.isEmpty(MyUuid)) {
            String uuid = UUID.randomUUID().toString();
            uuid = uuid.substring(0, 8);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("uuid_key", uuid);
            editor.commit();
        }
        Log.d("UUID", MyUuid);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, HomeFragment.class, null)
                    .setReorderingAllowed(true)
                    .commit();
        }

        /*notificationService = new NotificationService();
        mServiceIntent = new Intent(this, notificationService.getClass());
        if (!isMyServiceRunning(notificationService.getClass())) {
            startService(mServiceIntent);
        }
        startService(new Intent(this, NotificationService.class));*/
    }

    /*private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }*/

    @Override
    protected void onDestroy() {
        Intent broadcastIntent = new Intent();
        //broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }
}