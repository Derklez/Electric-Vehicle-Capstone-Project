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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import models.BatteryModel;

public class NewBattery extends Activity {
    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("batteries");
    List<BatteryModel> batteryList;
    EditText batteryName;
    EditText batteryDescription;
    EditText maxVoltage;
    EditText cutoff;
    EditText capacity;
    Button submitBtn;

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

        submitBtn = findViewById(R.id.submitBattery);
        submitBtn.setEnabled(false);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = batteryName.getText().toString();
                String description = batteryDescription.getText().toString();
                Double batteryMax = Double.parseDouble(maxVoltage.getText().toString());
                Double cutoffVoltage = Double.parseDouble(cutoff.getText().toString());
                Double batteryCapacity = Double.parseDouble(capacity.getText().toString());
                BatteryModel batteryModel = new BatteryModel(getFreshId(), name, description,
                        batteryMax, cutoffVoltage, batteryCapacity);
                String pushString = mDatabase.push().getKey();
                //mDatabase.child("batteries").child(pushString).setValue(batteryModel);
                mDatabase.child(pushString).setValue(batteryModel);
                closeActivity();
                Toast t = Toast.makeText(getApplicationContext(),
                        "New battery successfully created", Toast.LENGTH_SHORT);
                t.show();
            }
        });
    }

    /**
     * Called on activity start. Generates a list of batteries based on Firebase data.
     */
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

    /**
     * Self explanatory.
     */
    private void closeActivity() {
        this.finish();
    }

    /**
     * Checks that each field is at least partially filled out.
     * TODO: add more validation for a battery object.
     */
    private void enableSubmitButton() {
        int nameLength = batteryName.getText().toString().length();
        int descriptionLength = batteryDescription.getText().toString().length();
        int maxVoltageLength = maxVoltage.getText().toString().length();
        int cutoffLength = cutoff.getText().toString().length();
        int capacityLength = capacity.getText().toString().length();
        if (nameLength > 0 && descriptionLength > 0 && maxVoltageLength > 0
                && cutoffLength > 0 && capacityLength > 0) {
            submitBtn.setEnabled(true);
        } else {
            submitBtn.setEnabled(false);
        }
    }

    /**
     * Figures out the max id of current batteries and returns 1 higher. All ids are ints
     * @return highest id + 1
     */
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

    /**
     * Called from onCreate to generate callbacks for each text field. Any time an edittext field
     * is changed, call the enabledSubmitButton function.
     */
    private void setTextListeners() {
        batteryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}

        });
        maxVoltage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}

        });
        cutoff.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}

        });
        capacity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}

        });
    }
}
