package com.nzse_chargingstation.app.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

import com.nzse_chargingstation.app.R;

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