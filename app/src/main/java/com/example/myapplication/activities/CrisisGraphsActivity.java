package com.example.myapplication.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.CrisisData;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CrisisGraphsActivity extends AppCompatActivity {

    private BarChart barChartCrises;
    private List<CrisisData> crisisDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crisis_graphs);

        barChartCrises = findViewById(R.id.barChartCrises);  // Alterado para BarChart
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
                        loadPatientData(caregiverId);  // Carregar dados dos pacientes
                    } else {
                        loadOwnData(userId);  // Carregar dados do próprio paciente
                    }
                } else {
                    Toast.makeText(CrisisGraphsActivity.this, "Erro: dados do usuário não encontrados.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CrisisGraphsActivity.this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
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
                    for (DataSnapshot yearSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot monthSnapshot : yearSnapshot.getChildren()) {
                            for (DataSnapshot crisisSnapshot : monthSnapshot.getChildren()) {
                                CrisisData crisisData = crisisSnapshot.getValue(CrisisData.class);
                                if (crisisData != null) {
                                    crisisDataList.add(crisisData);
                                }
                            }
                        }
                    }
                    sortAndUpdateGraph();  // Ordenar os dados e atualizar o gráfico
                } else {
                    Toast.makeText(CrisisGraphsActivity.this, "Nenhum dado de crise encontrado.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CrisisGraphsActivity.this, "Erro ao carregar dados de crises", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatientData(String caregiverId) {
        Query patientRef = FirebaseDatabase.getInstance().getReference("users")
                .orderByChild("caregiverId")
                .equalTo(caregiverId);  // Isso retorna um Query, que é correto.

        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
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
                                    sortAndUpdateGraph();  // Ordenar os dados e atualizar o gráfico
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(CrisisGraphsActivity.this, "Erro ao carregar dados de crises", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CrisisGraphsActivity.this, "Erro ao carregar pacientes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortAndUpdateGraph() {
        // Ordenar a lista de crises por timestamp (do mais recente para o mais antigo)
        Collections.sort(crisisDataList, new Comparator<CrisisData>() {
            @Override
            public int compare(CrisisData crisis1, CrisisData crisis2) {
                return Long.compare(crisis2.getTimestamp(), crisis1.getTimestamp());  // Ordenar do mais recente
            }
        });

        // Criar uma lista de entradas para o gráfico
        List<BarEntry> entries = new ArrayList<>();

        long totalCrises = 0;
        long totalDuration = 0;

        for (int i = 0; i < crisisDataList.size(); i++) {
            CrisisData crisis = crisisDataList.get(i);
            totalCrises += crisis.getCrisisCount(); // Somando o número de crises
            totalDuration += crisis.getDuration(); // Somando a duração de todas as crises

            // Calculando o tempo médio até o ponto atual (média acumulada de tempo)
            long averageTime = totalDuration / totalCrises;

            // Adicionando ao gráfico: eixo X = número de crises e eixo Y = tempo médio
            entries.add(new BarEntry(totalCrises, averageTime));  // (número de crises, tempo médio)
        }

        // Criando o dataset com as entradas para o gráfico
        BarDataSet dataSet = new BarDataSet(entries, "Número de Crises vs Tempo Médio");
        dataSet.setColor(getResources().getColor(R.color.primaryColor)); // Escolher a cor para a barra
        dataSet.setValueTextColor(getResources().getColor(R.color.accentColor)); // Cor dos valores

        // Criando o BarData e aplicando no gráfico
        BarData barData = new BarData(dataSet);
        barChartCrises.setData(barData);
        barChartCrises.invalidate();  // Atualiza o gráfico

        // Se necessário, você pode configurar a aparência do gráfico aqui (como títulos, eixos, etc.)
        barChartCrises.getDescription().setEnabled(false);  // Desabilita descrição padrão

        // Configurações do eixo X
        barChartCrises.getXAxis().setGranularity(1f);  // Define a granulação no eixo X
        barChartCrises.getXAxis().setLabelCount(5, true);  // Número de rótulos no eixo X
        barChartCrises.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "Crises: " + (int) value;  // Adicionando rótulo "Crises: X"
            }
        });

        // Configurações do eixo Y
        barChartCrises.getAxisLeft().setGranularity(1f);  // Granularidade no eixo Y
        barChartCrises.getAxisLeft().setAxisMinimum(0f);  // Define o valor mínimo do eixo Y
        barChartCrises.getAxisLeft().setLabelCount(5, true); // Número de rótulos no eixo Y
        barChartCrises.getAxisLeft().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f s", value);  // Exibe o valor em segundos
            }
        });

        // Definindo os rótulos de cada eixo
        barChartCrises.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Posição do eixo X
        barChartCrises.getXAxis().setLabelRotationAngle(45f);  // Rotacionar os rótulos do eixo X para melhor visualização
        barChartCrises.getAxisLeft().setDrawLabels(true);
        barChartCrises.getAxisLeft().setLabelCount(6, true); // Número de rótulos no eixo Y
        barChartCrises.getAxisRight().setEnabled(false); // Desabilita o eixo Y direito

        // Adicionando título ao gráfico
        barChartCrises.getLegend().setEnabled(false); // Se não quiser legenda
    }
}
