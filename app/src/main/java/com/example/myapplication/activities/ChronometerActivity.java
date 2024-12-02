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
            listenToBluetoothCommands(); // Inicia a escuta para comandos Bluetooth
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Carregar dados históricos de crises do Firebase
        loadCrisisData();

        startStopButton.setOnClickListener(view -> toggleChronometer());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
        }
    }

    private void toggleChronometer() {
        Log.d("ChronometerActivity", "Toggle cronômetro. Estado: " + (isRunning ? "Iniciando" : "Parando"));
        if (isRunning) {
            stopAndSaveData();
        } else {
            startChronometer();
        }
    }

    private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        isRunning = true;
        startStopButton.setText("Stop");

        sendSms("Cronômetro iniciado.");
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
                                                    String phoneNumber = dataSnapshot.getValue(String.class);
                                                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                                                        Log.d("ChronometerActivity", "Número de telefone do cuidador encontrado: " + phoneNumber);

                                                        if (phoneNumber.startsWith("+")) {
                                                            try {
                                                                SmsManager smsManager = SmsManager.getDefault();
                                                                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                                                                Log.d("ChronometerActivity", "Mensagem SMS enviada com sucesso.");
                                                            } catch (Exception e) {
                                                                Log.e("ChronometerActivity", "Erro ao enviar SMS: " + e.getMessage());
                                                            }
                                                        } else {
                                                            Log.e("ChronometerActivity", "Número de telefone inválido.");
                                                        }
                                                    } else {
                                                        Log.e("ChronometerActivity", "Telefone não encontrado ou inválido.");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.e("ChronometerActivity", "Erro ao recuperar o telefone: " + error.getMessage());
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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChronometerActivity", "Erro ao recuperar caregiverId: " + error.getMessage());
            }
        });
    }

    private void connectToBluetoothDevice(BluetoothDevice device) {
        try {
            // Configuração do UUID padrão para o Bluetooth
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            UUID uuid = device.getUuids()[0].getUuid();
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            Log.d("ChronometerActivity", "Conectado ao dispositivo Bluetooth.");
        } catch (IOException e) {
            Log.e("ChronometerActivity", "Erro ao conectar ao dispositivo Bluetooth: " + e.getMessage());
        }
    }

    private void listenToBluetoothCommands() {
        // Método para escutar comandos Bluetooth
        // Implemente a lógica para escutar comandos Bluetooth, como iniciar e parar o cronômetro via Bluetooth
    }
}
