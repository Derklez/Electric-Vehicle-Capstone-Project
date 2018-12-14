package edu.uwm.team10.electricvehicleapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class MaintenanceFragment extends Fragment {

    private View view;
    private Button newVehicle;
    private Button newBattery;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maintenance, container, false);

        newVehicle = view.findViewById(R.id.newVehicle);
        newBattery = view.findViewById(R.id.newBattery);

        newVehicle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openNewDialog(0);
            }
        });

        newBattery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openNewDialog(1);
            }
        });

        return view;
    }

    /**
     * Starts a new activity based on which button was clicked
     * @param dialogFlag 0 indicates vehicle button was pressed, 1 indicates battery button
     */
    private void openNewDialog(int dialogFlag) {
        Intent intent;
        if (dialogFlag == 0) {
            intent = new Intent(getActivity(), NewVehicle.class);
        } else {
            intent = new Intent(getActivity(), NewBattery.class);
        }
        startActivity(intent);
    }
}
