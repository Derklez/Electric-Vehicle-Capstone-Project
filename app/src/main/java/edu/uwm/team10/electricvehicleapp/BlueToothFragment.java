package edu.uwm.team10.electricvehicleapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;


//Bluetoothfragment handles bluetooth. Will be used for connecting to an arduino which will handle the analog measurments converted to digital and transmitted via bluetooth.
//arduino may use Bluetooth LE
//this fragment uses standard bluetooth.
//On/off toggles bluetooth
//Discover toggles listing of nearby bluetooth devices should set this on a timer. Next advances the "cursor" for listed devices.
public class BlueToothFragment extends Fragment {

    BluetoothAdapter blueadapter;
    BluetoothSocket bsocket;
    ArrayList<BluetoothDevice> devices;
    Set<BluetoothDevice> paired;
    ArrayList<BluetoothDevice> found = new ArrayList<BluetoothDevice>();

    private View view;

    private String dname, daddress;
    TextView name;
    Button on;
    Button off;
    Button connect;
    Button next;
    Button discover;

    int cur = -1;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                found.add(device);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        blueadapter = BluetoothAdapter.getDefaultAdapter();
        daddress = blueadapter.getAddress();
        dname = blueadapter.getName();

        if(blueadapter.isEnabled()){
            blueadapter.cancelDiscovery();
            blueadapter.disable();
        }


// This callback is added to the start scan method as a parameter in this way
// bleAdapter.startLeScan(mLeScanCallback);

        discover = view.findViewById(R.id.bdiscover);
        next = view.findViewById(R.id.bnext);
        on = view.findViewById(R.id.bon);
        off = view.findViewById(R.id.boff);
        connect = view.findViewById(R.id.bconnect);
        //next = view.findViewById(R.id.bnext);
        name = view.findViewById(R.id.bname);
        name.setText("");
        on.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BlueToothON();
            }
        });

        off.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                BlueToothOFF();
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connect();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                next();
            }
        });
        discover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                discover();
            }
        });


        return view;
    }

    public void BlueToothON(){
        name.setText("");
        if (!blueadapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getActivity().getApplicationContext(), "BlueTooth adapter enabled",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "BlueTooth adapter already enabled", Toast.LENGTH_LONG).show();
        }
        blueadapter.cancelDiscovery();

    }
    public void discover(){

        BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                // BLE device was found, we can get its information now
                Toast.makeText(getActivity().getApplicationContext(), "BLE device found",Toast.LENGTH_LONG).show();
            }
        };

        if(blueadapter.isDiscovering()){
            blueadapter.cancelDiscovery();
            Toast.makeText(getActivity().getApplicationContext(), "Discovery reset", Toast.LENGTH_LONG).show();
            discover();
        }
        else{
            blueadapter.startDiscovery();
            Toast.makeText(getActivity().getApplicationContext(), "Discovery enabled", Toast.LENGTH_LONG).show();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getActivity().registerReceiver(receiver, discoverDevicesIntent);
        }
    }
    public void BlueToothOFF(){
        blueadapter.cancelDiscovery();
        blueadapter.disable();
        Toast.makeText(getActivity().getApplicationContext(), "BlueTooth adapter disabled" ,Toast.LENGTH_LONG).show();
        name.setText("Bluetooth is disabled.");
    }
    public void connect(){//todo
//        try{
//            bsocket.connect();
//        }catch (IOException e){
//
//        }
//        paired = blueadapter.getBondedDevices();
    }
    public void receive() {

    }
    public void next(){
        if(found == null){
            return;
        }
        cur++;
        if(cur >= found.size()){
            cur = 0;
        }
        if(found.size()<=0){
            return;
        }
        BluetoothDevice temp = found.get(cur);
        dname = temp.getName();
        daddress = temp.getAddress();
        BluetoothDevice dispositivo = blueadapter.getRemoteDevice(daddress);//connects to the device's address and checks if it's available
        try { name.setText("Name: "+dname+"\nAddress: "+daddress); }
        catch(Exception e){}
    }
}
