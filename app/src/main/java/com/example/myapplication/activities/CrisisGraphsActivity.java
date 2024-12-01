package com.example.myapplication.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.CrisisData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CrisisGraphsActivity extends AppCompatActivity {

    private LineChart lineChartCrises;
    private List<CrisisData> crisisDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crisis_graphs);

        lineChartCrises = findViewById(R.id.lineChartCrises);
        crisisDataList = new ArrayList<>();

        // Carregar dados das crises
        loadCrisisData();
    }

    private void loadCrisisData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Acessar o nó do Firebase que contém as crises do usuário
        DatabaseReference crisisRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("crisisData");

        // Escuta para os dados do Firebase com ano, mês e dia
        crisisRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot yearSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot monthSnapshot : yearSnapshot.getChildren()) {
                            for (DataSnapshot daySnapshot : monthSnapshot.getChildren()) {
                                for (DataSnapshot crisisSnapshot : daySnapshot.getChildren()) {
                                    CrisisData crisisData = crisisSnapshot.getValue(CrisisData.class);
                                    if (crisisData != null) {
                                        crisisDataList.add(crisisData);  // Adiciona os dados da crise à lista
                                    }
                                }
                            }
                        }
                    }
                    // Após carregar os dados, atualiza o gráfico
                    if (crisisDataList.isEmpty()) {
                        updateGraphWithNoData();
                    } else {
                        updateGraph();
                    }
                } else {
                    Toast.makeText(CrisisGraphsActivity.this, "Nenhuma crise registrada para este usuário", Toast.LENGTH_SHORT).show();
                    updateGraphWithNoData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CrisisGraphsActivity.this, "Erro ao carregar os dados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGraph() {
        // Listas para armazenar os dados de número de crises e tempo médio
        List<Entry> crisisEntries = new ArrayList<>();
        List<Entry> averageTimeEntries = new ArrayList<>();

        // Agora estamos utilizando a lista de crises carregada
        for (int i = 0; i < crisisDataList.size(); i++) {
            CrisisData crisis = crisisDataList.get(i);
            long crisisCount = crisis.getCrisisCount();
            long averageTime = crisis.getAverageTime();

            // Adiciona o número de crises (eixo X)
            crisisEntries.add(new Entry(i, crisisCount));
            // Adiciona o tempo médio das crises (eixo Y)
            averageTimeEntries.add(new Entry(i, averageTime));
        }

        // Criando os dados de linha para o gráfico
        LineDataSet crisisDataSet = new LineDataSet(crisisEntries, "Número de Crises");
        LineDataSet averageTimeDataSet = new LineDataSet(averageTimeEntries, "Tempo Médio (segundos)");

        // Definindo cores diferentes para as linhas
        crisisDataSet.setColor(getResources().getColor(R.color.primaryColor));
        averageTimeDataSet.setColor(getResources().getColor(R.color.accentColor));

        // Adicionando os dados ao gráfico
        LineData lineData = new LineData(crisisDataSet, averageTimeDataSet);
        lineChartCrises.setData(lineData);
        lineChartCrises.invalidate(); // Atualiza o gráfico

        // Definindo formatação para o valor mostrado nas linhas (opcional)
        crisisDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Formatar como inteiro para número de crises
            }
        });

        averageTimeDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Formatar como inteiro para tempo médio
            }
        });
    }

    private void updateGraphWithNoData() {
        // Caso não haja dados, exibe o gráfico vazio com uma mensagem
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 0)); // Exemplo: gráfico vazio com um valor de 0

        LineDataSet lineDataSet = new LineDataSet(entries, "Número de Crises");
        LineData lineData = new LineData(lineDataSet);

        lineChartCrises.setData(lineData);
        lineChartCrises.invalidate(); // Atualiza o gráfico

        Toast.makeText(this, "Sem dados de crises para exibir no gráfico", Toast.LENGTH_SHORT).show();
    }
}
