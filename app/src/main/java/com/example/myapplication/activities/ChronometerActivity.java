package com.example.myapplication.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.utils.FirebaseUtils;
import com.example.myapplication.R;

public class ChronometerActivity extends AppCompatActivity {

    private TextView chronometerView;
    private Button startStopButton, pairBluetoothButton;
    private boolean isRunning = false;
    private long startTime = 0L;
    private Handler handler = new Handler();
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronometer);

        chronometerView = findViewById(R.id.chronometerView);
        startStopButton = findViewById(R.id.startChronometerButton);
        pairBluetoothButton = findViewById(R.id.pairBluetoothButton);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        startStopButton.setOnClickListener(view -> {
            if (isRunning) {
                stopChronometer();
            } else {
                startChronometer();
            }
        });

        pairBluetoothButton.setOnClickListener(view -> {
            Intent intent = new Intent(ChronometerActivity.this, BluetoothDeviceSelectionActivity.class);
            startActivity(intent);
        });
    }

    private void startChronometer() {
        isRunning = true;
        startTime = SystemClock.elapsedRealtime();
        handler.post(updateChronometer);
        startStopButton.setText("Stop");

        // Registra a crise no Firebase e notifica o cuidador via SMS
        FirebaseUtils.logSeizureData();
    }

    private void stopChronometer() {
        isRunning = false;
        handler.removeCallbacks(updateChronometer);
        startStopButton.setText("Start");
    }

    private Runnable updateChronometer = new Runnable() {
        @Override
        public void run() {
            long elapsedMillis = SystemClock.elapsedRealtime() - startTime;
            int seconds = (int) (elapsedMillis / 1000) % 60;
            int minutes = (int) (elapsedMillis / (1000 * 60)) % 60;
            int hours = (int) (elapsedMillis / (1000 * 60 * 60));

            String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            chronometerView.setText(time);

            handler.postDelayed(this, 1000);
        }
    };
}
