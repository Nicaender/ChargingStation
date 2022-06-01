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
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap map;
    private TextView tv_radius_value;
    private FusedLocationProviderClient fusedLocationClient;
    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static int radius_value = 0;

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

    @SuppressLint({"NonConstantResourceId", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization
        BottomNavigationView bottom_nav_bar = findViewById(R.id.bottom_navbar);
        bottom_nav_bar.setSelectedItemId(R.id.nav_maps);
        tv_radius_value = findViewById(R.id.textview_radius_value);
        Button btn_radius_confirm = findViewById(R.id.button_radius_confirm);
        ImageButton imgbtn_arrow_up = findViewById(R.id.imagebutton_arrow_up);
        ImageButton imgbtn_arrow_down = findViewById(R.id.imagebutton_arrow_down);

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

        // get current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initializing the google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Setting the start value of filter range
        if(radius_value < 10)
        {
            String tmp = "0" + radius_value;
            tv_radius_value.setText(tmp);
        }
        else
            tv_radius_value.setText(String.valueOf(radius_value));

        // Implementation of button to confirm the radius filter
        btn_radius_confirm.setOnClickListener(v -> {
            ContainerAndGlobal.setFilter_range(radius_value);
            ContainerAndGlobal.enable_filter();
            map.clear();
            addCSToMaps();
        });

        // Implementation of increment from radius_value
        imgbtn_arrow_up.setOnClickListener(v -> {
            if(radius_value < 99)
            {
                radius_value++;
                if(radius_value < 10)
                {
                    String tmp = "0" + radius_value;
                    tv_radius_value.setText(tmp);
                }
                else
                    tv_radius_value.setText(String.valueOf(radius_value));
            }
        });

        // Implementation of decrement from radius_value
        imgbtn_arrow_down.setOnClickListener(v -> {
            if(radius_value > 0)
            {
                radius_value--;
                if(radius_value < 10)
                {
                    String tmp = "0" + radius_value;
                    tv_radius_value.setText(tmp);
                }
                else
                    tv_radius_value.setText(String.valueOf(radius_value));
            }
        });

        // Implementation of bottom navigation bar
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

        ChargingStation test_subject_1 = new ChargingStation("Hochschule Darmstadt", 49.86625273516996, 8.640257820411557);
        ChargingStation test_subject_2 = new ChargingStation("KFC Weiterstadt", 49.905710, 8.581990);
        ChargingStation test_subject_3 = new ChargingStation("Media Campus", 49.902004957188076, 8.854893065467536);
        ContainerAndGlobal.getCharging_station_list().add(test_subject_1);
        ContainerAndGlobal.getCharging_station_list().add(test_subject_2);
        ContainerAndGlobal.getCharging_station_list().add(test_subject_3);

        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
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
    }

    @Override
    public boolean onMarkerClick(@NonNull final Marker marker) {
        if(ContainerAndGlobal.getCurrent_location() != null)
            marker.setSnippet("Distance: " + df.format(ContainerAndGlobal.calculateLength(marker.getPosition(), ContainerAndGlobal.getCurrent_location())) + " KM, click for more info");
        else
            marker.setSnippet("Distance: unknown");

        return false;
    }

    @Override
    public void onInfoWindowClick(@NonNull final Marker marker) {
        if(!report_charging_station(marker))
            return;

        startActivity(new Intent(getApplicationContext(), ReportActivity.class));
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
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            ContainerAndGlobal.setCurrent_location(location);
                            LatLng start = new LatLng(ContainerAndGlobal.getCurrent_location().getLatitude(), ContainerAndGlobal.getCurrent_location().getLongitude());
                            float zoomLevel = (float) 15.0;
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, zoomLevel));
                            if(ContainerAndGlobal.isFirst_time())
                            {
                                ContainerAndGlobal.setFirst_time(false);
                                map.clear();
                                startActivity(new Intent(getApplicationContext(), LoadingActivity.class));
                                overridePendingTransition(0, 0);
                                finish();
                            }
                        }
                    });
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_COARSE_LOCATION, true);
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
        for(int i = 0; i < ContainerAndGlobal.getCharging_station_list().size(); i++)
        {
            map.addMarker(new MarkerOptions()
                    .position(ContainerAndGlobal.getCharging_station_list().get(i).getLocation())
                    .title(ContainerAndGlobal.getCharging_station_list().get(i).getAddress()));
        }
        for(int i = 0; i < ContainerAndGlobal.getCharging_station_list_filtered().size(); i++)
        {
            map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .position(ContainerAndGlobal.getCharging_station_list_filtered().get(i).getLocation())
                    .title(ContainerAndGlobal.getCharging_station_list_filtered().get(i).getAddress()));
        }
    }

    /**
     * Marking a charging station as defective
     * @param marker from clicked location in google map
     * @return true if marker exists & the same location of charging station exists
     */
    private boolean report_charging_station(Marker marker)
    {
        for(int i = 0; i < ContainerAndGlobal.getCharging_station_list().size(); i++)
        {
            if(Objects.requireNonNull(marker.getPosition()).equals(ContainerAndGlobal.getCharging_station_list().get(i).getLocation()))
            {
                ContainerAndGlobal.setReported_charging_station(ContainerAndGlobal.getCharging_station_list().get(i));
                return true;
            }
        }
        for(int i = 0; i < ContainerAndGlobal.getCharging_station_list_filtered().size(); i++)
        {
            if(Objects.requireNonNull(marker.getPosition()).equals(ContainerAndGlobal.getCharging_station_list_filtered().get(i).getLocation()))
            {
                ContainerAndGlobal.setReported_charging_station(ContainerAndGlobal.getCharging_station_list_filtered().get(i));
                return true;
            }
        }

        return false;
    }
}