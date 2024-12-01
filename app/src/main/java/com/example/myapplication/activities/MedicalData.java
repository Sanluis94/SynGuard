package com.example.myapplication.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.CrisisData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MedicalData extends AppCompatActivity {

    private TextView crisisCountTextView;
    private TextView averageTimeTextView;
    private TextView lastCrisisTimeTextView;
    private TextView noDataTextView;  // TextView para mostrar mensagem quando não houver dados
    private DatabaseReference databaseReference;
    private List<CrisisData> crisisDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_data);

        crisisCountTextView = findViewById(R.id.tvCrisisCount);
        averageTimeTextView = findViewById(R.id.tvAverageTime);
        lastCrisisTimeTextView = findViewById(R.id.tvLastCrisisTime);
        noDataTextView = findViewById(R.id.tvNoData);  // Adicionando TextView para mensagem de no data
        databaseReference = FirebaseDatabase.getInstance().getReference("users"); // "users" é a raiz que contém todos os dados

        crisisDataList = new ArrayList<>();

        // Carregar dados das crises
        loadCrisisData();
    }

    private void loadCrisisData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Acessar o nó do Firebase que contém as crises do usuário
        DatabaseReference crisisRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("crisisData");

        // Escuta para os dados do Firebase com ano, mês e dia
        crisisRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot yearSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot monthSnapshot : yearSnapshot.getChildren()) {
                            for (DataSnapshot daySnapshot : monthSnapshot.getChildren()) {
                                for (DataSnapshot crisisSnapshot : daySnapshot.getChildren()) {
                                    CrisisData crisisData = crisisSnapshot.getValue(CrisisData.class);
                                    if (crisisData != null) {
                                        crisisDataList.add(crisisData);  // Adiciona os dados da crise à lista
                                    }
                                }
                            }
                        }
                    }
                    updateUI();  // Atualiza a interface com os dados de crise
                } else {
                    crisisCountTextView.setText("Total de Crises: 0");
                    averageTimeTextView.setText("Tempo Médio das Crises: N/A");
                    lastCrisisTimeTextView.setText("Última Crise: N/A");
                    noDataTextView.setText("Sem dados de crise para este paciente.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MedicalData.this, "Erro ao carregar dados de crises", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        int totalCrisisCount = 0;
        long totalDuration = 0;
        long totalAverageTime = 0;
        long lastCrisisTime = 0;

        // Calculando as somas para total de crises, duração total e tempo médio
        for (CrisisData crisisData : crisisDataList) {
            totalCrisisCount = (int) crisisData.getCrisisCount();
            totalDuration = crisisData.getDuration();
            totalAverageTime = crisisData.getAverageTime();
            lastCrisisTime = Math.max(lastCrisisTime, crisisData.getLastCrisisTime()); // Pega o maior (último) tempo de crise
        }

        // Exibindo os resultados
        crisisCountTextView.setText("Total de Crises: " + totalCrisisCount);
        averageTimeTextView.setText("Tempo Médio das Crises: " + (totalCrisisCount > 0 ? (totalDuration / totalCrisisCount) + " segundos" : "N/A"));

        // Formatando a data da última crise
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String formattedDate = (lastCrisisTime > 0) ? sdf.format(new Date(lastCrisisTime)) : "N/A";
        lastCrisisTimeTextView.setText("Última Crise: " + formattedDate);

        // Caso não haja dados, mostrar a mensagem correspondente
        noDataTextView.setText(crisisDataList.isEmpty() ? "Sem dados de crise para este paciente." : "");
    }
}
