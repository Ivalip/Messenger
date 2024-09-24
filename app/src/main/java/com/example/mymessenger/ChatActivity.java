package com.example.mymessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymessenger.Database.Entity.ChatMessage;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements SerialInputOutputManager.Listener {
    String numberOfChat;
    ViewModel viewModel;
    ChatRecyclerViewAdapter adapter;
    private boolean connected = false;
    private SerialInputOutputManager usbIoManager;
    ImageButton edit;
    EditText text;
    RecyclerView recyclerView;
    String message = "";
    UsbSerialPort usbSerialPort;
    BroadcastReceiver broadcastReceiver;
    Handler mainLooper;
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

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        mainLooper = new Handler(Looper.getMainLooper());

        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);

        if (availableDrivers.isEmpty()) {
            System.out.println("Drivers empty");
            Log.d("Driver empty", availableDrivers.toString());
        }
        else {
            // Open a connection to the first available driver.
            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {
                // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
                Log.d("Connection empty", connection.toString());
                return;
            }

            usbSerialPort = driver.getPorts().get(0);
            Log.d("PORTS", driver.getPorts().toString());

            // Most devices have just one port (port 0)
            try {
                usbSerialPort.open(connection);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Set parameters of our device.
            try {
                usbSerialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            usbIoManager = new SerialInputOutputManager(usbSerialPort, this);
            usbIoManager.start();
            connected = true;
        }

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
        recyclerView.setAdapter(adapter);

        if (connected) {
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (text.getText().equals("")) {    }
                    else {
                        try {
                            writeMessage(text.getText().toString());
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
    @Override
    public void onNewData(byte[] data) {
            runOnUiThread(() -> {
               createMessage(new String(data));
            });
    }
    public void createMessage (String msg) {
        if (!msg.isEmpty()) {
            message += msg;
        }
        if (message.contains("<END>")) {
            String time = message.substring(message.indexOf("<TIME>") + 6, message.length() - 5);
            Long ReceiverID = Long.parseLong(message.substring(message.indexOf("<ReceiverID>") + 12, message.indexOf("<SenderID>")));
            Long SenderID = Long.parseLong(message.substring(message.indexOf("<SenderID>") + 10, message.indexOf("<START>")));
            String text = message.substring(message.indexOf("<START>") + 7, message.indexOf("<TIME>"));
            viewModel.insert(new ChatMessage(text, DataFormater.formater(time), SenderID.toString(),"0", "USER"));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    viewModel.mutableLiveData = viewModel.getById(String.valueOf(numberOfChat));
                }
            }).start();
            message = "";
        }
    }
    public void writeMessage (String msg) throws IOException {
        msg = "<ReceiverID>" + numberOfChat + "<SenderID>" + MyUuid + "<START>" + msg + "<TIME>" + System.currentTimeMillis() + "<END>";
        usbSerialPort.write(msg.getBytes(), 2000);
    }

    @Override
    public void onRunError(Exception e) {

    }



}