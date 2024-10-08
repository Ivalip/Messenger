package com.example.mymessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.example.mymessenger.Database.Entity.ChatMessage;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageHandler implements SerialInputOutputManager.Listener {
    Context context;
    boolean connected = false;
    private SerialInputOutputManager usbIoManager;
    String message = "";
    UsbSerialPort usbSerialPort;
    BroadcastReceiver broadcastReceiver;
    UsbManager usbManager;
    NotificationService service;

    public MessageHandler(Context context, NotificationService service, UsbManager usbManager) {
        Log.d("Handler", "CREATED");
        this.service = service;
        this.usbManager = usbManager;
        this.context = context;
    }

    public boolean isConnected () {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Log.d("Driver empty", availableDrivers.toString());
            return false;
        }
        else {
            // Open a connection to the first available driver.
            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {
                // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
                Log.d("Connection empty", connection.toString());
                return false;
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
        }
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        connected = true;
        return true;
    }

    @Override
    public void onNewData(byte[] data) {
        new Thread(() -> {
            AddMessage(new String(data));
        }).start();
    }

    public void AddMessage (String msg) {
        if (!msg.isEmpty()) {
            message += msg;
        }
        if (message.contains("<END>")) {
            Log.d("SYSMSG", message);
            String type = message.substring(message.indexOf("<TYPE>")+6, message.indexOf("<ReceiverID"));
            String time = message.substring(message.indexOf("<TIME>") + 6, message.length() - 5);
            List<String> myList = new ArrayList<>(Arrays.asList(message.substring(message.indexOf("<ReceiverID>") + 12, message.indexOf("<SenderID>")).split(",")));
            String ReceiverID = message.substring(message.indexOf("<ReceiverID>") + 12, message.indexOf("<SenderID>"));
            String SenderID = message.substring(message.indexOf("<SenderID>") + 10, message.indexOf("<START>"));
            String text = message.substring(message.indexOf("<START>") + 7, message.indexOf("<TIME>"));
            service.insertMessage(new ChatMessage(text, DataFormater.formater(time), SenderID, ReceiverID, type));
            message = "";

    }

    public void SendMessage (String msg, String numberOfChat,
                              String MyUuid, String type) throws IOException {
        msg = "<TYPE>" + type + "<ReceiverID>" + numberOfChat + "<SenderID>" + MyUuid +
                "<START>" + msg + "<TIME>" + System.currentTimeMillis() + "<END>";
        usbSerialPort.write(msg.getBytes(), 2000);
    }

    @Override
    public void onRunError(Exception e) {

    }

    public String formatter(String time) {
        Long inttime = Long.parseLong(time);
        inttime = inttime / 1000;
        long seconds = inttime % 60;
        inttime = inttime / 60;
        long day = inttime / 1440;
        long hour = (inttime  % 1440) / 60;
        long minutes = (inttime % 1440) % 60;
        return day + ":" + String.format("%2s", hour).replace(' ', '0') + ":" +
                String.format("%2s", minutes).replace(' ', '0') + ":" +
                String.format("%2s", seconds).replace(' ', '0');
    }

}
