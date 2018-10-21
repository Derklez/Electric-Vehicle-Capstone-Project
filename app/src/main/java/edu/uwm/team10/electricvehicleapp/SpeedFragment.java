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

    @Override
    public void onResume() {
        super.onResume();
        mTimer1 = new Runnable() {
            @Override
            public void run() {
                mSeries1.resetData(generateSpeedData());
                mHandler.postDelayed(this, 500);
            }
        };
        mHandler.postDelayed(mTimer1, 500);
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer1);
        super.onPause();
    }


    private DataPoint[] generateSpeedData() {
        // int count = 30;
        if (((MainActivity)getActivity()).getSpeedMeasurements() != null) {
            int count = ((MainActivity) getActivity()).getSpeedMeasurements().size();
            DataPoint[] speedValues = new DataPoint[count];
            for (int i = 0; i < count; ++i) {
                double x = i;
                speedValues[i] = new DataPoint(i, ((MainActivity) getActivity()).getSpeedMeasurements().get(i));
            }
            return speedValues;
        }
        return new DataPoint[0];
    }

}
