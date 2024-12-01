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
        }

        // Verificar se o Bluetooth está ativado
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        // Verificar permissões para Bluetooth
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        } else {
            startBluetoothScan();
        }
    }

    // Método para iniciar a busca por dispositivos Bluetooth
    private void startBluetoothScan() {
        // Atualizar UI
        statusTextView.setText("Procurando dispositivos...");
        progressBar.setVisibility(ProgressBar.VISIBLE);

        // Registrar o receptor para detectar dispositivos Bluetooth
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter);

        // Iniciar a busca por dispositivos Bluetooth
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        bluetoothAdapter.startDiscovery();
    }

    // BroadcastReceiver para lidar com os dispositivos encontrados
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(BluetoothDevices.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                // Adicionar dispositivo à lista
                if (deviceName != null && !devicesList.contains(deviceName)) {
                    devicesList.add(deviceName + " (" + deviceAddress + ")");
                    devicesAdapter.notifyDataSetChanged();
                }

                // Conectar ao HC-05 se encontrado
                if (deviceName != null && deviceName.equals("HC-05")) { // Nome do dispositivo HC-05
                    bluetoothDevice = device;
                    connectToDevice();
                }
            }
        }
    };

    // Método para conectar ao dispositivo Bluetooth
    private void connectToDevice() {
        new Thread(() -> {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                handler.post(() -> {
                    Toast.makeText(BluetoothDevices.this, "Conectado ao HC-05", Toast.LENGTH_SHORT).show();

                    // Enviar comando para iniciar o cronômetro
                    sendStartChronometerCommand();
                });
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(BluetoothDevices.this, "Falha ao conectar", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sendStartChronometerCommand() {
        try {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                // Enviar comando para iniciar o cronômetro, pode ser um simples caractere ou string
                bluetoothSocket.getOutputStream().write("START_CRONOMETER".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        unregisterReceiver(broadcastReceiver); // Não se esqueça de desregistrar o receptor de broadcast
    }

    // Lidar com a resposta da solicitação de permissão
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothScan();
            } else {
                Toast.makeText(this, "Permissões de Bluetooth necessárias.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
