package edu.uwm.team10.electricvehicleapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
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
    EditText batteryName;
    EditText batteryDescription;
    EditText maxVoltage;
    EditText cutoff;
    EditText capacity;
    Button submit;

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

        batteryName = findViewById(R.id.batteryName);
        batteryDescription = findViewById(R.id.batteryDescription);
        maxVoltage = findViewById(R.id.maxVoltage);
        cutoff = findViewById(R.id.cutoffVoltage);
        capacity = findViewById(R.id.capacity);

        setTextListeners();

        submit = findViewById(R.id.submitBattery);
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = batteryName.getText().toString();
                String description = batteryDescription.getText().toString();
                Double batteryMax = Double.parseDouble(maxVoltage.getText().toString());
                Double cutoffVoltage = Double.parseDouble(cutoff.getText().toString());
                Double batteryCapacity = Double.parseDouble(capacity.getText().toString());
                BatteryModel batteryModel = new BatteryModel(getFreshId(), name, description,
                        batteryMax, cutoffVoltage, batteryCapacity);
                String pushString = mDatabase.push().getKey();
                mDatabase.child(pushString).setValue(batteryModel);
                closeActivity();
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

    private void closeActivity() {
        this.finish();
    }

    private void enableSubmitButton() {
        int nameLength = batteryName.getText().toString().length();
        int descriptionLength = batteryDescription.getText().toString().length();
        int maxVoltageLength = maxVoltage.getText().toString().length();
        int cutoffLength = cutoff.getText().toString().length();
        int capacityLength = capacity.getText().toString().length();
        if (nameLength > 0 && descriptionLength > 0 && maxVoltageLength > 0
                && cutoffLength > 0 && capacityLength > 0) {
            submit.setEnabled(true);
        } else {
            submit.setEnabled(false);
        }
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

    private void setTextListeners() {
        batteryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                enableSubmitButton();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        batteryDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                enableSubmitButton();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        maxVoltage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                enableSubmitButton();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        cutoff.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                enableSubmitButton();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        capacity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                enableSubmitButton();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
