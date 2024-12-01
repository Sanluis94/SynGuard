package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.MedicalData;

import java.util.ArrayList;

public class MedicalDataAdapter extends RecyclerView.Adapter<MedicalDataAdapter.ViewHolder> {

    private ArrayList<MedicalData> medicalDataList;

    public MedicalDataAdapter(ArrayList<MedicalData> medicalDataList) {
        this.medicalDataList = medicalDataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalData medicalData = medicalDataList.get(position);

        // Exibe o mês/ano da crise
        holder.tvMonthYear.setText(medicalData.getMonthYear() != null ? medicalData.getMonthYear() : "Desconhecido");

        // Exibe a quantidade de crises no mês, com verificação de valor
        int crisisCount = medicalData.getCrisisCount();
        holder.tvCrisisCount.setText(crisisCount > 0 ? "Crises: " + crisisCount : "Nenhuma crise");

        // Exibe o tempo médio das crises, com formatação adequada
        // Se o tempo médio estiver em segundos, converte para minutos
        double averageTime = medicalData.getAverageTime();
        if (averageTime > 0) {
            double averageTimeInMinutes = averageTime / 60.0;
            holder.tvAverageTime.setText(String.format("Média: %.2f mins", averageTimeInMinutes));
        } else {
            holder.tvAverageTime.setText("Média: -");
        }
    }

    @Override
    public int getItemCount() {
        return medicalDataList.size();
    }

    // Método para atualizar a lista de dados
    public void updateDataList(ArrayList<MedicalData> newData) {
        if (newData != null) {
            this.medicalDataList.clear(); // Limpar a lista antes de adicionar os novos dados
            this.medicalDataList.addAll(newData);
            notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonthYear, tvCrisisCount, tvAverageTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvMonthYear = itemView.findViewById(R.id.tvMonthYear);
            tvCrisisCount = itemView.findViewById(R.id.tvCrisisCount);
            tvAverageTime = itemView.findViewById(R.id.tvAverageTime);
        }
    }
}
