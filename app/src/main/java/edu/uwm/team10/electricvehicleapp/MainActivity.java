package edu.uwm.team10.electricvehicleapp;

import android.Manifest;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import models.TripDateModel;
import models.TripModel;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener, LocationListener {

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;
    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView xAccel, yAccel, zAccel;
    private DrawerLayout mDrawerLayout;
    private Boolean isTripActive;
    private long tripStartTime;
    private long tripEndTime;
    private double averageSpeed;
    private double currentSpeed;
    private ArrayList<Double> speedMeasurements;
    private double startVolts;
    private double endVolts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Need permission to access user's location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer,
                500000); // Call every half second (500k microseconds)

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        xAccel = findViewById(R.id.xAccel);
        yAccel = findViewById(R.id.yAccel);
        zAccel = findViewById(R.id.zAccel);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        setTripActive(false);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        this.onLocationChanged(null);

        // Sets fragment to Home when opening. Rotating device and other odd movements will
        // reset the app to Home if there is not a null check
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_voltage:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new VoltageFragment()).commit();
                break;
            case R.id.nav_speed:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SpeedFragment()).commit();
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        super.onBackPressed();
    }

    public void onButtonTap(View v) {
        Toast myToast = Toast.makeText(getApplicationContext(), "Ouch!", Toast.LENGTH_LONG);
        myToast.show();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Testing 123");
    }

    public void startTrip() {
        if (!getTripActive()) {
            tripStartTime = System.nanoTime();
            setTripActive(true);
            speedMeasurements = new ArrayList<>(); // When starting a new trip, wipe old measurements
        }
    }

    public void endTrip() {
        if (getTripActive()) {
            tripEndTime = System.nanoTime();
            setTripActive(false);
            double elapsedTime = (getTripEndTime()
                    - getTripStartTime()) / (1e+9);
            TripDateModel tripDateModel = new TripDateModel(getTripStartTime(),getTripEndTime(),
                    "Wed, Oct 18 2018");
            TripModel tripModel = new TripModel(10.0, 1, 20,
                    elapsedTime, getEndVolts(), 1, getStartVolts(), tripDateModel, speedMeasurements);
            String pushString = mDatabase.child("trips").push().getKey();
            mDatabase.child("trips").child(pushString).setValue(tripModel);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof HomeFragment) {
            HomeFragment homeFragment = (HomeFragment) fragment;
            homeFragment.setXAccelText(" " + event.values[0]);
            homeFragment.setYAccelText(" " + event.values[1]);
            homeFragment.setZAccelText(" " + event.values[2]);
        }
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
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof HomeFragment) {
            HomeFragment homeFragment = (HomeFragment) fragment;
            if (location == null) {
                homeFragment.setSpeedText("-.- m/s");
            } else {
                float nCurrentSpeed = location.getSpeed();
                homeFragment.setSpeedText(nCurrentSpeed + " m/s");
                if (isTripActive) { addSpeedMeasurement(nCurrentSpeed); }
            }
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





    // GETTERS AND SETTERS
    public Boolean getTripActive() {
        return isTripActive;
    }

    public void setTripActive(Boolean tripActive) {
        isTripActive = tripActive;
    }

    public long getTripStartTime() {
        return tripStartTime;
    }

    public long getTripEndTime() {
        return tripEndTime;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getCurrentSpeed() { return currentSpeed; }

    public void setCurrentSpeed(double currentSpeed) { this.currentSpeed = currentSpeed; }

    public ArrayList<Double> getSpeedMeasurements() { return speedMeasurements; }

    public void addSpeedMeasurement(double measurement) {
        speedMeasurements.add(measurement);
    }

    public double getStartVolts() {
        return startVolts;
    }

    public void setStartVolts(double startVolts) {
        this.startVolts = startVolts;
    }

    public double getEndVolts() {
        return endVolts;
    }

    public void setEndVolts(double endVolts) {
        this.endVolts = endVolts;
    }
}
