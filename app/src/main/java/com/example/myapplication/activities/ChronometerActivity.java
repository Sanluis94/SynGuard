package com.example.myapplication.activities;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.models.CrisisData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

public class ChronometerActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private Button startStopButton;
    private boolean isRunning = false;
    private long pauseOffset = 0;
    private long totalCrisisTime = 0L;
    private long crisisCount = 0L;
    private long lastCrisisTime = 0L;
    private long averageTime = 0L;

    private BluetoothDevice bluetoothDevice;  // Para armazenar o dispositivo Bluetooth
    private BluetoothSocket bluetoothSocket;  // Para a conexão Bluetooth

    private DatabaseReference databaseReference;

    private static final int SMS_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronometer);

        chronometer = findViewById(R.id.chronometer);
        startStopButton = findViewById(R.id.startStopButton);

        // Recuperar o dispositivo Bluetooth a partir do Intent
        bluetoothDevice = getIntent().getParcelableExtra("bluetoothDevice");

        if (bluetoothDevice != null) {
            // Conectar ao dispositivo Bluetooth
            connectToBluetoothDevice(bluetoothDevice);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        startStopButton.setOnClickListener(view -> toggleChronometer());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
        }

        // Recuperar o número de telefone do cuidador para enviar SMS
        getCaregiverPhoneNumber();
    }

    private void getCaregiverPhoneNumber() {
        // Obtém o ID do usuário atual
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.e("ChronometerActivity", "Usuário não autenticado.");
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Recuperar o número de telefone do cuidador do Firebase
        databaseReference.child("users").child(userId).child("phoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String caregiverPhoneNumber = dataSnapshot.getValue(String.class);
                    if (caregiverPhoneNumber != null && !caregiverPhoneNumber.isEmpty()) {
                        // Salvar o número de telefone para o envio do SMS
                        sendSmsToCaregiver(caregiverPhoneNumber, "Cronômetro iniciado.");
                    } else {
                        Toast.makeText(ChronometerActivity.this, "Número do cuidador não encontrado.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChronometerActivity.this, "Dados do cuidador não encontrados.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChronometerActivity.this, "Erro ao acessar dados do cuidador: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSmsToCaregiver(String caregiverPhoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(caregiverPhoneNumber, null, message, null, null);
            Log.d("ChronometerActivity", "Mensagem enviada ao cuidador.");
        } catch (Exception e) {
            Log.e("ChronometerActivity", "Erro ao enviar mensagem: " + e.getMessage());
            Toast.makeText(this, "Erro ao enviar mensagem.", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleChronometer() {
        if (isRunning) {
            stopAndSaveData();
        } else {
            startChronometer();
        }
    }

    private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chronometer.start();
        isRunning = true;
        startStopButton.setText("Stop");

        // Enviar SMS para o cuidador assim que o cronômetro começar
        sendSmsToCaregiver(getCaregiverPhoneNumberFromDatabase(), "Cronômetro iniciado.");
        Toast.makeText(this, "Cronômetro iniciado", Toast.LENGTH_SHORT).show();
    }

    private void stopAndSaveData() {
        chronometer.stop();
        long duration = SystemClock.elapsedRealtime() - chronometer.getBase();
        isRunning = false;

        lastCrisisTime = duration / 1000;
        totalCrisisTime += lastCrisisTime;
        crisisCount++;

        averageTime = totalCrisisTime / crisisCount;

        saveDataToFirebase(lastCrisisTime, averageTime);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        startStopButton.setText("Start");

        sendSmsToCaregiver(getCaregiverPhoneNumberFromDatabase(), "Cronômetro parado. Duração: " + lastCrisisTime + " segundos.");
        Toast.makeText(this, "Cronômetro parado e dados salvos", Toast.LENGTH_SHORT).show();
    }

    private String getCaregiverPhoneNumberFromDatabase() {
        // Recuperar o número do cuidador diretamente do banco de dados
        final String[] phoneNumber = {""};  // Usar um array para obter valor de forma assíncrona
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.child("users").child(userId).child("phoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    phoneNumber[0] = dataSnapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ChronometerActivity", "Erro ao acessar dados do cuidador: " + databaseError.getMessage());
            }
        });
        return phoneNumber[0];  // Isso ainda está assíncrono, mas serve como um exemplo
    }

    private void saveDataToFirebase(long lastCrisisTime, long averageTime) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.e("ChronometerActivity", "Usuário não autenticado.");
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Incrementa 1 porque o mês começa do 0 (Janeiro)

        DatabaseReference crisisRef = databaseReference.child(userId).child("crisisData").child(year + "-" + month).push();

        CrisisData crisisData = new CrisisData(lastCrisisTime, totalCrisisTime, crisisCount, lastCrisisTime, averageTime);
        crisisRef.setValue(crisisData)
                .addOnSuccessListener(aVoid -> Log.d("ChronometerActivity", "Dados da crise salvos com sucesso."))
                .addOnFailureListener(e -> Log.e("ChronometerActivity", "Erro ao salvar dados: " + e.getMessage()));
    }

    private void connectToBluetoothDevice(BluetoothDevice device) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            bluetoothSocket.connect();
            Log.d("ChronometerActivity", "Conectado ao dispositivo Bluetooth.");
        } catch (IOException e) {
            Log.e("ChronometerActivity", "Erro ao conectar ao Bluetooth: " + e.getMessage());
            Toast.makeText(this, "Erro ao conectar ao dispositivo Bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão de SMS concedida.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão de SMS negada.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
