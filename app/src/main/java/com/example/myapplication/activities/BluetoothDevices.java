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
import android.os.ParcelUuid;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class BluetoothDevices extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private ArrayAdapter<String> devicesAdapter;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
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
                return;
            }
            startActivityForResult(enableBtIntent, 1);
        }

        if (checkAndRequestPermissions()) {
            discoverBluetoothDevices();
        }

        devicesListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = bluetoothDevices.get(position);
            connectToDevice(device); // Qualquer dispositivo pode ser conectado
        });

        // Registrar BroadcastReceiver para eventos de volume
        IntentFilter volumeFilter = new IntentFilter();
        volumeFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(volumeReceiver, volumeFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(bluetoothReceiver);
            unregisterReceiver(volumeReceiver);  // Desregistrar o receiver de volume
        } catch (IllegalArgumentException e) {
            Log.e("BluetoothDevices", "Receiver já foi desregistrado.", e);
        }
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showPairedDevices() {
        // Obtém a lista de dispositivos Bluetooth já pareados
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // Exibe dispositivos pareados na lista
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();
                String deviceUUID = device.getUuids() != null && device.getUuids().length > 0 ? device.getUuids()[0].toString() : MY_UUID.toString(); // UUID dinâmico ou fixo
                devicesAdapter.add(deviceName + " (" + deviceAddress + ") - UUID: " + deviceUUID);
                bluetoothDevices.add(device);
            }
            devicesAdapter.notifyDataSetChanged();
            statusTextView.setText("Dispositivos pareados encontrados.");
        } else {
            statusTextView.setText("Nenhum dispositivo pareado encontrado.");
        }
    }

    // Evento para capturar teclas físicas
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        Log.d("BluetoothDevices", "Tecla pressionada: " + keyCode);

        // Verifica se é um botão de volume
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d("BluetoothDevices", "Botão de volume up pressionado");
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Log.d("BluetoothDevices", "Botão de volume down pressionado");
        }
        return true;
    }

    // BroadcastReceiver para capturar mudanças no volume
    private final BroadcastReceiver volumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                Log.d("BluetoothDevices", "Volume alterado");
                // Aqui você pode capturar o nível de volume ou realizar outra ação
            }
        }
    };

    private void connectToDevice(BluetoothDevice device) {
        new Thread(() -> {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("BluetoothConnection", "Permissão BLUETOOTH_CONNECT negada. Não foi possível estabelecer a conexão.");
                    return;
                }

                bluetoothAdapter.cancelDiscovery(); // Cancelar a descoberta antes de conectar

                // Criando um socket RFCOMM com o UUID fixo do HC-05
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);

                try {
                    // Conectar ao dispositivo HC-05
                    socket.connect();
                    bluetoothSocket = socket;

                    // Iniciar o thread para ler dados do dispositivo
                    startListeningForData(socket);

                    runOnUiThread(() -> {
                        statusTextView.setText("Conectado a " + device.getName());
                    });
                } catch (IOException e) {
                    Log.e("BluetoothConnection", "Erro ao conectar com o dispositivo: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        statusTextView.setText("Falha na conexão.");
                        Toast.makeText(BluetoothDevices.this, "Falha ao conectar", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                Log.e("BluetoothConnection", "Erro inesperado durante o processo de conexão: " + e.getMessage(), e);
            }
        }).start();
    }

    private void startListeningForData(BluetoothSocket socket) {
        new Thread(() -> {
            try {
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String message;

                while ((message = reader.readLine()) != null) {
                    // Exibe a mensagem recebida no UI
                    String finalMessage = message;
                    runOnUiThread(() -> {
                        Log.d("Bluetooth", "Mensagem recebida: " + finalMessage);
                        statusTextView.setText("Mensagem do dispositivo: " + finalMessage);
                    });
                }

            } catch (IOException e) {
                Log.e("BluetoothData", "Erro ao ler dados do dispositivo Bluetooth: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    statusTextView.setText("Erro ao ler dados.");
                    Toast.makeText(BluetoothDevices.this, "Erro ao ler dados", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // Envio de dados para o dispositivo HC-05
    private void sendDataToDevice(String data) {
        new Thread(() -> {
            try {
                if (bluetoothSocket == null) {
                    Log.e("BluetoothData", "Socket Bluetooth não está conectado.");
                    return;
                }

                OutputStream outputStream = bluetoothSocket.getOutputStream();
                outputStream.write(data.getBytes());
                outputStream.flush();
                Log.d("Bluetooth", "Mensagem enviada: " + data);

            } catch (IOException e) {
                Log.e("BluetoothData", "Erro ao enviar dados para o dispositivo: " + e.getMessage(), e);
            }
        }).start();
    }

    // Modificação na função de permissões para logar erros específicos
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
                Log.w("BluetoothPermissions", "Permissão necessária não concedida: " + permission);
                neededPermissions.add(permission);
            }
        }

        if (!neededPermissions.isEmpty()) {
            Log.i("BluetoothPermissions", "Solicitando permissões: " + neededPermissions.toString());
            ActivityCompat.requestPermissions(this, neededPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    // Modificação na função de descoberta para logar falhas específicas
    private void discoverBluetoothDevices() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        statusTextView.setText("Procurando dispositivos Bluetooth...");

        // Registra o receiver para eventos de descoberta e finalização da descoberta
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);

        // Exibe os dispositivos já pareados (para interação)
        showPairedDevices();

        // Inicia a descoberta de dispositivos Bluetooth, incluindo dispositivos não pareados
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BluetoothDiscovery", "Permissão BLUETOOTH_SCAN não concedida. Não é possível realizar a descoberta.");
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
                    Log.e("BluetoothReceiver", "Permissão BLUETOOTH_CONNECT não concedida durante a descoberta.");
                    return;
                }
                if (device != null && device.getName() != null) {
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();
                    String deviceUUID = device.getUuids() != null && device.getUuids().length > 0 ? device.getUuids()[0].toString() : MY_UUID.toString(); // UUID dinâmico ou fixo

                    devicesAdapter.add(deviceName + " (" + deviceAddress + ") - UUID: " + deviceUUID);
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


}
