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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ChronometerActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private Button startStopButton, bluetoothButton;
    private boolean isRunning = false;
    private long pauseOffset = 0;

    int crisisCount = 0;       // Exemplo inicial
    long averageTime = 0L;     // Média inicial
    long lastCrisisTime = 0L;  // Última crise inicial

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
        bluetoothButton = findViewById(R.id.bluetoothButton);

        databaseReference = FirebaseDatabase.getInstance().getReference("crisisData");

        startStopButton.setOnClickListener(view -> toggleChronometer());

        bluetoothButton.setOnClickListener(view -> connectBluetooth());
    }

    private void toggleChronometer() {
        if (!isRunning) {
            startChronometer();
        } else {
            stopChronometer();
        }
    }

    private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();
        isRunning = true;
        startStopButton.setText("Stop");

        // Save initial data and send notification
        sendNotificationToCaregiver();
        Toast.makeText(this, "Cronômetro iniciado", Toast.LENGTH_SHORT).show();
    }

    private void stopChronometer() {
        chronometer.stop();
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
        isRunning = false;
        startStopButton.setText("Reset");

        // Save data to Firebase
        long duration = pauseOffset / 1000;
        saveDataToFirebase(duration);
    }

    private void resetChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        startStopButton.setText("Start");
    }

    private void saveDataToFirebase(long duration) {
        String caregiverId = "dummyCaregiverId"; // Substitua pelo ID real
        // Construtor esperado
        CrisisData crisisData = new CrisisData(caregiverId, duration, crisisCount, averageTime, lastCrisisTime);

        databaseReference.push().setValue(crisisData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Dados salvos com sucesso", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Erro ao salvar dados", Toast.LENGTH_SHORT).show());
    }

    private void sendNotificationToCaregiver() {
        String channelId = "CaregiverNotification";
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Caregiver Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Cronômetro iniciado")
                .setContentText("O cronômetro foi iniciado pelo paciente.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(1, builder.build());
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

        // Iniciar conexão Bluetooth com o dispositivo HC-05
        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice("00:00:00:00:00:00"); // Insira o endereço do HC-05
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
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MODULE_UUID);
            bluetoothSocket.connect();

            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
            bluetoothConnected = true;

            Toast.makeText(this, "Conexão Bluetooth estabelecida", Toast.LENGTH_SHORT).show();

            // Listener para receber dados do HC-05
            handler.post(() -> {
                try {
                    if (inputStream.available() > 0) {
                        int data = inputStream.read();
                        if (data == '1') { // Pressionado
                            toggleChronometer();
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Erro ao ler Bluetooth", e);
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Erro ao conectar Bluetooth", e);
            Toast.makeText(this, "Erro ao conectar Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }
}
