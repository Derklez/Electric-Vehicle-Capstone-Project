package edu.uwm.team10.electricvehicleapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    private Button startTrip;

    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        startTrip = view.findViewById(R.id.startTrip);

        if (((MainActivity)getActivity()).getTripActive()) {
            setStartButtonText("End Trip");
        } else {
            setStartButtonText("Start Trip");
        }

        // Set on click for start trip button. This should reconfigure the UI
        startTrip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!((MainActivity)getActivity()).getTripActive()) { // If a trip is NOT active
                    ((MainActivity)getActivity()).startTrip();
                } else {
                    ((MainActivity)getActivity()).endTrip();
                }
            }
        });

        return view;
    }

    public void setSpeedText(String text) {
        if (view != null) {
            TextView currentSpeed = view.findViewById(R.id.currentSpeed);
            currentSpeed.setText(text);
        }
    }

    public void setXAccelText(String text) {
        TextView xAccel = view.findViewById(R.id.xAccel);
        xAccel.setText(text);
    }

    public void setYAccelText(String text) {
        TextView yAccel = view.findViewById(R.id.yAccel);
        yAccel.setText(text);
    }

    public void setZAccelText(String text) {
        TextView zAccel = view.findViewById(R.id.zAccel);
        zAccel.setText(text);
    }

    public void setStartButtonText(String text) {
        startTrip.setText(text);
    }


}
