package com.nzse_chargingstation.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    BottomNavigationView bottom_nav_bar;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        bottom_nav_bar = findViewById(R.id.bottom_navbar);
        bottom_nav_bar.setSelectedItemId(R.id.nav_maps);

        // Saving state of our app
        // using SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);

        // When user reopens the app
        // after applying dark/light mode
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Bottom navbar implementation
        bottom_nav_bar.setOnItemSelectedListener(item -> {

            switch (item.getItemId())
            {
                case R.id.nav_maps:
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
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
            }
            return false;
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // mMap.setMaxZoomPreference(4);

        LatLng department = new LatLng (49.86625273516996, 8.640257820411557);
        googleMap.addMarker(new MarkerOptions().position(department).title("Marker h_da, fbi"));

        LatLng kfc = new LatLng (49.905710, 8.581990);
        googleMap.addMarker(new MarkerOptions().position(department).title("KFC Weiterstadt"));

        LatLng mediacampus  = new LatLng (49.902004957188076, 8.854893065467536);
        googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(mediacampus).title("Marker Media Campus"));
/*
        Here are the approximate zoom levels and what they do :
        1: World
        5: Landmass/continent
        10: City
        15: Streets
        20: Buildings
 */
        float zoomLevel = (float) 17.0;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(department, zoomLevel));

//        double centerlat = (mediacampus.latitude+department.latitude)/2;
//        double centerlon = (mediacampus.longitude+department.longitude)/2;
//        LatLng center  = new LatLng (centerlat, centerlon);
//        googleMap.animateCamera(CameraUpdateFactory.newLatLng(department));
//        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }
}