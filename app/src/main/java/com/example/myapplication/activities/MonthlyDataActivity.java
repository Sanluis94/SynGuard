package com.example.myapplication.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.MedicalDataAdapter;
import com.example.myapplication.models.MedicalData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MonthlyDataActivity extends AppCompatActivity {

    private RecyclerView rvMonthlyData;
    private MedicalDataAdapter adapter;
    private ArrayList<MedicalData> medicalDataList;
    private DatabaseReference monthlyDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_data);

        rvMonthlyData = findViewById(R.id.rvMonthlyData);
        rvMonthlyData.setLayoutManager(new LinearLayoutManager(this));

        medicalDataList = new ArrayList<>();
        adapter = new MedicalDataAdapter(medicalDataList);
        rvMonthlyData.setAdapter(adapter);

        // Firebase reference
        String patientId = "PatientID"; // Substituir pelo ID do paciente real
        monthlyDataRef = FirebaseDatabase.getInstance().getReference("MonthlyData").child(patientId);

        loadMonthlyData();
    }

    private void loadMonthlyData() {
        monthlyDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicalDataList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MedicalData medicalData = dataSnapshot.getValue(MedicalData.class);
                    if (medicalData != null) {
                        medicalDataList.add(medicalData);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MonthlyDataActivity.this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
