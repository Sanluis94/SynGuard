package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.models.MedicalData;
import com.example.myapplication.utils.FirebaseUtils;
import com.example.myapplication.R;

public class CaregiverMenu extends AppCompatActivity {

    private TextView caregiverIdView;
    private Button viewPatientDataButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver_menu);

        caregiverIdView = findViewById(R.id.caregiverIdView);
        viewPatientDataButton = findViewById(R.id.viewPatientDataButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Carregar o ID do cuidador
        String caregiverId = FirebaseUtils.getCurrentUserId();
        caregiverIdView.setText(String.format("Caregiver ID: %s", caregiverId));

        // Botão para visualizar dados do paciente
        viewPatientDataButton.setOnClickListener(view -> {
            Intent intent = new Intent(CaregiverMenu.this, MedicalData.class);
            startActivity(intent);
        });

        // Botão para logout
        logoutButton.setOnClickListener(view -> {
            FirebaseUtils.logout();
            Intent intent = new Intent(CaregiverMenu.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
