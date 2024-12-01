package com.example.myapplication.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.CrisisData;
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

        // Obtenção do ID do paciente e do cuidador
        String caregiverId = "caregiverId123"; // ID do cuidador, pode ser obtido a partir do login
        String patientId = "patientId456"; // ID do paciente, pode ser obtido a partir do login

        // Firebase reference
        crisisDataRef = FirebaseDatabase.getInstance().getReference("users")
                .child(caregiverId) // ID do cuidador
                .child("patients")
                .child(patientId) // ID do paciente
                .child("crisisData");

        loadGraphData();
    }

    private void loadGraphData() {
        crisisDataRef.orderByKey().limitToLast(1) // Pega o último dado de crise com base no timestamp
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<BarEntry> barEntries = new ArrayList<>();
                        List<Entry> lineEntries = new ArrayList<>();
                        int index = 0;

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String monthYear = dataSnapshot.getKey();  // Obtém a chave (timestamp)
                            CrisisData crisisData = dataSnapshot.getValue(CrisisData.class);

                            if (crisisData != null) {
                                // Adicionando entradas para o gráfico de barras (crises por mês)
                                barEntries.add(new BarEntry(index, crisisData.getCrisisCount()));

                                // Adicionando entradas para o gráfico de linha (tempo médio das crises)
                                float averageTimeInMinutes = (float) crisisData.getAverageTime() / 60;  // Convertendo de segundos para minutos
                                lineEntries.add(new Entry(index, averageTimeInMinutes));

                                index++;
                            }
                        }

                        // Atualizando os gráficos
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

        barChartCrises.invalidate();
    }

    private void updateLineChart(List<Entry> lineEntries) {
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Tempo Médio (minutos)");
        lineDataSet.setColor(getResources().getColor(R.color.teal_200));
        lineDataSet.setCircleColor(getResources().getColor(R.color.teal_200));

        LineData lineData = new LineData(lineDataSet);
        lineChartAverageTime.setData(lineData);

        Description description = new Description();
        description.setText("Tempo Médio das Crises");
        lineChartAverageTime.setDescription(description);

        lineChartAverageTime.invalidate();
    }
}
