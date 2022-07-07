package com.nzse_chargingstation.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.classes.ChargingStation;
import com.nzse_chargingstation.app.classes.ChargingStationDistanceComparator;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.Defective;
import com.nzse_chargingstation.app.classes.LocaleHelper;
import com.nzse_chargingstation.app.classes.RoutePlan;
import com.nzse_chargingstation.app.fragments.FavoritesFragment;
import com.nzse_chargingstation.app.fragments.MapsFragment;
import com.nzse_chargingstation.app.fragments.RouteFragment;
import com.nzse_chargingstation.app.fragments.SettingsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private long pressedTime;

    private FusedLocationProviderClient fusedLocationClient;

    private BottomNavigationView bottomNavBar;

    private final int REQUEST_LOCATION_PERMISSION = 1;

    @SuppressLint({"NonConstantResourceId", "MissingPermission"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MapsFragment mapsFragment = new MapsFragment();
        final FavoritesFragment favoritesFragment = new FavoritesFragment();
        final SettingsFragment settingsFragment = new SettingsFragment();
        final RouteFragment routeFragment = new RouteFragment();

        try {
            // get current location
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            if(ContainerAndGlobal.isFirstTime()) {
                ContainerAndGlobal.setFirstTime(false);

                // Clearing old data in case the user closes the app and then reopens it
                ContainerAndGlobal.getChargingStationList().clear();
                ContainerAndGlobal.getFavoriteList().clear();
                ContainerAndGlobal.getFilteredList().clear();
                ContainerAndGlobal.getMarkedList().clear();
                ContainerAndGlobal.getRoutePlanList().clear();
                ContainerAndGlobal.getDefectiveList().clear();

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
                getOldData();
            }

            if(ContainerAndGlobal.getCurrentLocation() != null && ContainerAndGlobal.isFirstTimeGPSEnabled()) {
                ContainerAndGlobal.setFirstTimeGPSEnabled(false);
                ContainerAndGlobal.getChargingStationList().sort(new ChargingStationDistanceComparator());
            }

            requestLocationPermission();

            // Initialization
            bottomNavBar = findViewById(R.id.bottomNavbar);
            bottomNavBar.setSelectedItemId(R.id.navMaps);

            // Saving state of our app
            // using SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
            ContainerAndGlobal.setDarkmode(sharedPreferences.getBoolean("isDarkModeOn", false));
            ContainerAndGlobal.setMaxViewChargingStation(sharedPreferences.getInt("maxChargingStations", 100));

            // When user reopens the app
            // after applying dark/light mode
            if (ContainerAndGlobal.isDarkmode()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            ContainerAndGlobal.setMaxViewChargingStation(ContainerAndGlobal.getMaxViewChargingStation());

            if(ContainerAndGlobal.isChangedSetting()) {
                bottomNavBar.setSelectedItemId(R.id.navSettings);
                ContainerAndGlobal.setChangedSetting(false);
            }
            else
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, mapsFragment).commit();


            // Implementation of bottom navigation bar
            bottomNavBar.setOnItemSelectedListener(item -> {

                switch (item.getItemId()) {
                    case R.id.navMaps:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, mapsFragment).commit();
                        return true;
                    case R.id.navRoute:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, routeFragment).commit();
                        return true;
                    case R.id.navFavorites:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, favoritesFragment).commit();
                        return true;
                    case R.id.navSettings:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, settingsFragment).commit();
                        return true;
                }
                return false;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * A function to request the user location, and what to do when location is acquired
     */
    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        try {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
            if(EasyPermissions.hasPermissions(this, perms)) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                ContainerAndGlobal.setCurrentLocation(location);
                                if(ContainerAndGlobal.isFirstTimeGPSEnabled()) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            ContainerAndGlobal.saveData(0, getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (pressedTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                ContainerAndGlobal.resetVariables();
                finish();
            } else {
                Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
            }
            pressedTime = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ContainerAndGlobal.getZoomToThisChargingStationOnDefective() != null)
            switchFragment(0);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "de"));
    }

    /**
     * Importing favorites and defectives from shared preferences
     */
    private void getOldData() {
        try {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();
            String json = sharedPrefs.getString("FavoriteList", "");
            Type type = new TypeToken<List<ChargingStation>>() {}.getType();
            List<ChargingStation> oldFavorites = gson.fromJson(json, type);
            if(oldFavorites != null) {
                for(int i = 0; i < oldFavorites.size(); i++) {
                    ChargingStation tmp = ContainerAndGlobal.searchChargingStation(oldFavorites.get(i).getPosition());
                    ContainerAndGlobal.addFavorite(tmp);
                }
            }
            json = sharedPrefs.getString("DefectiveList", "");
            type = new TypeToken<List<Defective>>() {}.getType();
            List<Defective> oldDefectives = gson.fromJson(json, type);
            if(oldDefectives != null) {
                for(int i = 0; i < oldDefectives.size(); i++) {
                    ChargingStation tmp = ContainerAndGlobal.searchChargingStation(oldDefectives.get(i).getDefectiveCs().getPosition());
                    Defective defectiveTmp = new Defective(tmp, oldDefectives.get(i).isFavorite(), oldDefectives.get(i).getReason());
                    defectiveTmp.setMarked(oldDefectives.get(i).isMarked());
                    ContainerAndGlobal.addDefective(defectiveTmp);
                }
            }
            json = sharedPrefs.getString("RouteList", "");
            type = new TypeToken<List<RoutePlan>>() {}.getType();
            List<RoutePlan> oldRoutePlans = gson.fromJson(json, type);
            if(oldRoutePlans != null) {
                for(int i = 0; i < oldRoutePlans.size(); i++) {
                    RoutePlan newRoutePlan = new RoutePlan(oldRoutePlans.get(i).getName());
                    for(int j = 0; j < oldRoutePlans.get(i).getChargingStationRoutes().size(); j++) {
                        newRoutePlan.getChargingStationRoutes().add(ContainerAndGlobal.searchChargingStation(oldRoutePlans.get(i).getChargingStationRoutes().get(j).getPosition()));
                    }
                    ContainerAndGlobal.getRoutePlanList().add(newRoutePlan);
                }
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * A function to switch the fragment from inside a fragment
     * @param option is the option, which fragment to be changed
     */
    public void switchFragment(int option) {
        try {
            if(option == 0)
                bottomNavBar.setSelectedItemId(R.id.navMaps);
            else if(option == 1)
                bottomNavBar.setSelectedItemId(R.id.navRoute);
            else if(option == 2)
                bottomNavBar.setSelectedItemId(R.id.navFavorites);
            else
                bottomNavBar.setSelectedItemId(R.id.navSettings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}