package com.example.myapplication.activities;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

public class MedicalData extends AppCompatActivity {

    private TextView crisisCountTextView;
    private TextView averageTimeTextView;
    private TextView lastCrisisTimeTextView;
    private DatabaseReference databaseReference;
    private List<CrisisData> crisisDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_data);

        crisisCountTextView = findViewById(R.id.tvCrisisCount);
        averageTimeTextView = findViewById(R.id.tvAverageTime);
        lastCrisisTimeTextView = findViewById(R.id.tvLastCrisisTime);
        databaseReference = FirebaseDatabase.getInstance().getReference("users"); // "users" é a raiz que contém todos os dados

        crisisDataList = new ArrayList<>();

        // Carregar dados das crises
        loadCrisisData();
    }

    private void loadCrisisData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userEmail = mAuth.getCurrentUser().getEmail();

        if (userEmail == null) {
            Toast.makeText(MedicalData.this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Referência Firebase para acessar os dados de usuários
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Realiza uma consulta para encontrar o usuário com o e-mail atual
        usersRef.orderByChild("email").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String caregiverId = snapshot.child("caregiverId").getValue(String.class);
                                String patientId = snapshot.getKey(); // O ID do paciente é a chave do nó

                                // Acessando dados de crises para o paciente
                                loadPatientCrisisData(caregiverId, patientId);
                            }
                        } else {
                            Toast.makeText(MedicalData.this, "Usuário não encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MedicalData.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPatientCrisisData(String caregiverId, String patientId) {
        if (caregiverId == null || patientId == null) {
            Toast.makeText(MedicalData.this, "Dados de paciente ou cuidador não encontrados", Toast.LENGTH_SHORT).show();
            return;
        }

        // Referência Firebase para acessar dados de crises do paciente
        DatabaseReference crisisDataRef = FirebaseDatabase.getInstance().getReference("users")
                .child(caregiverId) // Acesso ao cuidador
                .child("patients") // Acesso aos pacientes associados
                .child(patientId) // Acesso ao paciente específico
                .child("crisisData"); // Dados das crises

        // Carregar os dados de crises ordenados pela data (timestamp)
        crisisDataRef.orderByChild("timestamp") // Ordena pela data da crise (timestamp)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        crisisDataList.clear();

                        // Verifica se o snapshot existe
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Long timestamp = snapshot.child("timestamp").getValue(Long.class);
                                Long duration = snapshot.child("duration").getValue(Long.class);
                                Integer crisisCount = snapshot.child("crisisCount").getValue(Integer.class);
                                Long averageTime = snapshot.child("averageTime").getValue(Long.class);
                                Long lastCrisisTime = snapshot.child("lastCrisisTime").getValue(Long.class);

                                // Verifique se os dados estão sendo obtidos corretamente
                                if (timestamp != null && duration != null && crisisCount != null && averageTime != null && lastCrisisTime != null) {
                                    // Verifique se crisisCount é maior que 0 antes de fazer qualquer divisão
                                    if (crisisCount > 0) {
                                        // Calcule o averageTime com base no número de crises
                                        averageTime = duration / crisisCount;
                                    } else {
                                        // Se crisisCount for zero, defina uma média padrão (ou 0)
                                        averageTime = 0L;
                                    }

                                    CrisisData crisisData = new CrisisData(timestamp, duration, crisisCount, averageTime, lastCrisisTime);
                                    crisisDataList.add(crisisData);
                                }
                            }

                            // Atualize a UI com os dados mais recentes
                            updateUI();
                        } else {
                            Toast.makeText(MedicalData.this, "Sem dados de crise para este paciente", Toast.LENGTH_SHORT).show();
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

        for (CrisisData crisisData : crisisDataList) {
            totalCrisisCount += crisisData.getCrisisCount();
            totalDuration += crisisData.getDuration();
            totalAverageTime += crisisData.getAverageTime();
            lastCrisisTime = crisisData.getLastCrisisTime();
        }

        // Atualiza as TextViews com os valores
        crisisCountTextView.setText("Total de Crises: " + totalCrisisCount);
        averageTimeTextView.setText("Tempo Médio das Crises: " + (totalAverageTime / totalCrisisCount) + " segundos");
        lastCrisisTimeTextView.setText("Última Crise: " + lastCrisisTime);
    }
}
