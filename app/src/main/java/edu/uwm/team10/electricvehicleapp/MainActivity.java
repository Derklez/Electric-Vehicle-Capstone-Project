package edu.uwm.team10.electricvehicleapp;

import android.Manifest;
import android.content.DialogInterface;
import android.os.SystemClock;
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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.BatteryModel;
import models.TripDateModel;
import models.TripModel;
import models.VehicleModel;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener, LocationListener {

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;
    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    final Context context = this;

    private SensorManager sensorManager;
    private Sensor accelerometer;

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
    private Chronometer homeChronometer;

    private String selectedVehicleName;
    private String selectedBatteryName;
    private int selectedVehicleId;
    private int selectedBatteryId;

    ArrayAdapter vehicleAdapter;
    ArrayAdapter batteryAdapter;


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
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); // Linear Acceleration removes gravity
        sensorManager.registerListener(this, accelerometer,
                500000, 500000); // Call every half second (500k microseconds)

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        homeChronometer = new Chronometer(context);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

    public void startTrip() {
        if (!getTripActive()) {
            createStartDialog();
        }
    }

    private void createStartDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_start_trip, null);

        final Spinner vehicleSpinner = mView.findViewById(R.id.vehicleSpinner);
        final Spinner batterySpinner = mView.findViewById(R.id.batterySpinner);
        final EditText startVoltage = mView.findViewById(R.id.startingVoltage);

        final List<String> vehicleList = new ArrayList<>();
        //vehicleList.add("Vehicles");
        final List<String> batteryList = new ArrayList<>();
        //batteryList.add("Batteries");

        DatabaseReference vehicleRef = FirebaseDatabase.getInstance().getReference("vehicles");
        DatabaseReference batteryRef = FirebaseDatabase.getInstance().getReference("batteries");

        vehicleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    VehicleModel vehicle = snapshot.getValue(VehicleModel.class);
                    vehicleList.add(vehicle.getVehicleName());
                }
                vehicleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        batteryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BatteryModel battery = snapshot.getValue(BatteryModel.class);
                    batteryList.add(battery.getBatteryName());
                }
                batteryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        vehicleAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, vehicleList);
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleSpinner.setAdapter(vehicleAdapter);
        batteryAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, batteryList);
        batteryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        batterySpinner.setAdapter(batteryAdapter);

        dialog.setPositiveButton("Start Trip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedVehicleName.equals("Vehicles") || selectedBatteryName.equals("Batteries")) {
                    Toast.makeText(context, "Please select both a vehicle and battery", Toast.LENGTH_LONG);
                } else {
                    selectedVehicleName = vehicleSpinner.getSelectedItem().toString();
                    selectedBatteryName = batterySpinner.getSelectedItem().toString();
                    calculateIds();
                    startTripHelper();
                }
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setView(mView);
        final AlertDialog mDialog = dialog.create();
        mDialog.show();
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        vehicleSpinner.setOnItemSelectedListener(new OnItemSelectedListener()  {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedVehicleName = vehicleList.get(position);
                //Log.i(TAG, "Voltage: " + startVoltage.getText() + "  Vehicle: " + selectedVehicleName + "  Battery: " + selectedBatteryName);
                if (selectedVehicleName != null && selectedBatteryName != null) {
                    if (!selectedVehicleName.equals("Vehicles") && !selectedBatteryName.equals("Batteries")) {
                        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parent) { return; }
        });
        batterySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBatteryName = batteryList.get(position);
                if (selectedVehicleName != null && selectedBatteryName != null) {
                    if (!selectedVehicleName.equals("Vehicles") && !selectedBatteryName.equals("Batteries")) {
                        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parent) { }
        });

        startVoltage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG, "Voltage: " + startVoltage.getText() + "  Vehicle: " + selectedVehicleName + "  Battery: " + selectedBatteryName);
                if (selectedVehicleName != null && selectedBatteryName != null) {
                    if (startVoltage.getText().length() > 0 && !selectedVehicleName.equals("Vehicles") && !selectedBatteryName.equals("Batteries")) {
                        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                }
            }
        });
    }

    private void calculateIds() {
        DatabaseReference vehicleRef = mDatabase.child("vehicles");
        ValueEventListener vehicleEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String vehicleName = snapshot.child("vehicleName").getValue(String.class);
                    if (vehicleName.equals(selectedVehicleName)) {
                        selectedVehicleId = snapshot.child("id").getValue(Integer.class);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        vehicleRef.addListenerForSingleValueEvent(vehicleEventListener);

        DatabaseReference batteryRef = mDatabase.child("batteries");
        ValueEventListener batteryEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String batteryName = snapshot.child("batteryName").getValue(String.class);
                    if (batteryName.equals(selectedBatteryName)) {
                        selectedBatteryId = snapshot.child("id").getValue(Integer.class);
                        Log.i(TAG, "Battery id: " + selectedBatteryId);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        batteryRef.addListenerForSingleValueEvent(batteryEventListener);
    }

    private void startTripHelper() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        tripStartTime = System.nanoTime();
        setTripActive(true);
        setDistanceTraveled(0.0);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).setStartButtonText("End Trip");
            homeChronometer = ((HomeFragment) fragment).getChronometer();
        }
        toggleChronometer();
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
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
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
        toggleChronometer();
        double elapsedTime = (getTripEndTime()
                - getTripStartTime()) / (1e+9);
        String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        TripDateModel tripDateModel = new TripDateModel(getTripStartTime(),getTripEndTime(),
                date);
        TripModel tripModel = new TripModel(calculateAverageSpeed(), selectedVehicleId,
                selectedBatteryId, getDistanceTraveled(),
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
            ((HomeFragment) fragment).setDistanceTraveledText("" + getDistanceTraveled() + " m");
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

    public void toggleChronometer() {
        if (getTripActive()) {
            homeChronometer.setBase(SystemClock.elapsedRealtime());
            homeChronometer.start();
        } else {
            homeChronometer.stop();
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
    //determines how many "bumps" we go over using accelerometer data.
    //takes a percent, if a 10% difference in vector is a bump, use .10 as input.
    //will need to change from all vectors to just the horizontal vector for bumpy roads
    //can use similar idea for changing direction frequently? Perhaps even frequent speed up and slow down horizontally
    public void bumpyRoad(double percent){


        double percentPlus = 1 + percent;
        double percentMinus = 1 - percent;

        double[] absolute = new double[accelMeasurements.size()];//array used to store absolute vectors

        for(int i = 0; i < accelMeasurements.size(); ++i){
            double x = accelMeasurements.get(i).get(0);
            double y = accelMeasurements.get(i).get(1);
            double z = accelMeasurements.get(i).get(2);

            double a;//absolute value of all vectors, xyz

            x = x*x;
            y = y*y;
            z = z*z;

            a = x+y+z;
            a = Math.sqrt(a);
            a = a - 9.8;
            absolute[i] = a;
        }

        double avg = 0.0;
        for (int i = 0; i < absolute.length; ++i){
            avg += absolute[i];
        }
        avg = avg / absolute.length;

        for(int i = 0; i < absolute.length; ++i){
            //check if each absolute accelerometer vector is 10% less or 10% greater than average vector
            if(absolute[i] < percentMinus){//if absolute vector at instance i is at least 10% less than avg, bump

            }
            if(absolute[i] > percentPlus){//if absolute vector at instance i is at least 10% greater than avg, bump

            }
        }
    }

}
