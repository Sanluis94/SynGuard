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
    private static final int PERMISSION_REQUEST_CODE = 1; // Código para solicitar permissões

    // UI Components
    private TextView statusTextView;
    private ProgressBar progressBar;
    private ListView devicesListView;
    private ArrayAdapter<String> devicesAdapter;
    private ArrayList<String> devicesList;
    private ArrayList<BluetoothDevice> bluetoothDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_devices);

        // Inicializando a UI
        statusTextView = findViewById(R.id.statusTextView);
        progressBar = findViewById(R.id.progressBar);
        devicesListView = findViewById(R.id.devicesListView);

        devicesList = new ArrayList<>();
        bluetoothDevices = new ArrayList<>();
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
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, PERMISSION_REQUEST_CODE);
        } else {
            discoverBluetoothDevices();
        }

        // Ao selecionar um item da lista, tenta se conectar ao dispositivo selecionado
        devicesListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = bluetoothDevices.get(position);
            connectToDevice(device); // Conectar ao dispositivo selecionado
        });
    }

    private void discoverBluetoothDevices() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        statusTextView.setText("Procurando dispositivos Bluetooth...");

        // Register for discovering Bluetooth devices
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);

        // Iniciar a descoberta de dispositivos Bluetooth
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
                if (device != null) {
                    if (ActivityCompat.checkSelfPermission(BluetoothDevices.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(BluetoothDevices.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                        return;
                    }

                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress(); // Endereço MAC

                    // Exibir dispositivos encontrados apenas se o nome for "HC-05"
                    if (deviceName != null && deviceName.equals("HC-05")) {
                        devicesList.add("Dispositivo encontrado: " + deviceName + "\n" + deviceAddress);
                        bluetoothDevices.add(device); // Adicionar dispositivo à lista
                        devicesAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(ProgressBar.INVISIBLE);
                        statusTextView.setText("Dispositivos encontrados! Selecione um dispositivo.");
                    }
                }
            }
        }
    };

    private void connectToDevice(BluetoothDevice device) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            statusTextView.setText("Conectado ao dispositivo Bluetooth.");

            // Navegar para o menu principal (MainActivity)
            Intent intent = new Intent(BluetoothDevices.this, PatientMenu.class);
            startActivity(intent);
            finish(); // Fecha a atividade atual

        } catch (IOException e) {
            statusTextView.setText("Falha ao conectar ao dispositivo.");
            Toast.makeText(this, "Falha ao conectar ao dispositivo.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, iniciar a descoberta de dispositivos
                discoverBluetoothDevices();
            } else {
                Toast.makeText(this, "Permissão negada. O Bluetooth não pode ser utilizado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothReceiver != null) {
            unregisterReceiver(bluetoothReceiver); // Liberar recursos do BroadcastReceiver
        }

        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close(); // Fechar a conexão Bluetooth ao sair da atividade
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
