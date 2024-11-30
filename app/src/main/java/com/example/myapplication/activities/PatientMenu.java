package com.example.myapplication.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.synguard.R;
import com.example.synguard.data.CrisisData;
import com.example.synguard.services.CronometerService;
import com.example.synguard.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class PatientMenu extends AppCompatActivity {

    private static final String TAG = "PatientMenuActivity";
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Substitua pelo UUID do seu módulo HC-05
    private static final String HC05_NAME = "HC-05"; // Substitua pelo nome do seu módulo HC-05

    private TextView medicalDataTextView;
    private Button startStopButton, medicalDataButton, logoutButton;
    private Chronometer chronometer;
    private long pauseOffset;
    private boolean running;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_menu);

        mAuth = FirebaseUtils.getFirebaseAuth();
        mDatabase = FirebaseUtils.getDatabaseReference();

        medicalDataTextView = findViewById(R.id.medicalDataTextView);
        startStopButton = findViewById(R.id.startStopButton);
        medicalDataButton = findViewById(R.id.medicalDataButton);
        logoutButton = findViewById(R.id.logoutButton);
        chronometer = findViewById(R.id.chronometer);

        startStopButton.setOnClickListener(v -> {
            if (!running) {
                startChronometer();
                sendBluetoothSignal("1"); // Envia sinal para o Arduino iniciar o cronômetro
            } else {
                stopChronometer();
                sendBluetoothSignal("0"); // Envia sinal para o Arduino parar o cronômetro
            }
        });

        medicalDataButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MedicalDataActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Inicializar Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não suportado neste dispositivo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        } else {
            connectToHC05();
        }

        // Buscar dados médicos do Firebase
        fetchMedicalData();
    }private void startChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();
        running = true;
        startStopButton.setText("Parar");

        // Iniciar serviço de cronômetro em segundo plano
        Intent serviceIntent = new Intent(this, CronometerService.class);
        serviceIntent.setAction("START");
        startService(serviceIntent);

        // Enviar notificação para o cuidador
        sendNotificationToCaregiver();
    }

    private void stopChronometer() {
        chronometer.stop();
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
        running = false;
        startStopButton.setText("Iniciar");

        // Parar serviço de cronômetro em segundo plano
        Intent serviceIntent = new Intent(this, CronometerService.class);
        serviceIntent.setAction("STOP");
        startService(serviceIntent);
    }

    private void sendBluetoothSignal(String signal) {
        if (outputStream != null) {
            try {
                outputStream.write(signal.getBytes());
            } catch (IOException e) {
                Log.e(TAG, "Erro ao enviar sinal Bluetooth: " + e.getMessage());
            }
        }
    }

    private void connectToHC05() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(HC05_NAME)) {
                    try {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                        bluetoothSocket.connect();
                        outputStream = bluetoothSocket.getOutputStream();
                        inputStream = bluetoothSocket.getInputStream();
                        beginListenForData();
                        Toast.makeText(this, "Conectado ao HC-05", Toast.LENGTH_SHORT).show();
                        break;
                    } catch (IOException e) {
                        Log.e(TAG, "Erro ao conectar ao HC-05: " + e.getMessage());
                    }
                }
            }
        } else {
            Toast.makeText(this, "Nenhum dispositivo pareado encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; // This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = inputStream.available();
                        if(bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++) {
                                byte b = packetBytes[i];
                                if(b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            // Processar dados recebidos do HC-05
                                            if (data.equals("1")) {
                                                // Iniciar cronômetro se o sinal for "1"
                                                if (!running) {
                                                    startChronometer();
                                                }
                                            } else if (data.equals("0")) {
                                                // Parar cronômetro se o sinal for "0"
                                                if (running) {
                                                    stopChronometer();
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    private void fetchMedicalData() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference medicalDataRef = mDatabase.child("users").child(userId).child("medicalData");

        medicalDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot