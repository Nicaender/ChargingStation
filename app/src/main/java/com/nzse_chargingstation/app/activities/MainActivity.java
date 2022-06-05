package com.nzse_chargingstation.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.fragments.FavoritesFragment;
import com.nzse_chargingstation.app.fragments.MapsFragment;
import com.nzse_chargingstation.app.fragments.MyCarsFragment;
import com.nzse_chargingstation.app.fragments.SettingsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    MapsFragment mapsFragment = new MapsFragment();
    MyCarsFragment myCarsFragment = new MyCarsFragment();
    FavoritesFragment favoritesFragment = new FavoritesFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    private FusedLocationProviderClient fusedLocationClient;

    private final int REQUEST_LOCATION_PERMISSION = 1;

    @SuppressLint({"NonConstantResourceId", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestLocationPermission();

        // Initialization
        BottomNavigationView bottom_nav_bar = findViewById(R.id.bottom_navbar);
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

        if(ContainerAndGlobal.isChangedSetting())
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, settingsFragment).commit();
            ContainerAndGlobal.setChangedSetting(false);
        }
        else
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapsFragment).commit();

        // Implementation of bottom navigation bar
        bottom_nav_bar.setOnItemSelectedListener(item -> {

            switch (item.getItemId())
            {
                case R.id.nav_maps:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapsFragment).commit();
                    return true;
                case R.id.nav_mycars:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, myCarsFragment).commit();
                    return true;
                case R.id.nav_favorites:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, favoritesFragment).commit();
                    return true;
                case R.id.nav_settings:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, settingsFragment).commit();
                    return true;
            }
            return false;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            ContainerAndGlobal.setCurrentLocation(location);
                            if(ContainerAndGlobal.isFirstTime())
                            {
                                ContainerAndGlobal.setFirstTime(false);
                                String jsonString = ContainerAndGlobal.getJSONData(this, "ChargingStationJSON.json");
                                try {
                                    JSONArray jsonarray = new JSONArray(jsonString);

                                    for (int i = 0; i < jsonarray.length(); i++) {
                                        JSONObject json_inside = jsonarray.getJSONObject(i);

                                        ContainerAndGlobal.parseLadesaeuleObject(json_inside);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                overridePendingTransition(0, 0);
                                finish();
                            }
                        }
                    });
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }
}