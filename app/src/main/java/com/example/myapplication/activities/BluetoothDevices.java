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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.R;

import java.io.IOException;
import java.util.UUID;

public class BluetoothDevices extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private Handler handler;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID do HC-05
    private static final String DEVICE_ADDRESS = "00:21:13:01:0A:F4"; // Substitua com o endereço MAC do seu HC-05

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_devices); // Certifique-se de ter o layout correto

        // Inicializar o Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não disponível no dispositivo.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Verificar se o Bluetooth está ativado
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

        // Registrar o receptor para detectar dispositivos Bluetooth
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter);

        // Iniciar a busca por dispositivos Bluetooth
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
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                if (deviceName != null && deviceName.equals("HC-05")) { // Nome do dispositivo HC-05
                    bluetoothDevice = device;
                    connectToDevice();
                }
            }
        }
    };

    private void connectToDevice() {
        // Tentando conectar ao dispositivo Bluetooth
        new Thread(() -> {
            try {
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
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                handler.post(() -> Toast.makeText(BluetoothDevices.this, "Conectado ao HC-05", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                handler.post(() -> Toast.makeText(BluetoothDevices.this, "Falha ao conectar", Toast.LENGTH_SHORT).show());
            }
        }).start();
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
}
