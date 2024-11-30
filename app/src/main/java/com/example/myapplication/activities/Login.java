package com.example.myapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button loginButton, registerButton, forgotPasswordButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        loginButton.setOnClickListener(v -> loginUser());

        registerButton.setOnClickListener(v -> navigateToRegister());

        forgotPasswordButton.setOnClickListener(v -> navigateToForgotPassword());
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(Login.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Verifica o tipo de usuário
                        String userId = mAuth.getCurrentUser().getUid();
                        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String userType = dataSnapshot.child("userType").getValue(String.class);
                                if ("cuidador".equals(userType)) {
                                    navigateToCaregiverMenu();
                                } else if ("paciente".equals(userType)) {
                                    navigateToPatientMenu();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(Login.this, "Erro ao verificar o tipo de usuário", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(Login.this, "Falha no login: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToCaregiverMenu() {
        Intent intent = new Intent(Login.this, CaregiverMenu.class);
        startActivity(intent);
        finish();
    }

    private void navigateToPatientMenu() {
        Intent intent = new Intent(Login.this, PatientMenu.class);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);
    }

    private void navigateToForgotPassword() {
        Intent intent = new Intent(Login.this, ForgotPassword.class);
        startActivity(intent);
    }
}
