package com.example.myapplication.activities;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class MedicalData extends AppCompatActivity {

    private TextView crisisCountTextView;
    private TextView averageTimeTextView;
    private TextView lastCrisisTimeTextView;  // Adicionando o TextView para a última crise
    private DatabaseReference databaseReference;
    private List<CrisisData> crisisDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_data);

        crisisCountTextView = findViewById(R.id.tvCrisisCount);
        averageTimeTextView = findViewById(R.id.tvAverageTime);
        lastCrisisTimeTextView = findViewById(R.id.tvLastCrisisTime);  // Inicializando o TextView da última crise
        databaseReference = FirebaseDatabase.getInstance().getReference("crisisData");

        crisisDataList = new ArrayList<>();

        // Carregar dados das crises
        loadCrisisData();
    }

    private void loadCrisisData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                crisisDataList.clear();  // Limpa a lista de crises

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CrisisData crisisData = snapshot.getValue(CrisisData.class);
                    if (crisisData != null) {
                        crisisDataList.add(crisisData);
                    }
                }

                // Exibir os dados
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MedicalData.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        int totalCrisisCount = 0;
        long totalDuration = 0;
        long totalAverageTime = 0;
        long lastCrisisTime = 0;  // Variável para armazenar o tempo da última crise

        for (CrisisData crisisData : crisisDataList) {
            totalCrisisCount += crisisData.getCrisisCount();
            totalDuration += crisisData.getDuration();
            totalAverageTime += crisisData.getAverageTime();

            // Encontrando o tempo da última crise
            if (crisisData.getLastCrisisTime() > lastCrisisTime) {
                lastCrisisTime = crisisData.getLastCrisisTime();
            }
        }

        // Atualizando os TextViews
        crisisCountTextView.setText("Total de Crises: " + totalCrisisCount);
        if (crisisDataList != null && crisisDataList.size() > 0) {
            averageTimeTextView.setText("Tempo Médio: " + (totalAverageTime / crisisDataList.size()) + " segundos");
        }
        else{
            averageTimeTextView.setText("Tempo Médio: " + totalAverageTime + " segundos");
        }
        // Exibindo o tempo da última crise
        if (lastCrisisTime > 0) {
            lastCrisisTimeTextView.setText("Última Crise: " + formatTimestamp(lastCrisisTime));
        } else {
            lastCrisisTimeTextView.setText("Última Crise: Nenhuma registrada");
        }
    }

    // Método para formatar o timestamp em uma data legível
    private String formatTimestamp(long timestamp) {
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        java.util.Date date = new java.util.Date(timestamp);
        return dateFormat.format(date);
    }
}
