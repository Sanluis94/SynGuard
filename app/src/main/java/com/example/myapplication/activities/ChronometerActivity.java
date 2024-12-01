package com.example.myapplication.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.example.myapplication.models.CrisisData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.UUID;

public class ChronometerActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private Button startStopButton, bluetoothButton;
    private boolean isRunning = false;
    private long pauseOffset = 0;

    private static int crisisCount = 0;
    private long totalCrisisTime = 0L; // Para calcular a média
    private long lastCrisisTime = 0L;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private static final UUID MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "ChronometerActivity";
    private Handler handler = new Handler();
    private boolean bluetoothConnected = false;

    // Firebase
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronometer);

        chronometer = findViewById(R.id.chronometer);
        startStopButton = findViewById(R.id.startStopButton);

        databaseReference = FirebaseDatabase.getInstance().getReference("crisisData");

        startStopButton.setOnClickListener(view -> toggleChronometer());
        bluetoothButton.setOnClickListener(view -> connectBluetooth());
    }

    private void toggleChronometer() {
        if (!isRunning) {
            startChronometer();
        } else {
            stopAndSaveData();
        }
    }

    private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        isRunning = true;
        startStopButton.setText("Stop");

        sendNotificationToCaregiver();
        Toast.makeText(this, "Cronômetro iniciado", Toast.LENGTH_SHORT).show();
    }

    private void stopAndSaveData() {
        chronometer.stop();
        long duration = SystemClock.elapsedRealtime() - chronometer.getBase();
        isRunning = false;

        // Atualiza os dados de crise
        lastCrisisTime = duration / 1000; // Convertendo para segundos
        totalCrisisTime += lastCrisisTime;
        crisisCount++;
        long averageTime = totalCrisisTime / crisisCount;

        // Salva os dados no Firebase
        saveDataToFirebase(lastCrisisTime, averageTime);

        // Reseta o cronômetro
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        startStopButton.setText("Start");

        Toast.makeText(this, "Cronômetro parado e dados salvos", Toast.LENGTH_SHORT).show();
    }

    private void saveDataToFirebase(long duration, long averageTime) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.e(TAG, "Usuário não autenticado.");
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calcular o ano e mês da crise
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Janeiro é 0, então adicionamos 1
        String crisisId = String.valueOf(crisisCount);

        // Criar o objeto de dados
        CrisisData crisisData = new CrisisData(crisisCount, duration, crisisCount, averageTime, duration);

        // Salvar no Firebase
        DatabaseReference crisisRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("crisisData")
                .child(String.valueOf(year))
                .child(String.valueOf(month))
                .child(crisisId);

        crisisRef.setValue(crisisData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Dados salvos com sucesso.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Dados da crise salvos no Firebase.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar dados.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao salvar dados: " + e.getMessage());
                });
    }

    private void sendNotificationToCaregiver() {
        // Obter o usuário atual
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Consultar o nó "users" para obter o caregiverId do paciente
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String caregiverId = task.getResult().child("caregiverId").getValue(String.class);

                // Buscar o cuidador correspondente no nó "users"
                usersRef.orderByChild("caregiverId").equalTo(caregiverId).get().addOnCompleteListener(caregiverTask -> {
                    if (caregiverTask.isSuccessful() && caregiverTask.getResult() != null) {
                        for (DataSnapshot snapshot : caregiverTask.getResult().getChildren()) {
                            String role = snapshot.child("role").getValue(String.class);
                            if ("cuidador".equals(role)) {
                                String phoneNumber = snapshot.child("phone").getValue(String.class);

                                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                                    sendSmsToCaregiver(phoneNumber);
                                } else {
                                    Log.e(TAG, "Número de telefone do cuidador não está disponível.");
                                }
                                break;
                            }
                        }
                    } else {
                        Log.e(TAG, "Erro ao buscar cuidador: " + caregiverTask.getException());
                    }
                });
            } else {
                Log.e(TAG, "Erro ao buscar paciente: " + task.getException());
            }
        });
    }

    private void sendSmsToCaregiver(String phoneNumber) {
        try {
            String message = "O cronômetro foi iniciado pelo paciente!";
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS enviado ao cuidador", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao enviar SMS: " + e.getMessage());
            Toast.makeText(this, "Falha ao enviar SMS", Toast.LENGTH_SHORT).show();
        }
    }

    private void connectBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não suportado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Ative o Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice("00:21:13:01:0A:F4"); // Substitua pelo endereço do HC-05
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MODULE_UUID);
            bluetoothSocket.connect();

            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
            bluetoothConnected = true;

            Toast.makeText(this, "Conexão Bluetooth estabelecida", Toast.LENGTH_SHORT).show();

            // Loop de leitura Bluetooth
            new Thread(() -> {
                try {
                    while (bluetoothConnected) {
                        int bytesAvailable = inputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] buffer = new byte[bytesAvailable];
                            inputStream.read(buffer);
                            String receivedData = new String(buffer, 0, bytesAvailable);
                            // Processa dados recebidos aqui (ex: inicia cronômetro)
                            Log.d(TAG, "Dados recebidos: " + receivedData);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Erro de leitura Bluetooth: " + e.getMessage());
                    bluetoothConnected = false;
                    Toast.makeText(this, "Erro na conexão Bluetooth", Toast.LENGTH_SHORT).show();
                }
            }).start();
        } catch (IOException e) {
            Log.e(TAG, "Erro ao conectar ao Bluetooth: " + e.getMessage());
            Toast.makeText(this, "Erro ao conectar ao Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

}
