package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.CrisisData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MedicalData extends AppCompatActivity {

    private TextView tvCrisisCount, tvAverageTime, tvLastCrisisTime;
    private Button btnBackToMenu;
    private DatabaseReference crisisDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_data);

        // Referências para os elementos da interface
        tvCrisisCount = findViewById(R.id.tvCrisisCount);
        tvAverageTime = findViewById(R.id.tvAverageTime);
        tvLastCrisisTime = findViewById(R.id.tvLastCrisisTime);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        // Obter ID do paciente logado
        String patientId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (patientId == null) {
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Referência ao nó de dados do paciente no Firebase
        crisisDataRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(patientId)
                .child("crisisData");

        // Recuperar os dados médicos
        loadMedicalData();

        // Botão para voltar ao menu
        btnBackToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(MedicalData.this, PatientMenu.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadMedicalData() {
        // Adicionar listener para recuperar dados do nó 'crisisData'
        crisisDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    long totalDuration = 0;
                    int crisisCount = 0;
                    long lastCrisisTime = 0;

                    // Iterar pelos registros de crise
                    for (DataSnapshot crisisSnapshot : snapshot.getChildren()) {
                        CrisisData crisisData = crisisSnapshot.getValue(CrisisData.class);
                        if (crisisData != null) {
                            totalDuration += crisisData.getDuration();
                            crisisCount++;
                            lastCrisisTime = Math.max(lastCrisisTime, crisisData.getLastCrisisTime());
                        }
                    }

                    // Atualizar a interface com os dados calculados
                    long averageTime = crisisCount > 0 ? totalDuration / crisisCount : 0;
                    tvCrisisCount.setText("Número de crises: " + crisisCount);
                    tvAverageTime.setText("Tempo médio das crises: " + formatTime(averageTime));
                    tvLastCrisisTime.setText("Tempo da última crise: " + formatTime(lastCrisisTime));
                } else {
                    Toast.makeText(MedicalData.this, "Nenhum dado encontrado.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MedicalData.this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
