package edu.uwm.team10.electricvehicleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MaintenanceFragment extends Fragment {

    private View view;
    private ImageView newVehicle;
    private ImageView newBattery;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maintenance, container, false);

        newVehicle = view.findViewById(R.id.newVehicle);
        newBattery = view.findViewById(R.id.newBattery);

        newVehicle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getContext(), NewVehicle.class));
            }
        });

        newBattery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getContext(), NewBattery.class));
            }
        });


        return view;
    }
}
