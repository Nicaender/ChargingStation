package com.nzse_chargingstation.app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class TechnikerActivity extends AppCompatActivity {

    Button btn_back_techniker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_techniker);

        btn_back_techniker = findViewById(R.id.button_back_techniker);

        btn_back_techniker.setOnClickListener(v -> finish());
    }
}