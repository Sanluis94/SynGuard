package com.example.myapplication.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CrisisGraphsActivity extends AppCompatActivity {

    private BarChart barChartCrises;
    private LineChart lineChartAverageTime;
    private DatabaseReference crisisDataRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crisis_graphs);

        barChartCrises = findViewById(R.id.barChartCrises);
        lineChartAverageTime = findViewById(R.id.lineChartAverageTime);

        // Firebase reference
        String patientId = "PatientID"; // Substituir pelo ID do paciente real
        crisisDataRef = FirebaseDatabase.getInstance().getReference("CrisisData").child(patientId);

        loadGraphData();
    }

    private void loadGraphData() {
        crisisDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<BarEntry> barEntries = new ArrayList<>();
                List<Entry> lineEntries = new ArrayList<>();
                int index = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String monthYear = dataSnapshot.getKey();
                    Long crisisCount = dataSnapshot.child("crisisCount").getValue(Long.class);
                    Double averageTime = dataSnapshot.child("averageTime").getValue(Double.class);

                    if (crisisCount != null && averageTime != null) {
                        barEntries.add(new BarEntry(index, crisisCount));
                        lineEntries.add(new Entry(index, averageTime.floatValue()));
                        index++;
                    }
                }

                updateBarChart(barEntries);
                updateLineChart(lineEntries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CrisisGraphsActivity.this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBarChart(List<BarEntry> barEntries) {
        BarDataSet barDataSet = new BarDataSet(barEntries, "Número de Crises");
        barDataSet.setColor(getResources().getColor(R.color.purple_200));
        BarData barData = new BarData(barDataSet);
        barChartCrises.setData(barData);

        Description description = new Description();
        description.setText("Número de Crises por Mês");
        barChartCrises.setDescription(description);

        barChartCrises.invalidate(); // Atualiza o gráfico
    }

    private void updateLineChart(List<Entry> lineEntries) {
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Tempo Médio (minutos)");
        lineDataSet.setColor(getResources().getColor(R.color.teal_200));
        lineDataSet.setCircleColor(getResources().getColor(R.color.teal_700));
        lineDataSet.setValueTextSize(10f);

        LineData lineData = new LineData(lineDataSet);
        lineChartAverageTime.setData(lineData);

        Description description = new Description();
        description.setText("Tempo Médio das Crises por Mês");
        lineChartAverageTime.setDescription(description);

        lineChartAverageTime.invalidate(); // Atualiza o gráfico
    }
}