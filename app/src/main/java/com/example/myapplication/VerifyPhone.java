package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.ReusableCodeForAll;

public class VerifyPhone extends AppCompatActivity {

    private EditText phoneNumberInput;
    private Button verifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);

        initializeViews();

        verifyButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberInput.getText().toString().trim();

            if (phoneNumber.isEmpty()) {
                ReusableCodeForAll.showToast(this, "Please enter a valid phone number!");
            } else {
                ReusableCodeForAll.showToast(this, "Phone number verified!");
                // Proceed with additional verification if required
            }
        });
    }

    private void initializeViews() {
        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        verifyButton = findViewById(R.id.verifyButton);
    }
}
