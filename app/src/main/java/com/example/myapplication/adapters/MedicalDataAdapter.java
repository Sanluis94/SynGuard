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
        holder.tvMonthYear.setText(medicalData.getMonthYear());
        holder.tvCrisisCount.setText("Crises: " + medicalData.getCrisisCount());
        holder.tvAverageTime.setText("Média: " + medicalData.getAverageTime() + " mins");
    }

    @Override
    public int getItemCount() {
        return medicalDataList.size();
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
