package com.nzse_chargingstation.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class SettingsActivity extends AppCompatActivity {

    BottomNavigationView bottom_nav_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bottom_nav_bar = findViewById(R.id.bottom_navbar);
        bottom_nav_bar.setSelectedItemId(R.id.nav_settings);

        // Bottom navbar implementation
        bottom_nav_bar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

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
            }
        });
    }
}