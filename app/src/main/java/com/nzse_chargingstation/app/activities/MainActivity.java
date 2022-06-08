package com.nzse_chargingstation.app.activities;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ChargingStation;
import com.nzse_chargingstation.app.classes.ChargingStationDistanceComparator;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.Favorite;
import com.nzse_chargingstation.app.fragments.FavoritesFragment;
import com.nzse_chargingstation.app.fragments.MapsFragment;
import com.nzse_chargingstation.app.fragments.MyCarsFragment;
import com.nzse_chargingstation.app.fragments.SettingsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

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

        if(ContainerAndGlobal.isFirstTime())
        {
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
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();
            String json = sharedPrefs.getString(TAG, "");
            Type type = new TypeToken<List<Favorite>>() {}.getType();
            List<Favorite> arrayList = gson.fromJson(json, type);
            if(arrayList != null)
            {
                for(int i = 0; i < arrayList.size(); i++)
                {
                    int index = ContainerAndGlobal.indexSearchInList(arrayList.get(i).getFavoriteCs().getLocation());
                    if(index != -1)
                    {
                        Favorite tmp = new Favorite(ContainerAndGlobal.getChargingStationList().get(index), index);
                        ContainerAndGlobal.getChargingStationList().remove(index);
                        ContainerAndGlobal.getFavoriteList().add(tmp);
                    }
                }
            }
            ContainerAndGlobal.setFirstTime(false);
        }

        requestLocationPermission();

        // Initialization
        BottomNavigationView bottom_nav_bar = findViewById(R.id.bottomNavbar);
        bottom_nav_bar.setSelectedItemId(R.id.navMaps);

        // Saving state of our app
        // using SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        final boolean isDarkModeOn = sharedPreferences.getBoolean("isDarkModeOn", false);
        final int maxViewRange = sharedPreferences.getInt("maxViewRange", 10);

        // When user reopens the app
        // after applying dark/light mode
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        ContainerAndGlobal.setMaxViewRange(maxViewRange);

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
                case R.id.navMaps:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapsFragment).commit();
                    return true;
                case R.id.navMyCars:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, myCarsFragment).commit();
                    return true;
                case R.id.navFavorites:
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, favoritesFragment).commit();
                    return true;
                case R.id.navSettings:
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
                            if(ContainerAndGlobal.isFirstTimeGPSEnabled())
                            {
                                ContainerAndGlobal.setFirstTimeGPSEnabled(false);
                                ContainerAndGlobal.getChargingStationList().sort(new ChargingStationDistanceComparator());
                                startActivity(new Intent(this, MainActivity.class));
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

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(ContainerAndGlobal.getFavoriteList());

        editor.putString(TAG, json);
        editor.commit();
    }
}