package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;

import androidx.core.app.ActivityCompat;

public class Utils {

    private static final int SMS_PERMISSION_REQUEST_CODE = 100;

    // Check SMS permission
    public static boolean checkSmsPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    // Request SMS permission
    public static void requestSmsPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
    }

    // Send SMS
    public static void sendSms(Context context, String phoneNumber, String message) {
        if (checkSmsPermission(context)) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            } catch (Exception e) {
                com.example.myapplication.ReusableCodeForAll.showToast(context, "Failed to send SMS: " + e.getMessage());
            }
        } else {
            com.example.myapplication.ReusableCodeForAll.showToast(context, "SMS permission not granted!");
        }
    }
}
