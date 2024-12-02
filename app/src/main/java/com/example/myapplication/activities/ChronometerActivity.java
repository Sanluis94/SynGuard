package com.example.myapplication.activities;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.models.CrisisData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.UUID;

public class ChronometerActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private Button startStopButton;
    private boolean isRunning = false;
    private long pauseOffset = 0;
    private long totalCrisisTime = 0L;  // Tempo total das crises
    private long crisisCount = 0L;
    private long lastCrisisTime = 0L;
    private long averageTime = 0L;

    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;

    private DatabaseReference databaseReference;

    private static final int SMS_PERMISSION_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronometer);

        chronometer = findViewById(R.id.chronometer);
        startStopButton = findViewById(R.id.startStopButton);

        bluetoothDevice = getIntent().getParcelableExtra("bluetoothDevice");

        if (bluetoothDevice != null) {
            connectToBluetoothDevice(bluetoothDevice);
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Carregar dados históricos de crises do Firebase
        loadCrisisData();

        startStopButton.setOnClickListener(view -> toggleChronometer());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
        }
    }

    private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        isRunning = true;
        startStopButton.setText("Stop");
        sendSms("Cronômetro iniciado.");
    }

    private void toggleChronometer() {
        Log.d("ChronometerActivity", "Toggle cronômetro. Estado: " + (isRunning ? "Iniciando" : "Parando"));
        if (isRunning) {
            stopAndSaveData();
        } else {
            startChronometer();
        }
    }

    private void stopAndSaveData() {
        chronometer.stop();
        long duration = SystemClock.elapsedRealtime() - chronometer.getBase(); // Calcula a duração da crise em milissegundos
        isRunning = false;

        lastCrisisTime = duration / 1000;  // Convertendo a duração para segundos
        totalCrisisTime += lastCrisisTime; // Soma a duração total das crises
        crisisCount++; // Aumenta o contador de crises

        // Recalcular o tempo médio das crises
        averageTime = crisisCount > 0 ? totalCrisisTime / crisisCount : 0;

        saveDataToFirebase(lastCrisisTime, averageTime, totalCrisisTime); // Salva os dados corrigidos no Firebase

        chronometer.setBase(SystemClock.elapsedRealtime()); // Reseta o cronômetro
        pauseOffset = 0;
        startStopButton.setText("Start");

        sendSms("Cronômetro parado. Duração: " + lastCrisisTime + " segundos."); // Envia a mensagem com a duração da crise
    }

    private void loadCrisisData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Carregar as crises registradas para o usuário autenticado
        databaseReference.child(userId).child("crisisData").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long totalTime = 0L;
                    long count = 0L;

                    // Iterar sobre todos os dados de crise
                    for (DataSnapshot crisisSnapshot : dataSnapshot.getChildren()) {
                        CrisisData crisisData = crisisSnapshot.getValue(CrisisData.class);
                        if (crisisData != null) {
                            totalTime += crisisData.getDuration();  // Somando o tempo das crises
                            count += crisisData.getCrisisCount();   // Somando a quantidade de crises
                        }
                    }

                    // Atualizar os valores históricos
                    totalCrisisTime = totalTime;  // Total de tempo de todas as crises
                    crisisCount = count;  // Total de crises
                    if (crisisCount > 0) {
                        averageTime = totalCrisisTime / crisisCount;  // Calculando a média
                    }

                    Log.d("ChronometerActivity", "Dados históricos carregados. Total de crises: " + crisisCount + ", Tempo total de crises: " + totalCrisisTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChronometerActivity", "Erro ao carregar dados históricos de crises: " + error.getMessage());
            }
        });
    }

    private void saveDataToFirebase(long lastCrisisTime, long averageTime, long totalTime) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Usando a data atual para gerar a chave única
        Calendar calendar = Calendar.getInstance();
        String dateKey = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);

        // Gerar um objeto CrisisData com os dados da crise
        CrisisData crisisData = new CrisisData(lastCrisisTime, averageTime, crisisCount, totalTime);

        // Salvar os dados da crise no Firebase para o usuário autenticado
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.child(userId).child("crisisData").child(dateKey).setValue(crisisData)
                .addOnSuccessListener(aVoid -> Log.d("ChronometerActivity", "Dados de crise salvos com sucesso!"))
                .addOnFailureListener(e -> Log.e("ChronometerActivity", "Erro ao salvar dados de crise: " + e.getMessage()));

        Log.d("ChronometerActivity", "CrisisCount após salvar: " + crisisCount);
    }

    private void sendSms(String message) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child(userId).child("caregiverId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String caregiverId = dataSnapshot.getValue(String.class);
                    Log.d("ChronometerActivity", "Caregiver ID encontrado: " + caregiverId);

                    if (caregiverId != null && !caregiverId.isEmpty()) {
                        // Procurando os usuários com o mesmo caregiverId
                        databaseReference.orderByChild("caregiverId").equalTo(caregiverId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                            String otherUserId = userSnapshot.getKey();  // UID do outro usuário
                                            Log.d("ChronometerActivity", "Outro usuário encontrado: " + otherUserId);

                                            // Buscando o telefone do cuidador
                                            databaseReference.child(otherUserId).child("phone").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {
                                                        String phoneNumber = dataSnapshot.getValue(String.class);
                                                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                                                            SmsManager smsManager = SmsManager.getDefault();
                                                            try {
                                                                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                                                                Log.d("ChronometerActivity", "Mensagem SMS enviada para o cuidador.");
                                                            } catch (Exception e) {
                                                                Log.e("ChronometerActivity", "Erro ao enviar SMS: " + e.getMessage());
                                                            }
                                                        } else {
                                                            Log.e("ChronometerActivity", "Número de telefone do cuidador não encontrado ou inválido.");
                                                        }
                                                    } else {
                                                        Log.e("ChronometerActivity", "Telefone do cuidador não encontrado no banco de dados.");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.e("ChronometerActivity", "Erro ao enviar SMS: " + error.getMessage());
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e("ChronometerActivity", "Erro ao buscar cuidador: " + error.getMessage());
                                    }
                                });
                    } else {
                        Log.e("ChronometerActivity", "Caregiver ID inválido.");
                    }
                } else {
                    Log.e("ChronometerActivity", "Caregiver ID não encontrado no banco de dados.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChronometerActivity", "Erro ao buscar caregiverId: " + error.getMessage());
            }
        });
    }

    private void connectToBluetoothDevice(BluetoothDevice device) {
        // Tentativa de conexão com o dispositivo Bluetooth
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e("ChronometerActivity", "Permissão de Bluetooth não concedida.");
                return;
            }
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();
            InputStream inputStream = socket.getInputStream();

            // Escutar os dados recebidos via Bluetooth
            listenToBluetoothCommands(inputStream);
            Log.d("ChronometerActivity", "Conexão Bluetooth bem-sucedida.");
        } catch (IOException e) {
            Log.e("ChronometerActivity", "Erro ao conectar com o dispositivo Bluetooth: " + e.getMessage());
        }
    }

    private void listenToBluetoothCommands(InputStream inputStream) {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String command = new String(buffer, 0, bytes).trim();
                    if ("START".equals(command)) {
                        runOnUiThread(() -> toggleChronometer());
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("ChronometerActivity", "Permissão SMS concedida.");
            } else {
                Toast.makeText(this, "Permissão para SMS negada.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
