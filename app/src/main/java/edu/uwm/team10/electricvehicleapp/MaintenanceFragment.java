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

    private void openNewDialog(int dialogFlag) {
        Intent intent;
        if (dialogFlag == 0) {
            intent = new Intent(getActivity(), NewVehicle.class);
        } else {
            intent = new Intent(getActivity(), NewBattery.class);
        }
        startActivity(intent);

//        LayoutInflater inflater = (LayoutInflater) getContext().
//                getSystemService(LAYOUT_INFLATER_SERVICE);
//        View popupView;
//        if (dialogFlag == 0) {
//            popupView = inflater.inflate(R.layout.new_vehicle, null);
//        } else {
//            popupView = inflater.inflate(R.layout.new_battery, null);
//        }
//
//        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
//        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
//        boolean focusable = true; // User can click outside of popup to dismiss it
//        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            popupWindow.setElevation(20);
//        }
//
//        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
//
//        popupView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                popupWindow.dismiss();
//                return true;
//            }
//        });
    }
}
