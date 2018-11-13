package edu.uwm.team10.electricvehicleapp;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HomeFragment extends Fragment {


    private static final String TAG = "MainActivity";
    private Button startTrip;
    private RelativeLayout homeFragment;
    private View view;
    private Chronometer chronometer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);

        homeFragment = view.findViewById(R.id.homeFragment);
        startTrip = view.findViewById(R.id.startTrip);
        //chronometer = view.findViewById(R.id.chronometer);

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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        LinearLayout lowerLayout = view.findViewById(R.id.lowerLayout);
        LinearLayout upperLayout = view.findViewById(R.id.upperLayout);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            lowerLayout.setOrientation(LinearLayout.HORIZONTAL);
            upperLayout.setOrientation(LinearLayout.HORIZONTAL);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            lowerLayout.setOrientation(LinearLayout.VERTICAL);
            upperLayout.setOrientation(LinearLayout.VERTICAL);
        }
    }

    public void setSpeedText(String text) {
        if (view != null) {
            TextView currentSpeed = view.findViewById(R.id.currentSpeed);
            currentSpeed.setText(text);
        }
    }

    public Chronometer getChronometer() {
        chronometer =view.findViewById(R.id.chronometer);
        return chronometer;
    }

    public void setDistanceTraveledText(String text) {
        TextView distanceTraveled = view.findViewById(R.id.distanceTraveled);
        distanceTraveled.setText(text);
    }

    public void setStartButtonText(String text) {
        startTrip.setText(text);
    }


}
