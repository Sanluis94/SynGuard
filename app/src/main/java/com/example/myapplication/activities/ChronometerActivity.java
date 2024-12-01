package com.example.myapplication.activities;

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
import com.example.myapplication.R;
import com.example.myapplication.models.CrisisData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class ChronometerActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private Button startStopButton;
    private boolean isRunning = false;
    private long pauseOffset = 0;

    private static int crisisCount = 0;
    private long totalCrisisTime = 0L; // Para calcular a média
    private long lastCrisisTime = 0L;

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

        sendNotificationToCaregiver(); // Envia o SMS ao iniciar o cronômetro
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
            Log.e("ChronometerActivity", "Usuário não autenticado.");
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
                    Log.d("ChronometerActivity", "Dados da crise salvos no Firebase.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar dados.", Toast.LENGTH_SHORT).show();
                    Log.e("ChronometerActivity", "Erro ao salvar dados: " + e.getMessage());
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
                                    Log.e("ChronometerActivity", "Número de telefone do cuidador não está disponível.");
                                }
                                break;
                            }
                        }
                    } else {
                        Log.e("ChronometerActivity", "Erro ao buscar cuidador: " + caregiverTask.getException());
                    }
                });
            } else {
                Log.e("ChronometerActivity", "Erro ao buscar paciente: " + task.getException());
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
            Log.e("ChronometerActivity", "Erro ao enviar SMS: " + e.getMessage());
            Toast.makeText(this, "Falha ao enviar SMS", Toast.LENGTH_SHORT).show();
        }
    }

}
