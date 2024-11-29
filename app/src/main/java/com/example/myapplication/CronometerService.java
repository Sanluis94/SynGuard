package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CronometerService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Lógica de serviço para registrar e enviar dados ao Firebase
        return START_STICKY;
    }
}
