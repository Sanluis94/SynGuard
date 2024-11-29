package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.CronometerService;

public class PatientMenu extends AppCompatActivity {

    private Button startChronometerButton, logoutButton, viewMedicalDataButton;
    private TextView patientIdView, caregiverIdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_menu);

        initializeViews();

        startChronometerButton.setOnClickListener(v -> {
            Intent intent = new Intent(PatientMenu.this, ChronometerActivity.class);
            startActivity(intent);
        });

        viewMedicalDataButton.setOnClickListener(v -> {
            Intent intent = new Intent(PatientMenu.this, CrisisDetails.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            // Perform logout
            finish();
        });
    }

    private void initializeViews() {
        startChronometerButton = findViewById(R.id.startChronometerButton);
        logoutButton = findViewById(R.id.logoutButton);
        viewMedicalDataButton = findViewById(R.id.viewMedicalDataButton);
        patientIdView = findViewById(R.id.patientIdView);
        caregiverIdView = findViewById(R.id.caregiverIdView);

        // Placeholder for real data
        patientIdView.setText("Patient ID: 123456");
        caregiverIdView.setText("Caregiver ID: ABCD12");
    }
}
