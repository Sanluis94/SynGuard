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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicalData extends AppCompatActivity {

    private TextView crisisCountTextView;
    private TextView averageTimeTextView;
    private TextView lastCrisisTimeTextView;
    private TextView noDataTextView;
    private DatabaseReference databaseReference;
    private List<CrisisData> crisisDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_data);

        crisisCountTextView = findViewById(R.id.tvCrisisCount);
        averageTimeTextView = findViewById(R.id.tvAverageTime);
        lastCrisisTimeTextView = findViewById(R.id.tvLastCrisisTime);
        noDataTextView = findViewById(R.id.tvNoData);
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        crisisDataList = new ArrayList<>();

        loadCrisisData();
    }

    private void loadCrisisData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String caregiverId = dataSnapshot.child("caregiverId").getValue(String.class);
                    if (caregiverId != null) {
                        loadPatientData(caregiverId);  // Carregar dados do paciente associado ao cuidador
                    } else {
                        loadOwnData(userId);  // Carregar dados do próprio paciente
                    }
                } else {
                    Toast.makeText(MedicalData.this, "Erro: dados do usuário não encontrados.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MedicalData.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOwnData(String userId) {
        DatabaseReference crisisRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("crisisData");

        crisisRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    crisisDataList.clear(); // Limpar a lista antes de adicionar os novos dados
                    for (DataSnapshot crisisSnapshot : dataSnapshot.getChildren()) {
                        CrisisData crisisData = crisisSnapshot.getValue(CrisisData.class);
                        if (crisisData != null) {
                            crisisDataList.add(crisisData);
                        }
                    }
                    updateUI();
                } else {
                    noDataTextView.setText("Sem dados de crise para este paciente.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MedicalData.this, "Erro ao carregar dados de crises", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatientData(String caregiverId) {
        Query patientRef = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("caregiverId")
                .equalTo(caregiverId);  // Buscar pacientes pelo caregiverId

        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    crisisDataList.clear(); // Limpar a lista antes de adicionar os novos dados
                    for (DataSnapshot patientSnapshot : dataSnapshot.getChildren()) {
                        String patientId = patientSnapshot.getKey();
                        DatabaseReference crisisRef = FirebaseDatabase.getInstance().getReference("users")
                                .child(patientId)
                                .child("crisisData");

                        crisisRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot crisisSnapshot) {
                                if (crisisSnapshot.exists()) {
                                    for (DataSnapshot crisisDataSnapshot : crisisSnapshot.getChildren()) {
                                        CrisisData crisisData = crisisDataSnapshot.getValue(CrisisData.class);
                                        if (crisisData != null) {
                                            crisisDataList.add(crisisData);
                                        }
                                    }
                                    updateUI();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(MedicalData.this, "Erro ao carregar dados de crises", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    noDataTextView.setText("Sem pacientes associados.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MedicalData.this, "Erro ao carregar pacientes", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateUI() {
        int totalCrisisCount = 0;
        long totalDuration = 0;
        long lastCrisisTime = 0;
        String formattedTimestamp = "";

        for (CrisisData crisisData : crisisDataList) {
            totalCrisisCount += crisisData.getCrisisCount();
            totalDuration += crisisData.getDuration();
            lastCrisisTime = Math.max(lastCrisisTime, crisisData.getLastCrisisTime());
            formattedTimestamp = crisisData.getFormattedTimestamp();

        }

        crisisCountTextView.setText("Total de Crises: " + totalCrisisCount);
        averageTimeTextView.setText("Tempo Médio das Crises: " + totalDuration + " segundos" );

        lastCrisisTimeTextView.setText("Última Crise: " + formattedTimestamp);

        noDataTextView.setText(crisisDataList.isEmpty() ? "Sem dados de crise para este paciente." : "");
    }

}
