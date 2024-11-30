package com.example.myapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.myapplication.R;

import java.util.Random;

public class Register extends Activity {

    private EditText fullNameField, emailField, passwordField, confirmPasswordField, caregiverIdField;
    private Button registerButton;
    private RadioGroup userTypeGroup;
    private RadioButton patientRadioButton, caregiverRadioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializa os campos e botões
        fullNameField = findViewById(R.id.fullNameField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        confirmPasswordField = findViewById(R.id.confirmPasswordField);
        caregiverIdField = findViewById(R.id.caregiverIdField);  // Visível apenas se o tipo for paciente

        userTypeGroup = findViewById(R.id.userTypeGroup);
        patientRadioButton = findViewById(R.id.patientRadioButton);
        caregiverRadioButton = findViewById(R.id.caregiverRadioButton);

        registerButton = findViewById(R.id.registerButton);

        // Ação de clique para o botão "Registrar"
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(); // Tenta registrar o usuário
            }
        });

        // Adiciona lógica para exibir campo de ID do cuidador somente para paciente
        userTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.patientRadioButton) {
                caregiverIdField.setVisibility(View.VISIBLE);  // Exibe campo de ID do cuidador
            } else {
                caregiverIdField.setVisibility(View.GONE);  // Esconde campo de ID do cuidador
            }
        });
    }

    // Método para registrar o usuário
    private void registerUser() {
        String fullName = fullNameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();
        String userType = getUserType();  // Obtém o tipo de usuário (paciente ou cuidador)
        String caregiverId = caregiverIdField.getText().toString().trim();

        // Verificação de campos obrigatórios
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

        // Se o tipo de usuário for cuidador, gerar um ID único
        if ("cuidador".equals(userType)) {
            String caregiverUniqueId = generateCaregiverId();
            // Aqui você pode salvar os dados no Firebase ou em outro banco de dados
            Toast.makeText(Register.this, "Usuário registrado com sucesso! ID do cuidador: " + caregiverUniqueId, Toast.LENGTH_LONG).show();
        } else {
            // Registra o paciente normalmente (não precisa de ID único)
            Toast.makeText(Register.this, "Paciente registrado com sucesso!", Toast.LENGTH_LONG).show();
        }

        // Navega para a tela de login após o registro
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
        finish(); // Finaliza a tela de registro
    }

    // Método para gerar um ID único para cuidadores
    private String generateCaregiverId() {
        Random random = new Random();
        StringBuilder caregiverId = new StringBuilder();

        // Gerar 4 letras aleatórias
        for (int i = 0; i < 4; i++) {
            caregiverId.append((char) (random.nextInt(26) + 'A'));
        }

        // Gerar 2 números aleatórios
        for (int i = 0; i < 2; i++) {
            caregiverId.append(random.nextInt(10));
        }

        return caregiverId.toString();
    }

    // Método para obter o tipo de usuário selecionado
    private String getUserType() {
        int selectedId = userTypeGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.patientRadioButton) {
            return "paciente";
        } else if (selectedId == R.id.caregiverRadioButton) {
            return "cuidador";
        }
        return "";  // Caso nenhum seja selecionado
    }
}
