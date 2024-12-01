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
import android.os.Handler;
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
    private BluetoothDevice bluetoothDevice;
    private Handler handler;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID do HC-05
    private static final String DEVICE_ADDRESS = "00:21:13:01:0A:F4"; // Substitua com o endereço MAC do seu HC-05

    private static final int PERMISSION_REQUEST_CODE = 1; // Código para solicitar permissões

    // UI Components
    private TextView statusTextView;
    private ProgressBar progressBar;
    private ListView devicesListView;
    private ArrayAdapter<String> devicesAdapter;
    private ArrayList<String> devicesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_devices); // Certifique-se de ter o layout correto

        // Inicializando a UI
        statusTextView = findViewById(R.id.statusTextView);
        progressBar = findViewById(R.id.progressBar);
        devicesListView = findViewById(R.id.devicesListView);

        devicesList = new ArrayList<>();
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devicesList);
        devicesListView.setAdapter(devicesAdapter);

        // Inicializar o Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não disponível no dispositivo.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        handler = new Handler();

        // Verifica se as permissões de Bluetooth e localização estão concedidas
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            discoverBluetoothDevices();
        }
    }

    private void discoverBluetoothDevices() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        statusTextView.setText("Procurando dispositivos Bluetooth...");

        // Register for discovering Bluetooth devices
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceAddress = device.getAddress(); // Endereço MAC
                devicesList.add(deviceName + "\n" + deviceAddress);
                devicesAdapter.notifyDataSetChanged();
            }
        }
    };

    private void connectToDevice() {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            statusTextView.setText("Conectado ao dispositivo Bluetooth.");

            // Agora você pode iniciar o cronômetro
            startChronometer();
        } catch (IOException e) {
            statusTextView.setText("Falha ao conectar ao dispositivo.");
            e.printStackTrace();
        }
    }

    private void startChronometer() {
        // Você pode iniciar o cronômetro aqui. Utilize a mesma lógica que estava no ChronometerActivity.
        // Ou você pode passar essa lógica para o BluetoothDevices caso seja necessário iniciar o cronômetro
        // diretamente a partir dessa activity.
    }
}
