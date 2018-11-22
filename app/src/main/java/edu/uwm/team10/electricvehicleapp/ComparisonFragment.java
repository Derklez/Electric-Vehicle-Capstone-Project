package edu.uwm.team10.electricvehicleapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.BatteryModel;
import models.TripModel;
import models.VehicleModel;

public class ComparisonFragment extends Fragment {

    private Button selectTrips;
    private View view;
    ArrayAdapter tripAdapter;
    TripModel trip1Select;
    TripModel trip2Select;
    ArrayList<VehicleModel> vehicleList;
    ArrayList<BatteryModel> batteryList;

    TextView trip1Text;
    TextView trip2Text;

    TextView vehicleUsedTrip1;
    TextView vehicleUsedTrip2;
    TextView batteryUsedTrip1;
    TextView batteryUsedTrip2;
    TextView durationTrip1;
    TextView durationTrip2;
    TextView distanceTrip1;
    TextView distanceTrip2;
    TextView startingVoltageTrip1;
    TextView startingVoltageTrip2;
    TextView endingVoltageTrip1;
    TextView endingVoltageTrip2;
    TextView voltsPerKmTrip1;
    TextView voltsPerKmTrip2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_comparison, container, false);
        selectTrips = view.findViewById(R.id.selectTrips);
        DatabaseReference vehicleRef = FirebaseDatabase.getInstance().getReference("vehicles");
        DatabaseReference batteryRef = FirebaseDatabase.getInstance().getReference("batteries");
        vehicleList = new ArrayList<>();
        batteryList = new ArrayList<>();
        vehicleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    vehicleList.add(snapshot.getValue(VehicleModel.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        batteryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    batteryList.add(snapshot.getValue(BatteryModel.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        selectTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSelectTripDialog();
            }
        });

        trip1Text = view.findViewById(R.id.trip1Text);
        trip2Text = view.findViewById(R.id.trip2Text);
        vehicleUsedTrip1 = view.findViewById(R.id.vehicleUsedTrip1);
        vehicleUsedTrip2 = view.findViewById(R.id.vehicleUsedTrip2);
        batteryUsedTrip1 = view.findViewById(R.id.batteryUsedTrip1);
        batteryUsedTrip2 = view.findViewById(R.id.batteryUsedTrip2);
        durationTrip1 = view.findViewById(R.id.durationTrip1);
        durationTrip2 = view.findViewById(R.id.durationTrip2);
        distanceTrip1 = view.findViewById(R.id.distanceTrip1);
        distanceTrip2 = view.findViewById(R.id.distanceTrip2);
        startingVoltageTrip1 = view.findViewById(R.id.startingVoltageTrip1);
        startingVoltageTrip2 = view.findViewById(R.id.startingVoltageTrip2);
        endingVoltageTrip1 = view.findViewById(R.id.endingVoltageTrip1);
        endingVoltageTrip2 = view.findViewById(R.id.endingVoltageTrip2);
        voltsPerKmTrip1 = view.findViewById(R.id.voltsPerKmTrip1);
        voltsPerKmTrip2 = view.findViewById(R.id.voltsPerKmTrip2);

        return view;
    }

    private void openSelectTripDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        View mView = getLayoutInflater().inflate(R.layout.compare_trip_dialog, null);

        final Spinner trip1 = mView.findViewById(R.id.trip1);
        final Spinner trip2 = mView.findViewById(R.id.trip2);

        //final List<Map<String, TripModel>> tripList = new ArrayList<>();
        final List<String> tripList = new ArrayList<>();
        final Map<String, TripModel> tripMap = new HashMap<>();

        DatabaseReference tripRef = FirebaseDatabase.getInstance().getReference("trips");

        tripRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 1;
                String lastDateString = ""; // Tracks if there were multiple trips in a day
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TripModel trip = snapshot.getValue(TripModel.class);
                    if (trip.getTripDate().getTripString().equals(lastDateString)) {
                        ++i;
                    } else {
                        i = 1;
                    }
                    String tripDateString = trip.getTripDate().getTripString() + " (trip " + i + ")";
                    lastDateString = trip.getTripDate().getTripString();
                    tripMap.put(tripDateString, trip);
                    tripList.add(tripDateString);
                }
                tripAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        tripAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, tripList);
        tripAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        trip1.setAdapter(tripAdapter);
        trip2.setAdapter(tripAdapter);

        dialog.setPositiveButton("Compare Trips", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runComparison();
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

        trip1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()  {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trip1Select = tripMap.get(tripList.get(position));
                if (trip1Select != null && trip2Select != null) {
                    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) { return; }
        });
        trip2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trip2Select = tripMap.get(tripList.get(position));
                if (trip1Select != null && trip2Select != null) {
                    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { return; }
        });
    }

    private void runComparison() {
        TripModel trip1 = trip1Select;
        TripModel trip2 = trip2Select;

        for (VehicleModel vehicle : vehicleList) {
            if (vehicle.getId() == trip1.getCarId()) {
                vehicleUsedTrip1.setText(vehicle.getVehicleName());
            }
            if (vehicle.getId() == trip2.getCarId()) {
                vehicleUsedTrip2.setText(vehicle.getVehicleName());
            }
        }
        for (BatteryModel battery : batteryList) {
            if (battery.getId() == trip1.getBatteryId()) {
                batteryUsedTrip1.setText(battery.getBatteryName());
            }
            if (battery.getId() == trip2.getBatteryId()) {
                batteryUsedTrip2.setText(battery.getBatteryName());
            }
        }
        trip1Text.setText(trip1.getTripDate().getTripString());
        trip2Text.setText(trip2.getTripDate().getTripString());
        Double trip1Dur = (double) Math.round(trip1.getElapsedTime() * 100) / 100; // Rounds seconds to 2 decimals
        Double trip2Dur = (double) Math.round(trip2.getElapsedTime() * 100) / 100;
        durationTrip1.setText(Double.toString(trip1Dur) + " seconds");
        durationTrip2.setText(Double.toString(trip2Dur) + " seconds");
        Double trip1Dist = (double) Math.round((trip1.getDistanceTraveled() / 1000.0) * 100) / 100; // Converts to m to km, then rounds to 2 decimals
        Double trip2Dist = (double) Math.round((trip2.getDistanceTraveled() / 1000.0) * 100) / 100;
        distanceTrip1.setText(Double.toString(trip1Dist) + " km");
        distanceTrip2.setText(Double.toString(trip2Dist) + " km");
        startingVoltageTrip1.setText(Double.toString(trip1.getStartVolts()) + " volts");
        startingVoltageTrip2.setText(Double.toString(trip2.getStartVolts()) + " volts");
        endingVoltageTrip1.setText(Double.toString(trip1.getEndVolts()) + " volts");
        endingVoltageTrip2.setText(Double.toString(trip2.getEndVolts()) + " volts");

        if (trip1.getDistanceTraveled() != 0.0) {
            Double trip1VoltDiff = trip1.getStartVolts() - trip1.getEndVolts();
            Double trip1Vpkm = (double) Math.round((trip1VoltDiff / (trip1.getDistanceTraveled() / 1000.0) * 100) / 100);
            voltsPerKmTrip1.setText(Double.toString(trip1Vpkm) + " volts/km");
        } else {
            voltsPerKmTrip1.setText("--- volts/km");
        }

        if (trip2.getDistanceTraveled() != 0.0) {
            Double trip2VoltDiff = trip2.getStartVolts() - trip2.getEndVolts();
            Double trip2Vpkm = (double) Math.round((trip2VoltDiff / (trip2.getDistanceTraveled() / 1000.0) * 100) / 100);
            voltsPerKmTrip2.setText(Double.toString(trip2Vpkm) + " volts/km");
        } else {
            voltsPerKmTrip2.setText("--- volts/km");
        }
    }
}
