package com.example.myapplication.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BluetoothDevices extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private ArrayAdapter<String> devicesAdapter;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int PERMISSION_REQUEST_CODE = 1;

    // UI Components
    private TextView statusTextView;
    private ProgressBar progressBar;
    private ListView devicesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_devices);

        // Inicializando UI
        statusTextView = findViewById(R.id.statusTextView);
        progressBar = findViewById(R.id.progressBar);
        devicesListView = findViewById(R.id.devicesListView);

        bluetoothDevices = new ArrayList<>();
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        devicesListView.setAdapter(devicesAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não disponível no dispositivo.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, 1);
        }

        if (checkAndRequestPermissions()) {
            discoverBluetoothDevices();
        }

        devicesListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = bluetoothDevices.get(position);
            if ("HC-05".equals(device.getName())) {
                connectToDevice(device);
            } else {
                Toast.makeText(this, "Este dispositivo não é compatível.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException e) {
            Log.e("BluetoothDevices", "Receiver já foi desregistrado.", e);
        }
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
        };

        ArrayList<String> neededPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                neededPermissions.add(permission);
            }
        }

        if (!neededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    private void discoverBluetoothDevices() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        statusTextView.setText("Procurando dispositivos Bluetooth...");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(BluetoothDevices.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (device != null && device.getName() != null) {
                    devicesAdapter.add(device.getName() + " (" + device.getAddress() + ")");
                    bluetoothDevices.add(device);
                    devicesAdapter.notifyDataSetChanged();
                    statusTextView.setText("Dispositivos encontrados.");
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                if (bluetoothDevices.isEmpty()) {
                    statusTextView.setText("Nenhum dispositivo encontrado.");
                }
            }
        }
    };

    private void connectToDevice(BluetoothDevice device) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothAdapter.cancelDiscovery(); // Certifique-se de cancelar a descoberta
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            Log.d("BluetoothDevices", "Conectado ao dispositivo Bluetooth.");
            Toast.makeText(this, "Dispositivo conectado.", Toast.LENGTH_SHORT).show();

            startChronometerActivity(device);

        } catch (IOException e) {
            Log.e("BluetoothDevices", "Erro ao conectar ao dispositivo: " + e.getMessage());
            Toast.makeText(this, "Erro ao conectar ao dispositivo.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startChronometerActivity(BluetoothDevice device) {
        Intent intent = new Intent(this, ChronometerActivity.class);
        intent.putExtra("bluetoothDevice", device);
        startActivity(intent);
    }
}
