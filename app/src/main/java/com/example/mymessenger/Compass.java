package com.example.mymessenger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Compass#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Compass extends Fragment implements SensorEventListener, LocationListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Context context;
    private ImageView compassImage;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor mRotationV;
    private boolean isRotationSet = false;
    private boolean isAccelerometerSet = false;
    private boolean isMagnetometerSet = false;
    private float[] rMat = new float[9];
    private float[] orientation = new float[3];
    private float[] lastAccelerometer = new float[3];
    private float[] lastMagnetometer = new float[3];
    private boolean lastRotationSet = false;
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;
    private float currentDegree = 0f;
    static double latitude;
    static double longitude;
    private double dlatitude;
    private double dlongitude;
    LocationManager locationManager;
    LocationListener locationListener;
    ImageView image;
    FrameLayout arrow;
    TextView coords;
    private int mAzimuth;
    private int tAzimuth;
    final float GEOKM = 86;

    public Compass() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Compass.
     */
    // TODO: Rename and change types and number of parameters
    public static Compass newInstance(String param1, String param2) {
        Compass fragment = new Compass();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compass, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        compassImage = view.findViewById(R.id.compassImage);
        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        coords = view.findViewById(R.id.coords);
        arrow = view.findViewById(R.id.arrow_box);
        String dest = getArguments().getString("DEST");
        Log.d("DEST", dest);
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(dest.split("\n")));
        dlatitude = Double.parseDouble(arrayList.get(0));
        dlongitude = Double.parseDouble(arrayList.get(1));
        Log.d("DEST2", dlatitude + " " + dlongitude);

        locationManager = (LocationManager)
                getContext().getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();
        // getLastKnownLocation so that user don't need to wait
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 123);
            return;
        }
        Log.d("COMPAS", "manager set");
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000, 10, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_ORIENTATION);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, magnetometer);
        sensorManager.unregisterListener(this, accelerometer);
        //sensorManager.unregisterListener(this, mRotationV);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rMat, event.values);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;
        }
        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(rMat, null, lastAccelerometer, lastMagnetometer);
            SensorManager.getOrientation(rMat, orientation);
            mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
        }
        // mAzimuth = Math.round(mAzimuth);
        compassImage.setRotation(-mAzimuth);
        arrow.setRotation(-mAzimuth +tAzimuth);

        //coords.setText(mAzimuth + "");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
            if ((sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                noSensorsAlert();
            }
            else {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                isAccelerometerSet = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
                isMagnetometerSet = sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        }
        else{
            mRotationV = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            isRotationSet = sensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void noSensorsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setMessage("Your device doesn't support the Compass.")
                .setCancelable(false)
                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       // finish();
                    }
                });
        alertDialog.show();
    }

    public void stop() {
        if(isAccelerometerSet && isMagnetometerSet){
            sensorManager.unregisterListener(this,accelerometer);
            sensorManager.unregisterListener(this,magnetometer);
        }
        else{
            if(isRotationSet)
                sensorManager.unregisterListener(this,mRotationV);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        Log.d("NEWLOC", "LAT: " + latitude + "\nLON: " + longitude);
        double dla = dlatitude - latitude;
        double dlo = dlongitude - longitude;
        double dSla = dla * 92;
        double dSlo = dlo * 92;
        double dS = Math.sqrt(dSla * dSla + dSlo * dSlo);
        if (dla > 0 && dlo > 0) {
            tAzimuth = (int)(Math.atan(Math.abs(dlo/dla)) * 180 / Math.PI);
        } else if (dla < 0 && dlo > 0) {
            tAzimuth = (int)(Math.atan(Math.abs(dla/dlo)) * 180 / Math.PI) + 90;
        } else if (dla < 0 && dlo < 0) {
            tAzimuth = (int)(Math.atan(Math.abs(dlo/dla)) * 180 / Math.PI) + 180;
        } else if (dla > 0 && dlo < 0) {
            tAzimuth = (int)(Math.atan(Math.abs(dla/dlo)) * 180 / Math.PI) + 270;
        }
        coords.setText(String.format("%.3f Км", dS));
        Log.d("locChange", "dlo: " + dlo + "| dla: " + dla + "\ntAzimuth: " + tAzimuth);
    }
    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}