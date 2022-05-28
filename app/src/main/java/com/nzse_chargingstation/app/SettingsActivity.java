package com.nzse_chargingstation.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    BottomNavigationView bottom_nav_bar;
    Button btn_login_techniker;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bottom_nav_bar = findViewById(R.id.bottom_navbar);
        btn_login_techniker =  findViewById(R.id.button_login_techniker);

        bottom_nav_bar.setSelectedItemId(R.id.nav_settings);

        // Button implementation
        btn_login_techniker.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
//                overridePendingTransition(0, 0);
//                finish();
        });

        // Bottom navbar implementation
        bottom_nav_bar.setOnItemSelectedListener(item -> {

            switch (item.getItemId())
            {
                case R.id.nav_maps:
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                case R.id.nav_mycars:
                    startActivity(new Intent(getApplicationContext(), MyCarsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                case R.id.nav_favorites:
                    startActivity(new Intent(getApplicationContext(), FavoritesActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                case R.id.nav_settings:
                    return true;
            }
            return false;
        });
    }
}