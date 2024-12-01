package com.example.myapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class Register extends Activity {

    private EditText fullNameField, emailField, passwordField, confirmPasswordField, caregiverIdField, phoneNumberField;
    private Button registerButton;
    private RadioGroup userTypeGroup;
    private RadioButton patientRadioButton, caregiverRadioButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicialização dos campos de entrada
        fullNameField = findViewById(R.id.fullNameField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        caregiverIdField = findViewById(R.id.caregiverIdField);
        phoneNumberField = findViewById(R.id.phoneNumberInput);

        // Inicialização dos botões e radio buttons
        userTypeGroup = findViewById(R.id.userTypeGroup);
        patientRadioButton = findViewById(R.id.patientRadioButton);
        caregiverRadioButton = findViewById(R.id.caregiverRadioButton);

        registerButton = findViewById(R.id.registerButton);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Listener para o RadioGroup para controlar a visibilidade dos campos adicionais
        userTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.patientRadioButton) {
                caregiverIdField.setVisibility(View.VISIBLE);
                phoneNumberField.setVisibility(View.GONE); // Esconde o campo de telefone para pacientes
            } else if (checkedId == R.id.caregiverRadioButton) {
                caregiverIdField.setVisibility(View.GONE);
                phoneNumberField.setVisibility(View.VISIBLE); // Mostra o campo de telefone para cuidadores
            }
        });

        // Ao clicar no botão de registro
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String fullName = fullNameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        String userType = getUserType();
        String caregiverId = caregiverIdField.getText().toString().trim();
        String phoneNumber = phoneNumberField.getText().toString().trim();

        // Validação de campos obrigatórios
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)
                || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(Register.this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(Register.this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("paciente".equals(userType) && TextUtils.isEmpty(caregiverId)) {
            Toast.makeText(Register.this, "O ID do cuidador é obrigatório para pacientes!", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("cuidador".equals(userType)) {
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(Register.this, "O número de telefone é obrigatório para cuidadores!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
                Toast.makeText(Register.this, "Digite um número de telefone válido!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Criação do usuário no Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        User newUser = new User(fullName, email, userType,
                                "cuidador".equals(userType) ? generateCaregiverId() : caregiverId,
                                phoneNumber);

                        mDatabase.child("users").child(userId).setValue(newUser)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(Register.this, "Usuário registrado com sucesso!", Toast.LENGTH_LONG).show();
                                        navigateToLogin();
                                    } else {
                                        Toast.makeText(Register.this, "Erro ao salvar os dados do usuário.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(Register.this, "Falha na criação do usuário: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String generateCaregiverId() {
        Random random = new Random();
        StringBuilder caregiverId = new StringBuilder();

        // Gerar 4 letras aleatórias
        for (int i = 0; i < 4; i++) {
            caregiverId.append((char) ('A' + random.nextInt(26)));
        }

        // Gerar 2 números aleatórios
        caregiverId.append(random.nextInt(10)).append(random.nextInt(10));
        return caregiverId.toString();
    }

    private String getUserType() {
        if (patientRadioButton.isChecked()) {
            return "paciente";
        } else {
            return "cuidador";
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        finish();
    }
}
