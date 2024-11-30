package com.example.myapplication.activities;

import static com.example.myapplication.activities.PatientMenu.MY_UUID;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.UserHandle;
import android.telephony.SmsManager;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.R;
import com.example.myapplication.activities.PatientMenu;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChronometerActivity extends AppCompatActivity {

    private Chronometer chronometer;
    private boolean isRunning = false; // Para controlar se o cronômetro está em execução ou não
    private long timeWhenStopped = 0; // Tempo quando o cronômetro foi parado
    private BluetoothService bluetoothService; // Para controlar a comunicação Bluetooth
    private static final String CAREGIVER_PHONE_NUMBER = "+551199999999"; // Número do cuidador, deve ser dinâmico

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chronometer);

        chronometer = findViewById(R.id.chronometer); // Referência ao componente Chronometer
        Button startStopButton = findViewById(R.id.startStopButton); // Botão para iniciar/parar/resetar

        bluetoothService = new BluetoothService(); // Instanciando o serviço Bluetooth
        bluetoothService.connectToDevice("00:11:22:33:44:55"); // Substitua com o endereço do dispositivo Bluetooth

        // Definir o comportamento do botão
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startStopResetChronometer(); // Chama o método para iniciar, parar ou resetar
            }
        });

        // Configuração para botões e funcionalidades adicionais (voltar à tela principal)
        Button backToMainButton = findViewById(R.id.backToMainButton);
        backToMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToMain(); // Volta para a tela principal do paciente
            }
        });
    }

    // Método para iniciar, parar ou resetar o cronômetro
    public void startStopResetChronometer() {
        if (isRunning) {
            // Se o cronômetro estiver rodando, pare-o
            chronometer.stop();
            timeWhenStopped = SystemClock.elapsedRealtime() - chronometer.getBase();
        } else {
            // Se o cronômetro estiver parado, inicie ou reset
            if (timeWhenStopped == 0) {
                chronometer.setBase(SystemClock.elapsedRealtime());
            } else {
                chronometer.setBase(SystemClock.elapsedRealtime() - timeWhenStopped);
                timeWhenStopped = 0;
            }
            chronometer.start();
        }

        // Alterna entre estado de execução ou parada
        isRunning = !isRunning;

        // Enviar SMS para o cuidador quando o cronômetro for iniciado
        if (isRunning) {
            sendSMSToCaregiver();
        }
    }

    // Método para enviar o SMS para o cuidador
    private void sendSMSToCaregiver() {
        String message = "O cronômetro foi iniciado. A crise começou.";

        // Envia SMS para o número do cuidador
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(CAREGIVER_PHONE_NUMBER, null, message, null, null);

        // Exibe uma mensagem de confirmação
        Toast.makeText(ChronometerActivity.this, "SMS enviado para o cuidador", Toast.LENGTH_SHORT).show();
    }

    // Método para conectar o dispositivo Bluetooth
    private class BluetoothService extends Context {
        private BluetoothAdapter mBluetoothAdapter;
        private BluetoothSocket mBluetoothSocket;
        private OutputStream mOutputStream;

        public BluetoothService() {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        @Override
        public AssetManager getAssets() {
            return null;
        }

        @Override
        public Resources getResources() {
            return null;
        }

        @Override
        public PackageManager getPackageManager() {
            return null;
        }

        @Override
        public ContentResolver getContentResolver() {
            return null;
        }

        @Override
        public Looper getMainLooper() {
            return null;
        }

        @Override
        public Context getApplicationContext() {
            return null;
        }

        @Override
        public void setTheme(int i) {

        }

        @Override
        public Resources.Theme getTheme() {
            return null;
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public String getPackageName() {
            return "";
        }

        @Override
        public ApplicationInfo getApplicationInfo() {
            return null;
        }

        @Override
        public String getPackageResourcePath() {
            return "";
        }

        @Override
        public String getPackageCodePath() {
            return "";
        }

        @Override
        public SharedPreferences getSharedPreferences(String s, int i) {
            return null;
        }

        @Override
        public boolean moveSharedPreferencesFrom(Context context, String s) {
            return false;
        }

        @Override
        public boolean deleteSharedPreferences(String s) {
            return false;
        }

        @Override
        public FileInputStream openFileInput(String s) throws FileNotFoundException {
            return null;
        }

        @Override
        public FileOutputStream openFileOutput(String s, int i) throws FileNotFoundException {
            return null;
        }

        @Override
        public boolean deleteFile(String s) {
            return false;
        }

        @Override
        public File getFileStreamPath(String s) {
            return null;
        }

        @Override
        public File getDataDir() {
            return null;
        }

        @Override
        public File getFilesDir() {
            return null;
        }

        @Override
        public File getNoBackupFilesDir() {
            return null;
        }

        @Nullable
        @Override
        public File getExternalFilesDir(@Nullable String s) {
            return null;
        }

        @Override
        public File[] getExternalFilesDirs(String s) {
            return new File[0];
        }

        @Override
        public File getObbDir() {
            return null;
        }

        @Override
        public File[] getObbDirs() {
            return new File[0];
        }

        @Override
        public File getCacheDir() {
            return null;
        }

        @Override
        public File getCodeCacheDir() {
            return null;
        }

        @Nullable
        @Override
        public File getExternalCacheDir() {
            return null;
        }

        @Override
        public File[] getExternalCacheDirs() {
            return new File[0];
        }

        @Override
        public File[] getExternalMediaDirs() {
            return new File[0];
        }

        @Override
        public String[] fileList() {
            return new String[0];
        }

        @Override
        public File getDir(String s, int i) {
            return null;
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(String s, int i, SQLiteDatabase.CursorFactory cursorFactory) {
            return null;
        }

        @Override
        public SQLiteDatabase openOrCreateDatabase(String s, int i, SQLiteDatabase.CursorFactory cursorFactory, @Nullable DatabaseErrorHandler databaseErrorHandler) {
            return null;
        }

        @Override
        public boolean moveDatabaseFrom(Context context, String s) {
            return false;
        }

        @Override
        public boolean deleteDatabase(String s) {
            return false;
        }

        @Override
        public File getDatabasePath(String s) {
            return null;
        }

        @Override
        public String[] databaseList() {
            return new String[0];
        }

        @Override
        public Drawable getWallpaper() {
            return null;
        }

        @Override
        public Drawable peekWallpaper() {
            return null;
        }

        @Override
        public int getWallpaperDesiredMinimumWidth() {
            return 0;
        }

        @Override
        public int getWallpaperDesiredMinimumHeight() {
            return 0;
        }

        @Override
        public void setWallpaper(Bitmap bitmap) throws IOException {

        }

        @Override
        public void setWallpaper(InputStream inputStream) throws IOException {

        }

        @Override
        public void clearWallpaper() throws IOException {

        }

        @Override
        public void startActivity(Intent intent) {

        }

        @Override
        public void startActivity(Intent intent, @Nullable Bundle bundle) {

        }

        @Override
        public void startActivities(Intent[] intents) {

        }

        @Override
        public void startActivities(Intent[] intents, Bundle bundle) {

        }

        @Override
        public void startIntentSender(IntentSender intentSender, @Nullable Intent intent, int i, int i1, int i2) throws IntentSender.SendIntentException {

        }

        @Override
        public void startIntentSender(IntentSender intentSender, @Nullable Intent intent, int i, int i1, int i2, @Nullable Bundle bundle) throws IntentSender.SendIntentException {

        }

        @Override
        public void sendBroadcast(Intent intent) {

        }

        @Override
        public void sendBroadcast(Intent intent, @Nullable String s) {

        }

        @Override
        public void sendOrderedBroadcast(Intent intent, @Nullable String s) {

        }

        @Override
        public void sendOrderedBroadcast(@NonNull Intent intent, @Nullable String s, @Nullable BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s1, @Nullable Bundle bundle) {

        }

        @Override
        public void sendBroadcastAsUser(Intent intent, UserHandle userHandle) {

        }

        @Override
        public void sendBroadcastAsUser(Intent intent, UserHandle userHandle, @Nullable String s) {

        }

        @Override
        public void sendOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, @Nullable String s, BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s1, @Nullable Bundle bundle) {

        }

        @Override
        public void sendStickyBroadcast(Intent intent) {

        }

        @Override
        public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s, @Nullable Bundle bundle) {

        }

        @Override
        public void removeStickyBroadcast(Intent intent) {

        }

        @Override
        public void sendStickyBroadcastAsUser(Intent intent, UserHandle userHandle) {

        }

        @Override
        public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle userHandle, BroadcastReceiver broadcastReceiver, @Nullable Handler handler, int i, @Nullable String s, @Nullable Bundle bundle) {

        }

        @Override
        public void removeStickyBroadcastAsUser(Intent intent, UserHandle userHandle) {

        }

        @Nullable
        @Override
        public Intent registerReceiver(@Nullable BroadcastReceiver broadcastReceiver, IntentFilter intentFilter) {
            return null;
        }

        @Nullable
        @Override
        public Intent registerReceiver(@Nullable BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, int i) {
            return null;
        }

        @Nullable
        @Override
        public Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, @Nullable String s, @Nullable Handler handler) {
            return null;
        }

        @Nullable
        @Override
        public Intent registerReceiver(BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, @Nullable String s, @Nullable Handler handler, int i) {
            return null;
        }

        @Override
        public void unregisterReceiver(BroadcastReceiver broadcastReceiver) {

        }

        @Nullable
        @Override
        public ComponentName startService(Intent intent) {
            return null;
        }

        @Nullable
        @Override
        public ComponentName startForegroundService(Intent intent) {
            return null;
        }

        @Override
        public boolean stopService(Intent intent) {
            return false;
        }

        @Override
        public boolean bindService(@NonNull Intent intent, @NonNull ServiceConnection serviceConnection, int i) {
            return false;
        }

        @Override
        public void unbindService(@NonNull ServiceConnection serviceConnection) {

        }

        @Override
        public boolean startInstrumentation(@NonNull ComponentName componentName, @Nullable String s, @Nullable Bundle bundle) {
            return false;
        }

        @Override
        public Object getSystemService(@NonNull String s) {
            return null;
        }

        @Nullable
        @Override
        public String getSystemServiceName(@NonNull Class<?> aClass) {
            return "";
        }

        @Override
        public int checkPermission(@NonNull String s, int i, int i1) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkCallingPermission(@NonNull String s) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkCallingOrSelfPermission(@NonNull String s) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkSelfPermission(@NonNull String s) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public void enforcePermission(@NonNull String s, int i, int i1, @Nullable String s1) {

        }

        @Override
        public void enforceCallingPermission(@NonNull String s, @Nullable String s1) {

        }

        @Override
        public void enforceCallingOrSelfPermission(@NonNull String s, @Nullable String s1) {

        }

        @Override
        public void grantUriPermission(String s, Uri uri, int i) {

        }

        @Override
        public void revokeUriPermission(Uri uri, int i) {

        }

        @Override
        public void revokeUriPermission(String s, Uri uri, int i) {

        }

        @Override
        public int checkUriPermission(Uri uri, int i, int i1, int i2) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkCallingUriPermission(Uri uri, int i) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkCallingOrSelfUriPermission(Uri uri, int i) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public int checkUriPermission(@Nullable Uri uri, @Nullable String s, @Nullable String s1, int i, int i1, int i2) {
            return PackageManager.PERMISSION_GRANTED;
        }

        @Override
        public void enforceUriPermission(Uri uri, int i, int i1, int i2, String s) {

        }

        @Override
        public void enforceCallingUriPermission(Uri uri, int i, String s) {

        }

        @Override
        public void enforceCallingOrSelfUriPermission(Uri uri, int i, String s) {

        }

        @Override
        public void enforceUriPermission(@Nullable Uri uri, @Nullable String s, @Nullable String s1, int i, int i1, int i2, @Nullable String s2) {

        }

        @Override
        public Context createPackageContext(String s, int i) throws PackageManager.NameNotFoundException {
            return null;
        }

        @Override
        public Context createContextForSplit(String s) throws PackageManager.NameNotFoundException {
            return null;
        }

        @Override
        public Context createConfigurationContext(@NonNull Configuration configuration) {
            return null;
        }

        @Override
        public Context createDisplayContext(@NonNull Display display) {
            return null;
        }

        @Override
        public Context createDeviceProtectedStorageContext() {
            return null;
        }

        @Override
        public boolean isDeviceProtectedStorage() {
            return false;
        }

        public void connectToDevice(String address) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            try {
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
                mBluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
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
                mBluetoothSocket.connect();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Método para enviar sinal para iniciar o cronômetro via Bluetooth
        public void sendSignalToStartChronometer() {
            try {
                mOutputStream.write("START_CRONOMETER".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para retornar à tela principal do paciente
    public void goBackToMain() {
        Intent intent = new Intent(ChronometerActivity.this, PatientMenu.class);
        startActivity(intent);
        finish();  // Finaliza a atividade atual
    }
}
