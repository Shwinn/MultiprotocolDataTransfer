package com.example.multiprotocoldatatransfer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    protected static BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice deviceDiscovered;
    protected static ConnectedThread deviceConnection = new ConnectedThread();

    private Button serverButton;
    private Button clientButton;
    private Button sendButton;
    private EditText typedMessage;
    protected static TextView chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            System.out.println("Device does not support Bluetooth");
        }
        else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.e("Printing", "ALREADY A PAIRED DEVICE");
                    Log.e("Printing", "Device Name: " + deviceName + " ; Device Hardware Address: " + deviceHardwareAddress);
                }
            }

            for(BluetoothDevice bd : pairedDevices){
                deviceDiscovered = bd;
                break;
            }

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);

            Intent discoverableIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);

            setContentView(R.layout.activity_main);
            serverButton = findViewById(R.id.button2);
            clientButton = findViewById(R.id.button3);
            sendButton = findViewById(R.id.button);
            typedMessage = findViewById(R.id.editText);
            chat = findViewById(R.id.textView);

            chat.append("\n");

            serverButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    Log.e("Printing", "Server Button Pressed");
                    AcceptThread at = new AcceptThread();
                    at.start();
                }
            });

            clientButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    Log.e("Printing", "Client Button Pressed");
                    ConnectThread ct = new ConnectThread(deviceDiscovered);
                    ct.start();
                }
            });

            sendButton.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    String message = typedMessage.getText().toString();
                    deviceConnection.write(message.getBytes());
                    chat.append("Sent: " + message + "\n");
                }
            });

            mBluetoothAdapter.startDiscovery();
            Log.e("Printing", "Started device discovery");
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.e("Printing", "BROADCAST RECEIVER DISCOVERED A DEVICE");
                Log.e("Printing", "Device Name: " + deviceName + " Device Hardware Address: " + deviceHardwareAddress);
            }
        }
    };

    protected void onDestroy(){
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }
}