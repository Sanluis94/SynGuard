package com.example.myapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CaregiverMenu extends Activity {

    private TextView tvCaregiverId, tvNotification;
    private Button btnViewMedicalData, btnViewGraphs, btnLogout;
    private String caregiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver_menu);

        // Inicializando os componentes
        tvCaregiverId = findViewById(R.id.tvCaregiverId);
        tvNotification = findViewById(R.id.tvNotification);
        btnViewMedicalData = findViewById(R.id.btnViewMedicalData);
        btnViewGraphs = findViewById(R.id.btnViewGraphs);
        btnLogout = findViewById(R.id.btnLogout);

        // Obter ID do cuidador (assumindo que ele foi salvo durante o login)
        caregiverId = FirebaseAuth.getInstance().getUid();
        tvCaregiverId.setText("ID Cuidador: " + caregiverId);

        // Configurando listener de notificação para o cronômetro
        listenForCronometerStart();

        // Ações dos botões
        btnViewMedicalData.setOnClickListener(v -> {
            Intent intent = new Intent(CaregiverMenu.this, CrisisDetails.class); // Tela com os dados médicos
            startActivity(intent);
        });

        btnViewGraphs.setOnClickListener(v -> {
            Intent intent = new Intent(CaregiverMenu.this, CrisisDetails.class); // Tela de gráficos
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(CaregiverMenu.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    private void listenForCronometerStart() {
        FirebaseDatabase.getInstance().getReference("CronometerStatus")
                .child(caregiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Boolean isRunning = snapshot.getValue(Boolean.class);
                            if (isRunning != null && isRunning) {
                                tvNotification.setText("Aviso: O cronômetro foi iniciado!");
                                Toast.makeText(CaregiverMenu.this, "O paciente iniciou o cronômetro.", Toast.LENGTH_SHORT).show();
                            } else {
                                tvNotification.setText("");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CaregiverMenu.this, "Erro ao verificar status do cronômetro.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
