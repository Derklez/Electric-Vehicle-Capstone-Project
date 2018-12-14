package edu.uwm.team10.electricvehicleapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
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

import models.VehicleModel;

public class NewVehicle extends Activity {

    private static final String TAG = "VehicleActivity";
    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("vehicles");
    List<VehicleModel> vehicleList;
    EditText vehicleName;
    EditText vehicleDescription;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_vehicle);
        Log.i(TAG, "Created this thing");

        vehicleList = new ArrayList<>();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * 0.7), (int)(height * 0.7));

        vehicleName = findViewById(R.id.vehicleName);
        vehicleDescription = findViewById(R.id.vehicleDescription);
        submitBtn = findViewById(R.id.submitVehicle);
        submitBtn.setEnabled(false); // Button is disabled by default to wait for user input

        setTextListeners();

        submitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = vehicleName.getText().toString();
                String description = vehicleDescription.getText().toString();
                VehicleModel vehicleModel = new VehicleModel(getFreshId(), name, description);
                String pushString = mDatabase.push().getKey();
                //mDatabase.child("vehicles").child(pushString).setValue(vehicleModel);
                mDatabase.child(pushString).setValue(vehicleModel);
                Toast t = Toast.makeText(getApplicationContext(),
                        "New vehicle successfully created", Toast.LENGTH_SHORT);
                t.show();
                closeActivity();
            }
        });
    }

    /**
     * Called on activity start. Generates a list of vehicles based on Firebase data.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot vehicleSnapshot : dataSnapshot.getChildren()) {
                    VehicleModel vehicle = vehicleSnapshot.getValue(VehicleModel.class);
                    vehicleList.add(vehicle);
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
     */
    private void enableSubmitButton() {
        int nameLength = vehicleName.getText().toString().length();
        int descriptionLength = vehicleDescription.getText().toString().length();
        if (nameLength > 0 && descriptionLength > 0) {
            submitBtn.setEnabled(true);
        } else {
            submitBtn.setEnabled(false);
        }
    }

    /**
     * Figures out the max id of current vehicles and returns 1 higher. All ids are ints
     * @return highest id + 1
     */
    private long getFreshId() {
        long id = 0;
        for (VehicleModel vehicle : vehicleList) {
            long vehicleId = vehicle.getId();
            if (vehicleId >= id) {
                id = vehicleId + 1;
            }
        }
        return id;
    }

    /**
     * Called from onCreate to generate callbacks for each text field. Any time an edittext field
     * is changed, call the enabledSubmitButton function.
     */
    private void setTextListeners() {
        vehicleName.addTextChangedListener(new TextWatcher() {
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
        vehicleDescription.addTextChangedListener(new TextWatcher() {
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
