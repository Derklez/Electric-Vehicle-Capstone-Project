package edu.uwm.team10.electricvehicleapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class ComparisonGraphDialogActivity extends AppCompatActivity {

    private GraphView comparisonGraph;
    private LineGraphSeries<DataPoint> speedSeries1;
    private LineGraphSeries<DataPoint> speedSeries2;
    private LineGraphSeries<DataPoint> accelSeries1;
    private LineGraphSeries<DataPoint> accelSeries2;
    CheckBox speedTrip1;
    CheckBox speedTrip2;
    CheckBox accelTrip1;
    CheckBox accelTrip2;
    double[] speedData1;
    double[] speedData2;
    double[] accelData1;
    double[] accelData2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comparison_graph_dialog);

        speedTrip1 = findViewById(R.id.speedTrip1);
        speedTrip2 = findViewById(R.id.speedTrip2);
        accelTrip1 = findViewById(R.id.accelTrip1);
        accelTrip2 = findViewById(R.id.accelTrip2);

        accelTrip1.setText(Html.fromHtml("Accel Trip 1 (m/s<sup><small>2</small></sup>)"));
        accelTrip2.setText(Html.fromHtml("Accel Trip 2 (m/s<sup><small>2</small></sup>)"));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            speedData1 = bundle.getDoubleArray("speedData1");
            speedData2 = bundle.getDoubleArray("speedData2");
            accelData1 = bundle.getDoubleArray("accelData1");
            accelData2 = bundle.getDoubleArray("accelData2");
        } else {
            speedData1 = null;
            speedData2 = null;
            accelData1 = null;
            accelData2 = null;
        }

        comparisonGraph = findViewById(R.id.comparisonGraph);
        comparisonGraph.setTitle("Speed/Acceleration Comparisons");
        comparisonGraph.getViewport().setXAxisBoundsManual(true);
        comparisonGraph.getViewport().setMinX(0);
        comparisonGraph.getViewport().setMinY(0);
        setGraphAxisSize();

        speedSeries1 = new LineGraphSeries<>(generateDataPoints(speedData1));
        speedSeries1.setDrawDataPoints(true);
        speedSeries1.setThickness(5);
        speedSeries1.setColor(Color.RED);

        speedSeries2 = new LineGraphSeries<>(generateDataPoints(speedData2));
        speedSeries2.setDrawDataPoints(true);
        speedSeries2.setThickness(5);
        speedSeries2.setColor(Color.YELLOW);

        accelSeries1 = new LineGraphSeries<>(generateDataPoints(accelData1));
        accelSeries1.setDrawDataPoints(true);
        accelSeries1.setThickness(5);
        accelSeries1.setColor(Color.BLUE);

        accelSeries2 = new LineGraphSeries<>(generateDataPoints(accelData2));
        accelSeries2.setDrawDataPoints(true);
        accelSeries2.setThickness(5);
        accelSeries2.setColor(Color.GREEN);

        speedTrip1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    comparisonGraph.addSeries(speedSeries1);
                    setGraphAxisSize();
                } else {
                    comparisonGraph.removeSeries(speedSeries1);
                    setGraphAxisSize();
                }
            }
        });
        speedTrip2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    comparisonGraph.addSeries(speedSeries2);
                    setGraphAxisSize();
                } else {
                    comparisonGraph.removeSeries(speedSeries2);
                    setGraphAxisSize();
                }
            }
        });
        accelTrip1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    comparisonGraph.addSeries(accelSeries1);
                    setGraphAxisSize();
                } else {
                    comparisonGraph.removeSeries(accelSeries1);
                    setGraphAxisSize();
                }
            }
        });
        accelTrip2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    comparisonGraph.addSeries(accelSeries2);
                    setGraphAxisSize();
                } else {
                    comparisonGraph.removeSeries(accelSeries2);
                    setGraphAxisSize();
                }
            }
        });
    }


    /**
     *     Resets the X axis of the graph to the appropriate length. First check if both trips have at
     *     least one graph displayed. Since speed and accel arrays from a trip should be the same length,
     *     we use the speed array length to set the X axis size. If both trips have at least one of speed
     *     or accel displayed, find the max length and set the axis to that. If only one of the trips is
     *     active, set X axis to that size. If neither, default to 8 (AndroidGraphView default)
     */
    private void setGraphAxisSize() {
        boolean trip1Displayed = speedTrip1.isChecked() || accelTrip1.isChecked();
        boolean trip2Displayed = speedTrip2.isChecked() || accelTrip2.isChecked();
        comparisonGraph.getViewport().setMinY(0);
        if (trip1Displayed && trip2Displayed) {
            int xAxisLength = Math.max(speedData1.length, speedData2.length);
            comparisonGraph.getViewport().setMaxX(xAxisLength);
        } else if (trip1Displayed) {
            comparisonGraph.getViewport().setMaxX(speedData1.length);
        } else if (trip2Displayed) {
            comparisonGraph.getViewport().setMaxX(speedData2.length);
        } else {
            comparisonGraph.getViewport().setMaxX(8);
        }
    }

    /**
     * Based on a passed in array, loop through each element and add it to an Android Graph View
     * DataPoint array to be displayed on a graph.
     * @param arrayData arbitrary data set to be turned into data points
     * @return the array of in order data points
     */
    private DataPoint[] generateDataPoints(double[] arrayData) {
        if (arrayData != null) {
            if (arrayData.length > 0) {
                int count = arrayData.length;
                DataPoint[] dataValues = new DataPoint[count];
                for (int i = 0; i < count; ++i) {
                    dataValues[i] = new DataPoint(i, arrayData[i]);
                }
                return dataValues;
            }
        }
        return new DataPoint[0];
    }
}
