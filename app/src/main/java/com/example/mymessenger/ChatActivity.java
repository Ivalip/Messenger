package com.example.mymessenger;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymessenger.Database.Entity.ChatMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    String chatId;
    ViewModel viewModel;
    ChatRecyclerViewAdapter adapter;
    NotificationService notificationService = ServiceLocator.getNotificationService();
    MessageHandler messageHandler;

    ImageButton send;
    ImageButton geo;
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
        send = findViewById(R.id.button);
        geo = findViewById(R.id.GEO);
        text = findViewById(R.id.editText);
        backBut = findViewById(R.id.BackBtn);

        messageHandler = notificationService.getMessageHandler();
        chatId = intent.getStringExtra("ChatID");

        SharedPreferences sharedPref = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        MyUuid = sharedPref.getString("uuid_key", "");
        Log.d("UUID_c", MyUuid);

        viewModel = new ViewModel();

        //Observing our LiveData for showing it in recyclerView
        viewModel.createByID(getApplicationContext(), chatId, MyUuid);
        List<ChatMessage> messageList = new ArrayList<>();
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

        backBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (messageHandler.isConnected()) {
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (text.getText().equals("")) {
                    }
                    else {
                        String recieverID = "";
                        if(chatId.equals("0")){
                            recieverID = "0," + MyUuid;
                        } else {
                            try {
                                recieverID = String.join(",", notificationService.net.graph.get(chatId).Path);
                            } catch (Exception e) {
                                Toast toast = Toast.makeText(getApplicationContext(), "User off grid", Toast.LENGTH_LONG);
                                toast.show();
                                throw new RuntimeException(e);
                            }
                        }
                        try {
                            notificationService.sendMessage(text.getText().toString(), recieverID, MyUuid, "USER");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        viewModel.insert(new ChatMessage(text.getText().toString(),
                                DataFormater.formater(System.currentTimeMillis() + ""),
                                MyUuid, chatId, "USER"));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                viewModel.mutableLiveData = viewModel.getById(chatId, MyUuid);
                            }
                        }).start();
                        Log.d("INSERT", "OBSERVE");
                        text.setText("");
                    }
                }
            });
            geo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String recieverID = String.join(",", notificationService.net.graph.get(chatId).Path);
                        notificationService.sendMessage("", recieverID, MyUuid, "GEO");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
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