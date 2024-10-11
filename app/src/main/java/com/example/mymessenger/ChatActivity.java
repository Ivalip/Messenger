package com.example.mymessenger;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
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
    String latitude;
    String longitude;

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

        adapter = new ChatRecyclerViewAdapter(getApplicationContext(), messageList, viewModel, this);
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
                    } else {
                        String recieverID = "";
                        if (chatId.equals("0")) {
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
        }
        geo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //try {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    LocationListener locationListener = new MyLocationListener();

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
                        return;
                    }

//                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    Log.d("Coordinates", location.getLatitude() +" " + location.getLongitude());

                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);

                    String best = locationManager.getBestProvider(criteria, true);
                    // getLastKnownLocation so that user don't need to wait
                    Location location = locationManager.getLastKnownLocation(best);
                    Log.d("Coordinates", location.getLatitude() +" " + location.getLongitude());
                    String latitude = String.valueOf(location.getLatitude());
                    String longitude = String.valueOf(location.getLongitude());

//
//
//                    Criteria criteria = new Criteria();
//                    criteria.setAccuracy(Criteria.ACCURACY_FINE);
//
//                    String best = locationManager.getBestProvider(criteria, true);
//                    // getLastKnownLocation so that user don't need to wait
//                    Location location = locationManager.getLastKnownLocation(best);
//
//                    String latitude = String.valueOf(location.getLatitude());
//                    String longitude = String.valueOf(location.getLongitude());


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
                        notificationService.sendMessage(latitude + "\n" + longitude, recieverID, MyUuid, "GEO");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    viewModel.insert(new ChatMessage(latitude + "\n" + longitude,
                            DataFormater.formater(System.currentTimeMillis() + ""),
                            MyUuid, chatId, "GEO"));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            viewModel.mutableLiveData = viewModel.getById(chatId, MyUuid);
                        }
                    }).start();
                    Log.d("INSERT", "OBSERVE");
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
            }
        });
    }
//    @SuppressLint("MissingPermission")
//    private void getLastLocation() {
//        // check if permissions are given
//        if (checkPermissions()) {
//            // check if location is enabled
//            if (isLocationEnabled()) {
//                // getting last location from FusedLocationClient object
//                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        Location location = task.getResult();
//                        if (location == null) {
//                            requestNewLocationData();
//                        } else {
////                            latitudeTextView.setText(location.getLatitude() + "");
////                            longitTextView.setText(location.getLongitude() + "");
//                        }
//                    }
//                });
//            } else {
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(intent);
//            }
//        } else {
//            // if permissions aren't available,
//            // request for permissions
//            requestPermissions();
//        }
//    }



//    @SuppressLint("MissingPermission")
//    private void requestNewLocationData() {
//        // Initializing LocationRequest object with appropriate methods
//        LocationRequest mLocationRequest = new LocationRequest();
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setInterval(5);
//        mLocationRequest.setFastestInterval(0);
//        mLocationRequest.setNumUpdates(1);
//        // setting LocationRequest on FusedLocationClient
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
//    }
//
//    private LocationCallback mLocationCallback = new LocationCallback() {
//
//        @Override
//        public void onLocationResult(LocationResult locationResult) {
//            Location mLastLocation = locationResult.getLastLocation();
//        }
//    };
//
//    // method to check for permissions
//    private boolean checkPermissions() {
//        return ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//
//        // If we want background location on Android 10.0 and higher, use:
////         ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
//    }
//
//    // method to request for permissions
//    private void requestPermissions() {
//        ActivityCompat.requestPermissions(this, new String[]{
//                Manifest.permission.ACCESS_COARSE_LOCATION,
//                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
//    }
//
//    // method to check if location is enabled
//    private boolean isLocationEnabled() {
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//    }
//
//    // If everything is alright then
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_ID) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getLastLocation();
//            }
//        }
//    }
//    private boolean isMyServiceRunning(Class<?> serviceClass) {
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                Log.i ("Service status", "Running");
//                return true;
//            }
//        }
//        Log.i ("Service status", "Not running");
//        return false;
//    }


}