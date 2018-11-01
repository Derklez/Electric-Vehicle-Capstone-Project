package edu.uwm.team10.electricvehicleapp;

import android.Manifest;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    private double currentSpeed;
    private ArrayList<Double> speedMeasurements;
    private ArrayList<ArrayList<Double>> accelMeasurements;
    private double startVolts;
    private double endVolts;
    private double distanceTraveled; // In meters


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
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); // Linear Acceleration removes gravity
        sensorManager.registerListener(this, accelerometer,
                500000, 500000); // Call every half second (500k microseconds)

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
            case R.id.nav_maintenance:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MaintenanceFragment()).commit();
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
            createStartDialog();
        }
    }

    private void createStartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please provide battery's starting voltage");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("Start Trip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setStartVolts(Double.parseDouble(input.getText().toString()));
                startTripHelper();
            }
        });
        builder.setNegativeButton("Cancel Trip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void startTripHelper() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).setStartButtonText("End Trip");
        }
        tripStartTime = System.nanoTime();
        setTripActive(true);
        setDistanceTraveled(0.0);
        speedMeasurements = new ArrayList<>(); // When starting a new trip, wipe old measurements
        accelMeasurements = new ArrayList<>();
    }

    public void endTrip() {
        if (getTripActive()) {
            createEndDialog();
        }
    }

    private void createEndDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please provide battery's ending voltage");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        builder.setPositiveButton("End Trip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setEndVolts(Double.parseDouble(input.getText().toString()));
                endTripHelper();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void endTripHelper() {
        tripEndTime = System.nanoTime();
        setTripActive(false);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).setStartButtonText("Start Trip");
        }
        double elapsedTime = (getTripEndTime()
                - getTripStartTime()) / (1e+9);
        String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        TripDateModel tripDateModel = new TripDateModel(getTripStartTime(),getTripEndTime(),
                date);
        TripModel tripModel = new TripModel(calculateAverageSpeed(), 1, getDistanceTraveled(),
                elapsedTime, getEndVolts(), getStartVolts(), tripDateModel, getSpeedMeasurements(),
                getAccelMeasurements());
        String pushString = mDatabase.child("trips").push().getKey();
        mDatabase.child("trips").child(pushString).setValue(tripModel);
    }

    /*
    Accelerometer callback
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isTripActive) {
            ArrayList<Double> measurements = new ArrayList<>();
            measurements.add((double) event.values[0]);
            measurements.add((double) event.values[1]);
            measurements.add((double) event.values[2]);
            addAccelMeasurement(measurements);
        }

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
                if (isTripActive) {
                    updateSpeedData(nCurrentSpeed);
                }
            }
        }
    }

    private void updateSpeedData(float currentSpeed)  {
        addSpeedMeasurement(currentSpeed);
        double addedDistance = currentSpeed * 1.0; // distance = speed * time
        addDistance(addedDistance);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).setDistanceTraveledText("Meters: " + getDistanceTraveled());
        }
    }

    private double calculateAverageSpeed() {
        ArrayList<Double> speedMeasurements = getSpeedMeasurements();
        int i;
        double averageSpeed = 0.0;
        for (i=0; i < speedMeasurements.size(); ++i) {
            averageSpeed += speedMeasurements.get(i);
        }
        return (averageSpeed / speedMeasurements.size());
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

    private void addDistance(double distance) { distanceTraveled += distance; }





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

    public double getDistanceTraveled() { return distanceTraveled; }

    public void setDistanceTraveled(double distance) { distanceTraveled = distance; }

    public ArrayList<ArrayList<Double>> getAccelMeasurements() {
        return accelMeasurements;
    }

    public void addAccelMeasurement(ArrayList<Double> measurement) {
        if (measurement != null) {
            accelMeasurements.add(measurement);
        }
    }

}
