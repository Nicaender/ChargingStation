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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    // Initialization
    private BottomNavigationView bottom_nav_bar;
    private GoogleMap map;
    private TextView tv_radius_value;
    private Button btn_radius_confirm;
    private ImageButton imgbtn_arrow_up, imgbtn_arrow_down;
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

        // get current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initializing the google map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        bottom_nav_bar = findViewById(R.id.bottom_navbar);
        bottom_nav_bar.setSelectedItemId(R.id.nav_maps);
        tv_radius_value = findViewById(R.id.textview_radius_value);
        btn_radius_confirm = findViewById(R.id.button_radius_confirm);
        imgbtn_arrow_up = findViewById(R.id.imagebutton_arrow_up);
        imgbtn_arrow_down = findViewById(R.id.imagebutton_arrow_down);

        if(radius_value < 10)
        {
            String tmp = "0" + radius_value;
            tv_radius_value.setText(tmp);
        }
        else
            tv_radius_value.setText(String.valueOf(radius_value));

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

        btn_radius_confirm.setOnClickListener(v -> {
            Container.setRange_filter(radius_value);
            map.clear();
            for(int i = 0; i < Container.getFiltered_list().size(); i++)
            {
                if(calculateLength(Container.getFiltered_list().get(i).getLocation(), Container.getLast_location()) > Container.getRange_filter())
                {
                    Container.getUnfiltered_list().add(Container.getFiltered_list().get(i));
                    Container.getFiltered_list().remove(i);
                    i--;
                }
            }
            for(int i = 0; i < Container.getUnfiltered_list().size(); i++)
            {
                if(calculateLength(Container.getUnfiltered_list().get(i).getLocation(), Container.getLast_location()) < Container.getRange_filter())
                {
                    Container.getFiltered_list().add(Container.getUnfiltered_list().get(i));
                    Container.getUnfiltered_list().remove(i);
                    i--;
                }
            }
            addCSToMaps();
        });

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
        map.setOnMarkerClickListener(this);
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
                            Container.setLast_location(location);
                            LatLng start = new LatLng(Container.getLast_location().getLatitude(), Container.getLast_location().getLongitude());
                            float zoomLevel = (float) 15.0;
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, zoomLevel));
                            if(Container.first_time)
                            {
                                Container.first_time = false;
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
        for(int i = 0; i < Container.getUnfiltered_list().size(); i++) {
            map.addMarker(new MarkerOptions()
                    .position(Container.getUnfiltered_list().get(i).getLocation())
                    .title(Container.getUnfiltered_list().get(i).getAddress()));
        }
        for(int i = 0; i < Container.getFiltered_list().size(); i++) {
            map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .position(Container.getFiltered_list().get(i).getLocation())
                    .title(Container.getFiltered_list().get(i).getAddress()));
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull final Marker marker) {
        if(Container.getLast_location() != null)
            marker.setSnippet("Distance: " + df.format(calculateLength(marker.getPosition(), Container.getLast_location())) + " KM");
        else
            marker.setSnippet("Distance: unknown");

        return false;
    }

    // Calculate distance between marker and user
    private double calculateLength(LatLng marker, Location user)
    {
        double lat1 = deg2grad(marker.latitude);
        double lat2 = deg2grad(user.getLatitude());
        double long1 = deg2grad(marker.longitude);
        double long2 = deg2grad(user.getLongitude());

        double deltalat = (lat2-lat1)/2;
        double deltalong = (long2-long1)/2;


        return (2 * 6371 * Math.asin(Math.sqrt(Math.sin(deltalat)*Math.sin(deltalat)+Math.cos(lat1)*Math.cos(lat2)*(Math.sin(deltalong)*Math.sin(deltalong)))));
    }

    private double deg2grad(double degree)
    {
        double pi = 3.14;
        return (degree * (pi/180));
    }
}