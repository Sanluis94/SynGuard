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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // Ao selecionar o tipo de usuário, exibe o campo de ID do cuidador e telefone do cuidador se for cuidador
        userTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.patientRadioButton) {
                caregiverIdField.setVisibility(View.VISIBLE);
                phoneNumberField.setVisibility(View.GONE); // Esconde o campo de telefone
            } else {
                caregiverIdField.setVisibility(View.GONE);
                phoneNumberField.setVisibility(View.VISIBLE); // Exibe o campo de telefone
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

        if ("cuidador".equals(userType) && TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(Register.this, "O número de telefone é obrigatório para cuidadores!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validação do formato do número de telefone apenas para cuidadores
        if ("cuidador".equals(userType) && !isPhoneNumberValid(phoneNumber)) {
            Toast.makeText(Register.this, "Número de telefone inválido! O formato correto é (xx) xxxxx-xxxx.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifica se o caregiverId existe no banco de dados para pacientes
        if ("paciente".equals(userType)) {
            mDatabase.child("users").orderByChild("caregiverId").equalTo(caregiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Criação do usuário no Firebase
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(Register.this, task -> {
                                    if (task.isSuccessful()) {
                                        String userId = mAuth.getCurrentUser().getUid();

                                        // Se for cuidador, gerar o caregiverId aleatório
                                        if ("cuidador".equals(userType)) {
                                            String caregiverIdGenerated = generateCaregiverId(); // Gerar ID aleatório para o cuidador
                                            User newUser = new User(fullName, email, userType, caregiverIdGenerated, phoneNumber);
                                            mDatabase.child("users").child(userId).setValue(newUser)
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            Toast.makeText(Register.this, "Cuidador registrado com sucesso!", Toast.LENGTH_LONG).show();
                                                            navigateToLogin();
                                                        } else {
                                                            Toast.makeText(Register.this, "Erro ao salvar os dados do cuidador.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            // Se for paciente, associar o caregiverId fornecido pelo usuário
                                            User newUser = new User(fullName, email, userType, caregiverId, phoneNumber);
                                            mDatabase.child("users").child(userId).setValue(newUser)
                                                    .addOnCompleteListener(task1 -> {
                                                        if (task1.isSuccessful()) {
                                                            Toast.makeText(Register.this, "Paciente registrado com sucesso!", Toast.LENGTH_LONG).show();
                                                            navigateToLogin();
                                                        } else {
                                                            Toast.makeText(Register.this, "Erro ao salvar os dados do paciente.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    } else {
                                        Toast.makeText(Register.this, "Falha na criação do usuário: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(Register.this, "O ID do cuidador informado não existe!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(Register.this, "Erro ao verificar ID do cuidador: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Se for cuidador, cria o usuário sem verificação do caregiverId
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(Register.this, task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            String caregiverIdGenerated = generateCaregiverId(); // Gerar ID aleatório para o cuidador
                            User newUser = new User(fullName, email, userType, caregiverIdGenerated, phoneNumber);
                            mDatabase.child("users").child(userId).setValue(newUser)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(Register.this, "Cuidador registrado com sucesso!", Toast.LENGTH_LONG).show();
                                            navigateToLogin();
                                        } else {
                                            Toast.makeText(Register.this, "Erro ao salvar os dados do cuidador.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(Register.this, "Falha na criação do usuário: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
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

    // Método para gerar o caregiverId com 6 letras e 2 números
    private String generateCaregiverId() {
        Random random = new Random();
        StringBuilder caregiverId = new StringBuilder();

        // Gerar 6 letras aleatórias
        for (int i = 0; i < 6; i++) {
            char letter = (char) (random.nextInt(26) + 'A'); // Letras de A a Z
            caregiverId.append(letter);
        }

        // Gerar 2 números aleatórios
        for (int i = 0; i < 2; i++) {
            int number = random.nextInt(10); // Números de 0 a 9
            caregiverId.append(number);
        }

        return caregiverId.toString();
    }

    // Método para validar número de telefone no formato (xx) xxxxx-xxxx
    private boolean isPhoneNumberValid(String phoneNumber) {
        String phonePattern = "^\\(\\d{2}\\) \\d{5}-\\d{4}$"; // Regex para validar o número no formato (xx) xxxxx-xxxx
        Pattern pattern = Pattern.compile(phonePattern);
        Matcher matcher = pattern.matcher(phoneNumber);
        return matcher.matches();
    }
}

