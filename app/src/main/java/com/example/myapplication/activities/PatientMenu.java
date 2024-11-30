package com.example.myapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.myapplication.R;
import com.example.myapplication.activities.MedicalData;
import com.google.firebase.auth.FirebaseAuth;

public class PatientMenu extends Activity {

    private Button btnStartChronometer, btnBluetooth, btnViewMedicalData, btnViewGraphs, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_menu);

        // Inicialização dos botões
        btnStartChronometer = findViewById(R.id.btnStartChronometer);
        btnBluetooth = findViewById(R.id.btnBluetooth);
        btnViewMedicalData = findViewById(R.id.btnViewMedicalData);
        btnViewGraphs = findViewById(R.id.btnViewGraphs);
        btnLogout = findViewById(R.id.btnLogout);

        // Ações dos botões
        btnStartChronometer.setOnClickListener(v -> {
            Intent intent = new Intent(PatientMenu.this, ChronometerActivity.class);
            startActivity(intent);
        });

        btnBluetooth.setOnClickListener(v -> {
            Intent intent = new Intent(PatientMenu.this, BluetoothDevices.class);
            startActivity(intent);
        });

        btnViewMedicalData.setOnClickListener(v -> {
            Intent intent = new Intent(PatientMenu.this, MedicalData.class);
            startActivity(intent);
        });

        btnViewGraphs.setOnClickListener(v -> {
            Intent intent = new Intent(PatientMenu.this, CrisisDetails.class); // Ou outra atividade responsável pelos gráficos
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(PatientMenu.this, Login.class);
            startActivity(intent);
            finish();
        });
    }
}
