package com.example.myapplication.utils;

import android.util.Log;

import com.example.myapplication.models.CrisisData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {

    private static final String TAG = "FirebaseUtils";
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public static String getCurrentUserId() {
        if (firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().getUid();
        }
        return null;
    }

    public static void logSeizureData() {
        String userId = getCurrentUserId();
        if (userId != null) {
            long timestamp = System.currentTimeMillis();
            CrisisData crisisData = new CrisisData(timestamp, 0); // Atualizar com a duração correta quando o cronômetro parar

            DatabaseReference crisisRef = databaseReference.child("patients").child(userId).child("crises");
            String crisisKey = crisisRef.push().getKey();

            if (crisisKey != null) {
                crisisRef.child(crisisKey).setValue(crisisData)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Seizure data logged successfully"))
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to log seizure data", e));
            }
        }
    }

    public static void sendSmsToCaregiver(String caregiverPhone) {
        // Lógica para enviar SMS ao cuidador
        Log.d(TAG, "Sending SMS to caregiver at " + caregiverPhone);
        // Implementar integração com um serviço de envio de SMS, como Twilio ou API similar
    }

    public static void logout() {
        firebaseAuth.signOut();
    }
}
