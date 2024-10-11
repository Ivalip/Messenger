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
import java.lang.reflect.Array;
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
    String type = "";
    boolean typeRecieved = false;
    String time = "";
    boolean timeRecieved = false;
    String receiverID = "";
    boolean recieverIDRecieved = false;
    String senderID = "";
    boolean senderIDRecieved = false;
    String text = "";
    boolean textRecieved = false;
    //Thread in = new Thread();
    int t1;
    int t2;
    int t3;
    int t4;
    int t5;
    int t6;

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
//        if(in.isAlive()){
//            AddMessage(new String(data));
//        } else {
//            in.start();
            AddMessage(new String(data));
//        }
    }

    public void AddMessage (String msg) {
        if (!msg.isEmpty()) {
            //if(!((message.endsWith("<") && msg.equals("<")) || (message.endsWith(">")) && msg.equals(">"))) {
            message += msg;
            //}
            t1 = message.indexOf("<TYPE>");
            t2 = message.indexOf("<ReceiverID>");
            t3 = message.indexOf("<SenderID>");
            t4 = message.indexOf("<START>");
            t5 = message.indexOf("<TIME>");
            t6 = message.indexOf("<END>");
        }
//        if (msg.endsWith("<TY")) {
//            message = "<TY";
//        }
        if (!typeRecieved) {
            if (t1 != -1 && t2 > t1) {
                Log.d("MSGHandler", "Get type: " + message);
                ArrayList<String> data = new ArrayList<>(Arrays.asList(message.substring(t1 + 6, t2).split("\\|")));
                if((Hash(data.get(0))+"").equals(data.get(1))){
                    type = data.get(0);
                    message = message.substring(t2);
                    typeRecieved = true;
                }
            }
        }
        if (!recieverIDRecieved) {
            if (t2 != -1 && t3 > t2) {
                Log.d("MSGHandler", "Get receiver: " + message);
                ArrayList<String> data = new ArrayList<>(Arrays.asList(message.substring(t2 + 12, t3).split("\\|")));
                if((Hash(data.get(0))+"").equals(data.get(1))) {
                    receiverID = data.get(0);
                    message = message.substring(t3);
                    recieverIDRecieved = true;
                }
            }
        }
        if(!senderIDRecieved) {
            if (t3 != -1 && t4 > t3) {
                Log.d("MSGHandler", "Get sender: " + message);
                ArrayList<String> data = new ArrayList<>(Arrays.asList(message.substring(t3 + 10, t4).split("\\|")));
                if((Hash(data.get(0))+"").equals(data.get(1))) {
                    senderID = data.get(0);
                    message = message.substring(t4);
                    senderIDRecieved = true;
                }
            }
        }
        if(!textRecieved) {
            if (t4 != -1 && t5 > t4) {
                Log.d("MSGHandler", "Get text: " + message);
                ArrayList<String> data = new ArrayList<>(Arrays.asList(message.substring(t4 + 7, t5).split("\\|")));
                if((Hash(data.get(0))+"").equals(data.get(1))) {
                    text = data.get(0);
                    message = message.substring(t5);
                    textRecieved = true;
                }
            }
        }
        if(!timeRecieved) {
            if (t5 != -1 && t6 > t5) {
                Log.d("MSGHandler", "Get time: " + message);
                ArrayList<String> data = new ArrayList<>(Arrays.asList(message.substring(t5 + 6, t6).split("\\|")));
                if((Hash(data.get(0))+"").equals(data.get(1))) {
                    time = data.get(0);
                    message = "";
                    timeRecieved = true;
                }
            }
        }
//        Log.d("MESSAGERecieved", "" + typeRecieved + recieverIDRecieved +
//                senderIDRecieved + textRecieved + timeRecieved);
        if (typeRecieved && recieverIDRecieved && senderIDRecieved && textRecieved && timeRecieved
                && message.contains("###")){
            Log.d("RECIEVE", "content: "+text+
                    " reciever: "+receiverID+
                    " sender: "+senderID+
                    " time: "+time+
                    " type: "+type);
            service.insertMessage(new ChatMessage(text, DataFormater.formater(time), senderID, receiverID, type));
            type = "";
            typeRecieved = false;
            receiverID = "";
            recieverIDRecieved = false;
            senderID = "";
            senderIDRecieved = false;
            text = "";
            textRecieved = false;
            time = "";
            timeRecieved = false;
        }
        t1 = message.lastIndexOf("<");
        t2 = message.lastIndexOf(">");
        if(t1 != -1 && t2 > t1) {
            message = message.substring(t1);
        }
        if (message.contains("###")){
            message = "";
        }
//        if (message.contains("<END>")) {
//            Log.d("SYSMSG", message);
//            type = message.substring(message.indexOf("<TYPE>") + 6, message.indexOf("<ReceiverID"));
//            List<String> myList = new ArrayList<>(Arrays.asList(message.substring(message.indexOf("<ReceiverID>") + 12, message.indexOf("<SenderID>")).split(",")));
//            receiverID = message.substring(message.indexOf("<ReceiverID>") + 12, message.indexOf("<SenderID>"));
//            senderID = message.substring(message.indexOf("<SenderID>") + 10, message.indexOf("<START>"));
//            text = message.substring(message.indexOf("<START>") + 7, message.indexOf("<TIME>"));
//            time = message.substring(message.indexOf("<TIME>") + 6, message.length() - 5);
//            service.insertMessage(new ChatMessage(text, DataFormater.formater(time), senderID, receiverID, type));
//            message = "";
//        }
    }

    public void SendMessage (String msg, String numberOfChat,
                              String MyUuid, String type) throws IOException {
        String time = System.currentTimeMillis() + "";
        msg = "<TYPE>" + type + "|" + Hash(type) +
                "<ReceiverID>" + numberOfChat + "|" + Hash(numberOfChat) +
                "<SenderID>" + MyUuid + "|" + Hash(MyUuid) +
                "<START>" + msg + "|" + Hash(msg) +
                "<TIME>" + time + "|" + Hash(time) + "<END>";
        for (int i = 0; i < 3; i++) {
            Log.d("MSGHANDKER", "Sending " + msg);
            usbSerialPort.write(msg.getBytes(), 0);
        }
        Log.d("MSGHANDKER", "Sending ###");
        usbSerialPort.write("###".getBytes(), 0);
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

    public int Hash(String s){
        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*31 + s.charAt(i);
        }
        return hash;
    }

}
