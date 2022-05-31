package com.nzse_chargingstation.app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FavoritesActivity extends AppCompatActivity {

    BottomNavigationView bottom_nav_bar;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        bottom_nav_bar = findViewById(R.id.bottom_navbar);
        bottom_nav_bar.setSelectedItemId(R.id.nav_favorites);

        // Implementation of bottom navigation bar
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
                    return true;
                case R.id.nav_settings:
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
            }
            return false;
        });
    }
}