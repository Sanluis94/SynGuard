package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.CrisisData;

public class CrisisDetails extends AppCompatActivity {

    private TextView crisisDateView, crisisDurationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crisis_details);

        crisisDateView = findViewById(R.id.crisisDateView);
        crisisDurationView = findViewById(R.id.crisisDurationView);

        // Recuperar os detalhes da crise
        CrisisData crisisData = (CrisisData) getIntent().getSerializableExtra("CRISIS_DATA");
        if (crisisData != null) {
            crisisDateView.setText(String.format("Date: %s", crisisData.getTimestamp()));
            crisisDurationView.setText(String.format("Duration: %d seconds", crisisData.getDuration()));
        }
    }
}
