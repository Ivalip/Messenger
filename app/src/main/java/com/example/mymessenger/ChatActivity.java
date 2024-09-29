package com.example.mymessenger;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymessenger.Database.Entity.ChatMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    String numberOfChat;
    ViewModel viewModel;
    ChatRecyclerViewAdapter adapter;
    NotificationService notificationService = ServiceLocator.getNotificationService();
    MessageHandler messageHandler;

    ImageButton edit;
    EditText text;
    RecyclerView recyclerView;

    AppCompatButton backBut;
    String MyUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_acivity);

        Intent intent = getIntent();

        recyclerView = findViewById(R.id.MESSAGES);
        edit = findViewById(R.id.button);
        text = findViewById(R.id.editText);
        backBut = findViewById(R.id.BackBtn);

        messageHandler = notificationService.getMessageHandler();
//        attachService();

        SharedPreferences sharedPref = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        MyUuid = sharedPref.getString("uuid_key", "");
        Log.d("UUID_c", MyUuid);

        backBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                finish();
            }
        });

        viewModel = new ViewModel();
        numberOfChat = intent.getStringExtra("Number");
        viewModel.createByID(getApplicationContext(), numberOfChat);
        List<ChatMessage> messageList = new ArrayList<>();

        //Observing our LiveData for showing it in recyclerView.
        viewModel.mutableLiveData.observe(this, (Observer<? super List<ChatMessage>>)
                new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(List<ChatMessage> messages) {
                if (messages.size() == 0) {
                    Log.d("EMPTY MESSAGES", "Empty list of messages");
                } else {
                    messageList.clear();
                    for (int i = 0; i < messages.size(); i++) {
                        messageList.add(messages.get(i));
                    }
                    adapter.newAddedData(messageList);
                }
            }
        });

        adapter = new ChatRecyclerViewAdapter(getApplicationContext(), messageList, viewModel);
        Log.d("Service", isMyServiceRunning(NotificationService.class)+"");
        recyclerView.setAdapter(adapter);

//        if (notificationService.isConnection()) {
//            edit.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (text.getText().equals("")) {    }
//                    else {
//                        try {
//                            notificationService.sendMessage(text.getText().toString(), numberOfChat, MyUuid);
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        viewModel.insert(new ChatMessage(text.getText().toString(),
//                                DataFormater.formater(System.currentTimeMillis() + ""),
//                                MyUuid, numberOfChat, "USER"));
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                viewModel.mutableLiveData = viewModel.getById(numberOfChat);
//                            }
//                        }).start();
//                        Log.d("INSERT", "OBSERVE");
//                        text.setText("");
//                    }
//                }
//            });
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (messageHandler.isConnected()) {
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (text.getText().equals("")) {

                    }
                    else {
                        try {
                            notificationService.sendMessage(text.getText().toString(), numberOfChat, MyUuid);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        viewModel.insert(new ChatMessage(text.getText().toString(),
                                DataFormater.formater(System.currentTimeMillis() + ""),
                                MyUuid, numberOfChat, "USER"));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                viewModel.mutableLiveData = viewModel.getById(numberOfChat);
                            }
                        }).start();
                        Log.d("INSERT", "OBSERVE");
                        text.setText("");
                    }
                }
            });
        }
    }

//    private void attachService() {
//        Intent service = new Intent(this, NotificationService.class);
//        bindService(service, mConnection, Service.BIND_AUTO_CREATE);
//    }
//
//    private void detachService() {
//        unbindService(mConnection);
//    }

    @Override
    protected void onDestroy() {
        Log.d("SMERT", "PLAK");
//        detachService();
        super.onDestroy();
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }
}