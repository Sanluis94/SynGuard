package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.MedicalData;

import java.util.List;

public class MedicalDataAdapter extends RecyclerView.Adapter<MedicalDataAdapter.MedicalDataViewHolder> {

    private final List<MedicalData> medicalDataList;

    public MedicalDataAdapter(List<MedicalData> medicalDataList) {
        this.medicalDataList = medicalDataList;
    }

    @NonNull
    @Override
    public MedicalDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record, parent, false);
        return new MedicalDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicalDataViewHolder holder, int position) {
        MedicalData medicalData = medicalDataList.get(position);

        holder.monthYear.setText(medicalData.getMonthYear());
        holder.totalCrises.setText(String.valueOf(medicalData.getTotalCrises()));
        holder.averageDuration.setText(String.valueOf(medicalData.getAverageDuration()));
    }

    @Override
    public int getItemCount() {
        return medicalDataList.size();
    }

    static class MedicalDataViewHolder extends RecyclerView.ViewHolder {

        TextView monthYear, totalCrises, averageDuration;

        public MedicalDataViewHolder(@NonNull View itemView) {
            super(itemView);

            monthYear = itemView.findViewById(R.id.monthYear);
            totalCrises = itemView.findViewById(R.id.totalCrises);
            averageDuration = itemView.findViewById(R.id.averageDuration);
        }
    }
}
