package edu.uwm.team10.electricvehicleapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import models.BatteryModel;

public class NewBattery extends Activity {
    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("batteries");
    List<BatteryModel> batteryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_battery);

        batteryList = new ArrayList<>();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * 0.7), (int)(height * 0.7));

        Button submit = findViewById(R.id.submitBattery);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText batteryName = findViewById(R.id.batteryName);
                String name = batteryName.getText().toString();
                EditText batteryDescription = findViewById(R.id.batteryDescription);
                String description = batteryDescription.getText().toString();
                EditText maxVoltage = findViewById(R.id.maxVoltage);
                Double batteryMax = Double.parseDouble(maxVoltage.getText().toString());
                EditText cutoff = findViewById(R.id.cutoffVoltage);
                Double cutoffVoltage = Double.parseDouble(cutoff.getText().toString());
                EditText capacity = findViewById(R.id.capacity);
                Double batterycapacity = Double.parseDouble(capacity.getText().toString());
                BatteryModel batteryModel = new BatteryModel(getFreshId(), name, description,
                        batteryMax, cutoffVoltage, batterycapacity);
                String pushString = mDatabase.push().getKey();
                mDatabase.child(pushString).setValue(batteryModel);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot batterySnapshot : dataSnapshot.getChildren()) {
                    BatteryModel battery = batterySnapshot.getValue(BatteryModel.class);
                    batteryList.add(battery);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private long getFreshId() {
        long id = 0;
        for (BatteryModel battery : batteryList) {
            long batteryId = battery.getId();
            if (batteryId >= id) {
                id = batteryId + 1;
            }
        }
        return id;
    }
}
