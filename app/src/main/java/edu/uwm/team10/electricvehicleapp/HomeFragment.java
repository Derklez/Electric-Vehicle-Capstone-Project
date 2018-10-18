package edu.uwm.team10.electricvehicleapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeFragment extends Fragment implements SensorEventListener, LocationListener {

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView xAccel, yAccel, zAccel;

    private View view;

    private static final String TAG = "MainActivity";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Need permission to access user's location
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }
        view = inflater.inflate(R.layout.fragment_home, container, false);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        xAccel = view.findViewById(R.id.xAccel);
        yAccel = view.findViewById(R.id.yAccel);
        zAccel = view.findViewById(R.id.zAccel);

        LocationManager locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        this.onLocationChanged(null);
        return view;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xAccel.setText(" " + event.values[0]);
        yAccel.setText(" " + event.values[1]);
        zAccel.setText(" " + event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView currentSpeed = view.findViewById(R.id.currentSpeed);

        if (location == null) {
            currentSpeed.setText("-.- m/s");
        } else {
            float nCurrentSpeed = location.getSpeed();
            currentSpeed.setText(nCurrentSpeed + " m/s");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
