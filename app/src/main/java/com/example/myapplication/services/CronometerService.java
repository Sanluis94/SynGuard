package com.example.myapplication.services;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;

public class CronometerService extends Service {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;

    private Handler handler = new Handler();
    private boolean isRunning = false;
    private long startTime = 0L;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Firebase
    private DatabaseReference databaseReference;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    public void connectToDevice(String deviceAddress) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e("CronometerService", "Bluetooth não está ativado.");
            stopSelf();
            return;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
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
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            listenForData();
        } catch (IOException e) {
            Log.e("CronometerService", "Erro ao conectar ao dispositivo Bluetooth.", e);
            stopSelf();
        }
    }

    private void listenForData() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    if (inputStream != null && (bytes = inputStream.read(buffer)) > 0) {
                        String message = new String(buffer, 0, bytes);
                        if (message.contains("START")) {
                            handler.post(this::toggleChronometer);
                        }
                    }
                } catch (IOException e) {
                    Log.e("CronometerService", "Erro ao ler do dispositivo Bluetooth.", e);
                    stopSelf();
                    break;
                }
            }
        }).start();
    }

    private void toggleChronometer() {
        if (isRunning) {
            stopChronometer();
        } else {
            startChronometer();
        }
    }

    private void startChronometer() {
        startTime = SystemClock.elapsedRealtime();
        isRunning = true;
        sendSmsNotification("Cronômetro iniciado.");
    }

    private void stopChronometer() {
        long duration = (SystemClock.elapsedRealtime() - startTime) / 1000; // Segundos
        isRunning = false;
        saveDataToFirebase(duration);
        sendSmsNotification("Cronômetro parado. Duração: " + duration + " segundos.");
    }

    private void saveDataToFirebase(long duration) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.e("CronometerService", "Usuário não autenticado.");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatabaseReference userRef = databaseReference.child(userId);
        DatabaseReference crisisRef = userRef.child("crisisData").child(String.valueOf(year))
                .child(String.valueOf(month)).child(String.valueOf(day)).push();

        crisisRef.child("duration").setValue(duration);
        crisisRef.child("crisisCount").setValue(1);

        userRef.child("crisisCount").setValue(1);
    }

    private void sendSmsNotification(String message) {
        String caregiverPhoneNumber = "5511973039004"; // Número do cuidador
        SmsManager smsManager = SmsManager.getDefault();
        try {
            smsManager.sendTextMessage(caregiverPhoneNumber, null, message, null, null);
            handler.post(() -> Toast.makeText(getApplicationContext(), "SMS enviado com sucesso.", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            handler.post(() -> Toast.makeText(getApplicationContext(), "Falha ao enviar SMS.", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            Log.e("CronometerService", "Erro ao fechar conexão Bluetooth.", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
