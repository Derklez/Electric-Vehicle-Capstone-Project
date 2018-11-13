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

import models.VehicleModel;

public class NewVehicle extends Activity {

    final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("vehicles");
    List<VehicleModel> vehicleList;
    EditText vehicleName;
    EditText vehicleDescription;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_vehicle);

        vehicleList = new ArrayList<>();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * 0.7), (int)(height * 0.7));

        vehicleName = findViewById(R.id.vehicleName);
        vehicleDescription = findViewById(R.id.vehicleDescription);
        submit = findViewById(R.id.submitVehicle);
        submit.setEnabled(false); // Button is disabled by default to wait for user input

        setTextListeners();

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = vehicleName.getText().toString();
                String description = vehicleDescription.getText().toString();
                VehicleModel vehicleModel = new VehicleModel(getFreshId(), name, description);
                String pushString = mDatabase.push().getKey();
                mDatabase.child(pushString).setValue(vehicleModel);
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

    private void closeActivity() {
        this.finish();
    }

    private void enableSubmitButton() {
        int nameLength = vehicleName.getText().toString().length();
        int descriptionLength = vehicleDescription.getText().toString().length();
        if (nameLength > 0 && descriptionLength > 0) {
            submit.setEnabled(true);
        } else {
            submit.setEnabled(false);
        }
    }

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
