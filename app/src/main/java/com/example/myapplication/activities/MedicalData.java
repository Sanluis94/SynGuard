package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.CrisisData;
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

        // Referência ao Firebase
        String patientId = "PatientID"; // Substituir pelo ID do paciente real
        crisisDataRef = FirebaseDatabase.getInstance().getReference("CrisisData").child(patientId);

        // Recuperar os dados do Firebase
        loadMedicalData();

        // Botão para voltar ao menu
        btnBackToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(MedicalData.this, PatientMenu.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadMedicalData() {
        crisisDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Obter os dados de CrisisData do Firebase
                    CrisisData crisisData = snapshot.getValue(CrisisData.class);
                    if (crisisData != null) {
                        tvCrisisCount.setText("Número de crises: " + crisisData.getCrisisCount());
                        tvAverageTime.setText("Tempo médio das crises: " + formatTime(crisisData.getAverageTime()));
                        tvLastCrisisTime.setText("Tempo da última crise: " + formatTime(crisisData.getLastCrisisTime()));
                    }
                } else {
                    Toast.makeText(MedicalData.this, "Nenhum dado encontrado.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
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
