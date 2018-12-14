package edu.uwm.team10.electricvehicleapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class SpeedFragment extends Fragment {

    private final Handler mHandler = new Handler();
    private Runnable mTimer1;
    private View view;
    private GraphView speedGraph;
    private LineGraphSeries<DataPoint> mSeries1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_speed, container, false);

        speedGraph = view.findViewById(R.id.speedGraph);
        mSeries1 = new LineGraphSeries<>(generateSpeedData());
        speedGraph.addSeries(mSeries1);

        return view;
    }

    /**
     * Callback for Android Graph View. Every 1 second regenerate the speed data so it is constantly
     * updated. Might need to make this delay long for efficiency.
     */
    @Override
    public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                mSeries1.resetData(generateSpeedData());
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mTimer1, 1000);
    }

    /**
     * Required for Android Graph View.
     */
    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        super.onPause();
    }

    /**
     * Based on the current speed measurements, create an array of DataPoints for the graph to use.
     * @return an array of DataPoints for the graph to use.
     */
    private DataPoint[] generateSpeedData() {
        ArrayList<Double> speedMeasurements = ((MainActivity)getActivity()).getSpeedMeasurements();
        if (speedMeasurements != null) {
            if (speedMeasurements.size() > 0) {
                int count = speedMeasurements.size();
                DataPoint[] speedValues = new DataPoint[count];
                for (int i = count - 1; i >= 0; --i) {
                    speedValues[i] = new DataPoint(i, speedMeasurements.get(i));
                }
                return speedValues;
            }
        }
        return new DataPoint[0];
    }

}
