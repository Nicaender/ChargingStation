package com.nzse_chargingstation.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    BottomNavigationView bottom_nav_bar;
    Button btn_login_techniker, btn_darkmode;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bottom_nav_bar = findViewById(R.id.bottom_navbar);
        btn_login_techniker =  findViewById(R.id.button_login_techniker);
        btn_darkmode = findViewById(R.id.button_darkmode);

        bottom_nav_bar.setSelectedItemId(R.id.nav_settings);

        // Saving state of our app
        // using SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);

        // When user reopens the app
        // after applying dark/light mode
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            btn_darkmode.setText(R.string.disable_darkmode);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            btn_darkmode.setText(R.string.enable_darkmode);
        }

        // Implementation of dark mode button
        btn_darkmode.setOnClickListener(v -> {
            if (isDarkModeOn) {
                // if dark mode is on it
                // will turn it off
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                // it will set isDarkModeOn
                // boolean to false
                editor.putBoolean("isDarkModeOn", false);
                editor.apply();
                // change text of Button
                btn_darkmode.setText(R.string.enable_darkmode);
            }
            else {
                // if dark mode is off
                // it will turn it on
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                // it will set isDarkModeOn
                // boolean to true
                editor.putBoolean("isDarkModeOn", true);
                editor.apply();
                // change text of Button
                btn_darkmode.setText(R.string.disable_darkmode);
            }
        });

        // Implementation of button to login site from techniker
        btn_login_techniker.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
//                overridePendingTransition(0, 0);
//                finish();
        });

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