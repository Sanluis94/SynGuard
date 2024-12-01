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

    private long totalCrisisTime = 0L; // Para calcular a média
    private long crisisCount = 0L; // Contagem de crises
    private long lastCrisisTime = 0L;

    // Firebase
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronometer);

        chronometer = findViewById(R.id.chronometer);
        startStopButton = findViewById(R.id.startStopButton);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        startStopButton.setOnClickListener(view -> toggleChronometer());

        // Recuperar os dados de crises do Firebase ao abrir o aplicativo
        loadCrisisData();
    }

    private void loadCrisisData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.e("ChronometerActivity", "Usuário não autenticado.");
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Acessar o nó do Firebase que contém os dados de crises do usuário
        DatabaseReference crisisRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("crisisData");

        // Recuperar os dados de crises
        crisisRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Verifica se existem dados de crises
                DataSnapshot dataSnapshot = task.getResult();
                if (dataSnapshot.exists()) {
                    // Carrega os dados de crises para calcular a soma e média
                    for (DataSnapshot crisisSnapshot : dataSnapshot.getChildren()) {
                        CrisisData crisisData = crisisSnapshot.getValue(CrisisData.class);
                        if (crisisData != null) {
                            crisisCount++; // Contabiliza a crise
                            totalCrisisTime += crisisData.getDuration(); // Soma a duração da crise
                        }
                    }
                    Log.d("ChronometerActivity", "Número de crises: " + crisisCount);
                    if (crisisCount > 0) {
                        long averageTime = totalCrisisTime / crisisCount; // Média das crises
                        Log.d("ChronometerActivity", "Tempo médio das crises: " + averageTime);
                    }
                } else {
                    Log.d("ChronometerActivity", "Nenhuma crise registrada.");
                }
            } else {
                Log.e("ChronometerActivity", "Erro ao carregar dados de crises.");
            }
        });
    }

    private void stopAndSaveData() {
        chronometer.stop();
        long duration = SystemClock.elapsedRealtime() - chronometer.getBase();
        isRunning = false;

        // Atualiza os dados de crise
        lastCrisisTime = duration / 1000; // Convertendo para segundos
        totalCrisisTime += lastCrisisTime; // Soma a duração da crise
        crisisCount++; // Incrementa o contador de crises
        long averageTime = totalCrisisTime / crisisCount;  // Calcula a média de todas as crises registradas

        // Salva os dados no Firebase
        saveDataToFirebase(lastCrisisTime, averageTime);

        // Reseta o cronômetro
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
        startStopButton.setText("Start");

        Toast.makeText(this, "Cronômetro parado e dados salvos", Toast.LENGTH_SHORT).show();
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

    private void saveDataToFirebase(long duration, long averageTime) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.e("ChronometerActivity", "Usuário não autenticado.");
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calcular o ano, mês e dia da crise
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Janeiro é 0, então adicionamos 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Obter a referência de "crises" para este usuário
        DatabaseReference crisisRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("crisisData")
                .child(String.valueOf(year))
                .child(String.valueOf(month))
                .child(String.valueOf(day));

        // Usar push() para gerar um ID único para a crise
        DatabaseReference crisisEntryRef = crisisRef.push();

        // Cria o objeto de dados da crise
        CrisisData crisisData = new CrisisData(System.currentTimeMillis(), duration, (int) crisisCount, averageTime, System.currentTimeMillis());

        // Salva os dados da crise
        crisisEntryRef.setValue(crisisData)
                .addOnSuccessListener(aVoid -> {
                    updateCrisisCount(userId); // Atualiza a contagem no Firebase após a crise ser salva
                    Toast.makeText(this, "Dados salvos com sucesso.", Toast.LENGTH_SHORT).show();
                    Log.d("ChronometerActivity", "Dados da crise salvos no Firebase.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao salvar dados.", Toast.LENGTH_SHORT).show();
                    Log.e("ChronometerActivity", "Erro ao salvar dados: " + e.getMessage());
                });
    }

    private void updateCrisisCount(String userId) {
        DatabaseReference crisisCountRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("crisisCount");

        crisisCountRef.setValue(crisisCount); // Atualiza o número total de crises
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
                Log.e("ChronometerActivity", "Erro ao obter caregiverId.");
            }
        });
    }

    private void sendSmsToCaregiver(String phoneNumber) {
        String message = "A crise do paciente foi registrada. Duração: " + lastCrisisTime + " segundos.";

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Log.d("ChronometerActivity", "Mensagem SMS enviada ao cuidador: " + phoneNumber);
    }
}
