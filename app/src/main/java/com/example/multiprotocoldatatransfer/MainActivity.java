package com.example.multiprotocoldatatransfer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Fragment;

import java.util.Set;


public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;
    protected static BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice deviceDiscovered;
    protected static ConnectedThread deviceConnection = new ConnectedThread();
    private final IntentFilter bluetoothIntentFilter = new IntentFilter();

    Channel mChannel;
    WifiP2pManager mManager;
    boolean isWifiP2PEnabled = true;
    private final IntentFilter wifiDirectIntentFilter = new IntentFilter();
    BroadcastReceiver wifiDirectReceiver;

    private Button serverButton;
    private Button clientButton;
    private Button sendButton;
    private EditText typedMessage;
    protected static TextView chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverButton = findViewById(R.id.button2);
        clientButton = findViewById(R.id.button3);
        sendButton = findViewById(R.id.button);
        typedMessage = findViewById(R.id.editText);
        chat = findViewById(R.id.textView);

        chat.append("\n");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.e("Printing", "Device does not support Bluetooth");
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

            //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            bluetoothIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, bluetoothIntentFilter);

            Intent discoverableIntent =
                    new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);

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

        wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiDirectIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        wifiDirectReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
    }

    @Override
    public void onResume(){
        super.onResume();
        registerReceiver(wifiDirectReceiver, wifiDirectIntentFilter);
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e("Printing", "Wifi Direct Discovery Succeeded");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.e("Printing", "Wifi Direct Discovery Failed with code: " + reasonCode);
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(wifiDirectReceiver);
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
            else if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){

            }
            else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){

            }
            else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){

            }
            else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

            }
        }
    };

    protected void onDestroy(){
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }
}