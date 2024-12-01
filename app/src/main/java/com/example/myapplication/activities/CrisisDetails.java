package com.example.myapplication.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.CrisisData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CrisisDetails extends AppCompatActivity {

    private TextView crisisDateView, crisisDurationView;
    private static final String TAG = "CrisisDetails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crisis_details);

        crisisDateView = findViewById(R.id.crisisDateView);
        crisisDurationView = findViewById(R.id.crisisDurationView);

        // Recuperar detalhes da crise do Firebase
        fetchCrisisDetails();
    }

    private void fetchCrisisDetails() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.e(TAG, "Usuário não autenticado.");
            Toast.makeText(this, "Erro: usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference crisisDataRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("crisisData");

        crisisDataRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot crisisSnapshot : snapshot.getChildren()) {
                        CrisisData crisisData = crisisSnapshot.getValue(CrisisData.class);
                        if (crisisData != null) {
                            crisisDateView.setText(String.format("Date: %s", crisisData.getTimestamp()));
                            crisisDurationView.setText(String.format("Duration: %d seconds", crisisData.getDuration()));
                        }
                    }
                } else {
                    Toast.makeText(CrisisDetails.this, "Nenhum dado de crise encontrado.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Erro ao recuperar dados: " + error.getMessage());
                Toast.makeText(CrisisDetails.this, "Erro ao recuperar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
