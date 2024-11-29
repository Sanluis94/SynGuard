package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            navigateToAppropriateMenu();
        } else {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
            finish();
        }
    }

    private void navigateToAppropriateMenu() {
        String currentUserId = FirebaseUtils.getCurrentUserId();
        if (currentUserId != null) {
            // Placeholder logic: Replace with actual user role determination
            boolean isCaregiver = determineUserRole(currentUserId);

            Intent intent;
            if (isCaregiver) {
                intent = new Intent(MainActivity.this, CaregiverMenu.class);
            } else {
                intent = new Intent(MainActivity.this, PatientMenu.class);
            }
            startActivity(intent);
            finish();
        }
    }

    private boolean determineUserRole(String userId) {
        // Implement Firebase call to check user role
        return false; // Placeholder for caregiver
    }
}
