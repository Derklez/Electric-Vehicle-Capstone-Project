package edu.uwm.team10.electricvehicleapp;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Handler;
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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private ArrayList<Double> recentAccelMeasurements;
    private double recentSpeedMeasurement;
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


    /**
     * Entry point of the application. This method primarily sets up all the hardware so it can take
     * readings and sets up a few other things like the drawer layout so the user can actually
     * interact with the application. The content view is activity_main.xml, but this is purely
     * a container for other views. On application load, the home fragment is the first "real" thing
     * displayed to the user.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Need permission to access user's location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); // Linear Acceleration removes gravity
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

        new Handler().postDelayed(new Runnable() { // Add new accel measurements every second
            @Override
            public void run() {
                addAccelMeasurement();
                new Handler().postDelayed(this, 1000);
            }
        }, 500);

        new Handler().postDelayed(new Runnable() { // Add new speed measurement every second
            @Override
            public void run() {
                updateSpeedData();
                new Handler().postDelayed(this, 1000);
            }
        }, 500);

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

    /**
     * Called when an item in the hamburger menu is selected. This method loads up the associated
     * fragment choosen by the user.
     * @param menuItem the menu item that was clicked
     * @return always return true
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                break;
            case R.id.nav_voltage:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ComparisonFragment()).commit();
                break;
            case R.id.nav_speed:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SpeedFragment()).commit();
                break;
            case R.id.nav_maintenance:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MaintenanceFragment()).commit();
                break;
            case R.id.nav_bluetooth:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BlueToothFragment()).commit();
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Opens hamburger menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Closes hamburger menu
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        super.onBackPressed();
    }

    /**
     * Simple public helper method
     */
    public void startTrip() {
        if (!getTripActive()) {
            createStartDialog();
        }
    }

    /**
     * This method builds the dialog that pops up when a user starts a trip. The layout is defined
     * in dialog_start_trip.xml. Each component has appropriate listeners attached and the spinners
     * are loaded with the vehicles and batteries from Firebase.
     */
    private void createStartDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_start_trip, null);

        final Spinner vehicleSpinner = mView.findViewById(R.id.vehicleSpinner);
        final Spinner batterySpinner = mView.findViewById(R.id.batterySpinner);
        final EditText startVoltage = mView.findViewById(R.id.startingVoltage);

        final List<String> vehicleList = new ArrayList<>();
        final List<String> batteryList = new ArrayList<>();

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
                setStartVolts(Double.parseDouble(startVoltage.getText().toString()));
                selectedVehicleName = vehicleSpinner.getSelectedItem().toString();
                selectedBatteryName = batterySpinner.getSelectedItem().toString();
                calculateIds();
                startTripHelper();
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

        vehicleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedVehicleName = vehicleList.get(position);
                if (selectedVehicleName != null && selectedBatteryName != null) {
                    if (startVoltage.getText().length() > 0 && !selectedVehicleName.equals("Vehicles") && !selectedBatteryName.equals("Batteries")) {
                        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    } else {
                        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
        batterySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBatteryName = batteryList.get(position);
                if (selectedVehicleName != null && selectedBatteryName != null) {
                    if (startVoltage.getText().length() > 0 && !selectedVehicleName.equals("Vehicles") && !selectedBatteryName.equals("Batteries")) {
                        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    } else {
                        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        startVoltage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (selectedVehicleName != null && selectedBatteryName != null) {
                    if (startVoltage.getText().length() > 0 && !selectedVehicleName.equals("Vehicles") && !selectedBatteryName.equals("Batteries")) {
                        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    } else {
                        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            }
        });
    }

    /**
     * Called when user officially starts trip from dialog confirmation. Based on what vehicle &
     * battery the user selected from the spinners in the dialog, this method will go through all
     * the Firebase entries and figure out the corresponding ID so the trip can be stored properly.
     * This method exists because we couldn't figure out how to make a map of key/values for the
     * spinners.
     */
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
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
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        batteryRef.addListenerForSingleValueEvent(batteryEventListener);
    }

    /**
     * Called when user officially starts a trip from the dialog. This method resets all appropriate
     * fields so a new trip is properly tracked.
     */
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

    /**
     * Simple helper method for when the end trip button is pressed.
     */
    public void endTrip() {
        if (getTripActive()) {
            createEndDialog();
        }
    }

    /**
     * Generates a dialog for the user to officially end a trip. Unlike the createStartDialog()
     * method, this method has no corresponding xml layout file. This is mainly due to that there
     * is only a single text field that needs to be entered.
     */
    private void createEndDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please provide battery's ending voltage");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
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

    /**
     * Triggered when user presses confirmation button in the end trip dialog. This method both
     * saves the trip to Firebase and freezes all appropriate readings as to not gather useless
     * data.
     */
    private void endTripHelper() {
        tripEndTime = System.nanoTime();
        setTripActive(false);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof HomeFragment) {
            Chronometer chron = ((HomeFragment) fragment).getChronometer();
            chron.stop();
            ((HomeFragment) fragment).setStartButtonText("Start Trip");
        }
        toggleChronometer();
        double elapsedTime = (getTripEndTime()
                - getTripStartTime()) / (1e+9);
        String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        TripDateModel tripDateModel = new TripDateModel(getTripStartTime(), getTripEndTime(),
                date);
        TripModel tripModel = new TripModel(calculateAverageSpeed(), selectedVehicleId,
                selectedBatteryId, getDistanceTraveled(),
                elapsedTime, getEndVolts(), getStartVolts(), tripDateModel, getSpeedMeasurements(),
                getAccelMeasurements());
        String pushString = mDatabase.child("trips").push().getKey();
        mDatabase.child("trips").child(pushString).setValue(tripModel);
    }

    /**
     * Accelerometer callback. First we reset recentAccelMeasurements and add the new update. This is
     *     called every time there is an accelerometer update (which is a lot), so we set a Handler in the
     *     onCreate method to only add data to our "true" accelerometer data every half second.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (isTripActive) {
                recentAccelMeasurements = new ArrayList<>();
                recentAccelMeasurements.add((double) event.values[0]);
                recentAccelMeasurements.add((double) event.values[1]);
                recentAccelMeasurements.add((double) event.values[2]);
            }
        }
    }

    /**
     * Required for accelerometer
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                500000);
    }

    /**
     * Callback for location service so we can get the user's current speed. This updates the UI
     * with the current speed and saves the speed measurement. Should be called every ~1 second.
     * @param location most recent location reading which contains speed and other information
     */
    @Override
    public void onLocationChanged(Location location) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (location != null) {
            recentSpeedMeasurement = location.getSpeed();
        } else {
            recentSpeedMeasurement = 0.0;
        }
        if (fragment instanceof HomeFragment) {
            HomeFragment homeFragment = (HomeFragment) fragment;
            if (location == null) {
                homeFragment.setSpeedText("-.- m/s");
            } else {
                double roundedSpeed = round(recentSpeedMeasurement, 2);
                homeFragment.setSpeedText(roundedSpeed + " m/s");
            }
        }
    }

    /**
     * Based on the most recent speed measurement taken, this to the overall distance traveled
     *     assuming 1 second per speed reading. If the user is on the home fragment, this method also
     *     updates the text fields to reflect the most up-to-date information.
     */
    private void updateSpeedData() {
        if (isTripActive) {
            addSpeedMeasurement(recentSpeedMeasurement);
            double addedDistance = recentSpeedMeasurement * 1.0; // distance = speed * time
            addDistance(addedDistance);
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment instanceof HomeFragment) {
                double distanceRounded = round(getDistanceTraveled(), 2);
                ((HomeFragment) fragment).setDistanceTraveledText("" + distanceRounded + " m");
                double averageSpeedRounded = round(calculateAverageSpeed(), 2);
                ((HomeFragment) fragment).setAverageSpeedText("" + averageSpeedRounded + " m/s");
            }
        }
    }

    /**
     * Calculates average speed over the entire trip. This should likely be highly modified if not
     * entirely removed down the road due to efficiency concerns. It made our video look better.
     * @return average speed over the trip so far
     */
    private double calculateAverageSpeed() {
        ArrayList<Double> speedMeasurements = getSpeedMeasurements();
        int i;
        double averageSpeed = 0.0;
        if (speedMeasurements.size() != 0) {
            for (i = 0; i < speedMeasurements.size(); ++i) {
                averageSpeed += speedMeasurements.get(i);
            }
            return (averageSpeed / speedMeasurements.size());
        } else {
            return averageSpeed;
        }
    }

    /**
     * Rounds a double to a certain number of decimal points
     * @param value the number to be rounded
     * @param places how many decimal places to round to
     * @return the rounded number
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Starts or stops the chronometer from tracking time.
     */
    public void toggleChronometer() {
        if (getTripActive()) {
            homeChronometer.setBase(SystemClock.elapsedRealtime());
            homeChronometer.start();
        } else {
            homeChronometer.stop();
        }
    }

    /**
     * This starts the chronometer on the home fragment back up after a fragment change. Without this,
     *     if the user changes fragments, the chronometer would stick at 00:00.
     */
    public void setHomeFragmentChronometer() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof HomeFragment) {
            if (isTripActive) {
                Chronometer chron = ((HomeFragment) fragment).getChronometer();
                chron.setBase(homeChronometer.getBase());
                chron.start();
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

    private void addDistance(double distance) {
        distanceTraveled += distance;
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

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public ArrayList<Double> getSpeedMeasurements() {
        return speedMeasurements;
    }

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

    public double getDistanceTraveled() {
        return distanceTraveled;
    }

    public void setDistanceTraveled(double distance) {
        distanceTraveled = distance;
    }

    public ArrayList<ArrayList<Double>> getAccelMeasurements() {
        return accelMeasurements;
    }

    public void addAccelMeasurement() {
        if (recentAccelMeasurements != null) {
            accelMeasurements.add(recentAccelMeasurements);
        }
    }

    /**
     * determines how many "bumps" we go over using accelerometer data.
     * takes a percent, if a 10% difference in vector is a bump, use .10 as input.
     * will need to change from all vectors to just the horizontal vector for bumpy roads
     * can use similar idea for changing direction frequently? Perhaps even frequent speed up and slow down horizontally
     * @param percent difference required to be considered a bump
     */
    public void bumpyRoad(double percent) {


        double percentPlus = 1 + percent;
        double percentMinus = 1 - percent;

        double[] absolute = new double[accelMeasurements.size()];//array used to store absolute vectors

        for (int i = 0; i < accelMeasurements.size(); ++i) {
            double x = accelMeasurements.get(i).get(0);
            double y = accelMeasurements.get(i).get(1);
            double z = accelMeasurements.get(i).get(2);

            double a;//absolute value of all vectors, xyz

            x = x * x;
            y = y * y;
            z = z * z;

            a = x + y + z;
            a = Math.sqrt(a);
            a = a - 9.8;
            absolute[i] = a;
        }

        double avg = 0.0;
        for (int i = 0; i < absolute.length; ++i) {
            avg += absolute[i];
        }
        avg = avg / absolute.length;

        for (int i = 0; i < absolute.length; ++i) {
            //check if each absolute accelerometer vector is 10% less or 10% greater than average vector
            if (absolute[i] < percentMinus) {//if absolute vector at instance i is at least 10% less than avg, bump

            }
            if (absolute[i] > percentPlus) {//if absolute vector at instance i is at least 10% greater than avg, bump

            }
        }
    }

}
