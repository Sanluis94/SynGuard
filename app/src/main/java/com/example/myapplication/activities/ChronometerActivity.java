package com.example.myapplication.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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

import java.util.Calendar;

public class ChronometerActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private Button startStopButton;
    private boolean isRunning = false;
    private long pauseOffset = 0;
    private long totalCrisisTime = 0L;
    private long crisisCount = 0L;
    private long lastCrisisTime = 0L;

    private DatabaseReference databaseReference;

    private static final int SMS_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronometer);

        chronometer = findViewById(R.id.chronometer);
        startStopButton = findViewById(R.id.startStopButton);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        startStopButton.setOnClickListener(view -> toggleChronometer());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
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
        chronometer.start();
        isRunning = true;
        startStopButton.setText("Stop");

        sendSmsToCaregiver("Cronômetro iniciado.");
        Toast.makeText(this, "Cronômetro iniciado", Toast.LENGTH_SHORT).show();
    }

    private void stopAndSaveData() {
        chronometer.stop();
        long duration = SystemClock.elapsedRealtime() - chronometer.getBase();
        isRunning = false;

        lastCrisisTime = duration / 1000;
        totalCrisisTime += lastCrisisTime;
        crisisCount++;

        long averageTime = totalCrisisTime / crisisCount;

        saveDataToFirebase(lastCrisisTime, averageTime);

        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        startStopButton.setText("Start");

        sendSmsToCaregiver("Cronômetro parado. Duração: " + lastCrisisTime + " segundos.");
        Toast.makeText(this, "Cronômetro parado e dados salvos", Toast.LENGTH_SHORT).show();
    }

    private void saveDataToFirebase(long duration, long averageTime) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.e("ChronometerActivity", "Usuário não autenticado.");
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;

        DatabaseReference crisisRef = databaseReference.child(userId).child("crisisData").child(year + "-" + month).push();

        CrisisData crisisData = new CrisisData(duration, totalCrisisTime, crisisCount, lastCrisisTime, averageTime);
        crisisRef.setValue(crisisData)
                .addOnSuccessListener(aVoid -> Log.d("ChronometerActivity", "Dados da crise salvos com sucesso."))
                .addOnFailureListener(e -> Log.e("ChronometerActivity", "Erro ao salvar dados: " + e.getMessage()));
    }

    private void sendSmsToCaregiver(String message) {
        String caregiverPhoneNumber = "+5511973039004"; // Número no formato internacional
        if (caregiverPhoneNumber == null || caregiverPhoneNumber.isEmpty()) {
            Toast.makeText(this, "Número do cuidador não configurado.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(caregiverPhoneNumber, null, message, null, null);
            Log.d("ChronometerActivity", "Mensagem enviada ao cuidador.");
        } catch (Exception e) {
            Log.e("ChronometerActivity", "Erro ao enviar mensagem: " + e.getMessage());
            Toast.makeText(this, "Erro ao enviar mensagem.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
