package com.nzse_chargingstation.app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nzse_chargingstation.app.R;

public class LoginActivity extends AppCompatActivity {

    Button btn_back_login_techniker, btn_confirm_login_techniker;
    EditText et_username, et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_back_login_techniker = findViewById(R.id.button_back_login_techniker);
        btn_confirm_login_techniker = findViewById(R.id.button_confirm_login_techniker);
        et_username = findViewById(R.id.edittext_username);
        et_password = findViewById(R.id.edittext_password);

        // Go back to previous activity
        btn_back_login_techniker.setOnClickListener(v -> finish());

        // Implementation of button to log in to techniker site
        btn_confirm_login_techniker.setOnClickListener(v -> {
            if(et_username.getText().toString().equals("nicaender") && et_password.getText().toString().equals("nic123"))
            {
                Toast.makeText(getApplicationContext(), "Login Success!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), TechnikerActivity.class));
                finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
            }
        });
    }
}