package com.nzse_chargingstation.app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nzse_chargingstation.app.R;

public class LoginActivity extends AppCompatActivity {

    Button btnBackLoginTechniker, btnConfirmLoginTechniker;
    EditText etUsername, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnBackLoginTechniker = findViewById(R.id.buttonBackLoginTechniker);
        btnConfirmLoginTechniker = findViewById(R.id.buttonConfirmLoginTechniker);
        etUsername = findViewById(R.id.editTextUsername);
        etPassword = findViewById(R.id.editTextPassword);

        // Go back to previous activity
        btnBackLoginTechniker.setOnClickListener(v -> finish());

        // Implementation of button to log in to techniker site
        btnConfirmLoginTechniker.setOnClickListener(v -> {
            if(etUsername.getText().toString().equals("nicaender") && etPassword.getText().toString().equals("nic123"))
            {
                Toast.makeText(getApplicationContext(), "Login Success!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), TechnicianActivity.class));
                finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
            }
        });
    }
}