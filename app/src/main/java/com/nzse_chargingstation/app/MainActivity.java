package com.nzse_chargingstation.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    BottomNavigationView bottom_nav_bar;
    private GoogleMap map;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in {@link
     * #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;



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
    public void onMapReady(@NonNull GoogleMap googleMap) {
//        map.setMaxZoomPreference(4);

        ChargingStation test_subject_1 = new ChargingStation("Hochschule Darmstadt", 49.86625273516996, 8.640257820411557);
        ChargingStation test_subject_2 = new ChargingStation("KFC Weiterstadt", 49.905710, 8.581990);
        ChargingStation test_subject_3 = new ChargingStation("Media Campus", 49.902004957188076, 8.854893065467536);
        Container.getUnfiltered_list().add(test_subject_1);
        Container.getUnfiltered_list().add(test_subject_2);
        Container.getUnfiltered_list().add(test_subject_3);

        map = googleMap;
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        enableMyLocation();
        addCSToMaps();

/*
        Here are the approximate zoom levels and what they do :
        1: World
        5: Landmass/continent
        10: City
        15: Streets
        20: Buildings
 */
        LatLng start = new LatLng(49.8728, 8.6512);
        float zoomLevel = (float) 15.0;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, zoomLevel));
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_COARSE_LOCATION, true);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    /**
     * Clear all markers on map and then loop for adding all charging stations as marker in map.
     */
    private void addCSToMaps() {
        ArrayList<ChargingStation> tmp_unfiltered = Container.getUnfiltered_list();
        ArrayList<ChargingStation> tmp_filtered = Container.getFiltered_list();

        map.clear();
        for(int i = 0; i < tmp_unfiltered.size(); i++) {
            map.addMarker(new MarkerOptions().position(tmp_unfiltered.get(i).getLocation()).title(tmp_unfiltered.get(i).getAddress()));
        }
        for(int i = 0; i < tmp_filtered.size(); i++) {
            map.addMarker(new MarkerOptions().position(tmp_filtered.get(i).getLocation()).title(tmp_filtered.get(i).getAddress()));
        }
    }
}