package com.nzse_chargingstation.app.fragments;

import static android.view.View.GONE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.activities.InfoActivity;
import com.nzse_chargingstation.app.activities.ReportActivity;
import com.nzse_chargingstation.app.activities.SearchActivity;
import com.nzse_chargingstation.app.classes.ChargingStation;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.InfoWindowAdapter;

import java.util.ArrayList;

public class MapsFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap googleMap;
    private ImageButton imgBtnFavorite;
    private ImageButton imgBtnReport;
    private ImageButton imgBtnMyLocation;
    private ImageView imgViewFavRepBackground;
    private MaterialSpinner spRadiusValue;
    private Marker clickedMarker;
    private Thread markerThread;
    private boolean stopThread = false, updateMarker = false, forceUpdate = false, updateLocationUI = true;
    private int favoriteY, reportY, spinnerX, locationX, backgroundY;
    private final float zoomLevel = (float) 15.0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

        stopThread = false;
        updateMarker = false;
        forceUpdate = false;
        threadInitialize();

        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(requireActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(mMap -> {
            googleMap = mMap;
            if(ContainerAndGlobal.isDarkmode())
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_in_night));

            googleMap.getUiSettings().setMyLocationButtonEnabled(false);

            InfoWindowAdapter markerInfoWindowAdapter = new InfoWindowAdapter(requireContext());
            googleMap.setInfoWindowAdapter(markerInfoWindowAdapter);

            googleMap.setOnMarkerClickListener(marker -> {
                // Triggered when user click any marker on the map
                clickedMarker = marker;
                if(ContainerAndGlobal.getCurrentLocation() != null)
                    marker.setSnippet("Distance: " + ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(marker.getPosition(), ContainerAndGlobal.getCurrentLocation())) + " KM, click for more info");
                else
                    marker.setSnippet("Distance: unknown");

                ContainerAndGlobal.setClickedChargingStation(ContainerAndGlobal.searchChargingStation(marker.getPosition()));

                if(ContainerAndGlobal.isInFavorite(ContainerAndGlobal.getClickedChargingStation()))
                    imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_24", "drawable", requireContext().getPackageName()));
                else
                    imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_border_24", "drawable", requireContext().getPackageName()));

                if(imgViewFavRepBackground.getVisibility() == View.GONE)
                {
                    imgViewFavRepBackground.setVisibility(View.VISIBLE);
                    imgBtnFavorite.setVisibility(View.VISIBLE);
                    imgBtnReport.setVisibility(View.VISIBLE);
                }
                ObjectAnimator animation = ObjectAnimator.ofFloat(imgViewFavRepBackground, "translationY", backgroundY);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnFavorite, "translationY", favoriteY);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnReport, "translationY", reportY);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(spRadiusValue, "translationX", -1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnMyLocation, "translationX", 1000f);
                animation.setDuration(250);
                animation.start();

                return false;
            });

            googleMap.setOnInfoWindowClickListener(marker -> googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), zoomLevel)));

            googleMap.setOnInfoWindowLongClickListener(marker -> startActivity(new Intent(getActivity(), InfoActivity.class)));

            googleMap.setOnInfoWindowCloseListener(marker -> {
                ObjectAnimator animation = ObjectAnimator.ofFloat(imgViewFavRepBackground, "translationY", 1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnFavorite, "translationY", 1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnReport, "translationY", 1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(spRadiusValue, "translationX", spinnerX);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnMyLocation, "translationX", locationX);
                animation.setDuration(250);
                animation.start();
            });

            googleMap.setOnCameraIdleListener(() -> updateLocationUI = true);

            googleMap.setOnCameraMoveListener(() -> {
                if(updateLocationUI && ContainerAndGlobal.getCurrentLocation() != null)
                    imgBtnMyLocation.setImageResource(getResources().getIdentifier("ic_baseline_location_searching_24", "drawable", requireContext().getPackageName()));
            });

            enableMyLocation();
            markerThread.start();
            if(updateMarker)
                forceUpdate = true;
            updateMarker = true;

            LatLng start;

            if(ContainerAndGlobal.getCurrentLocation() != null)
                start = new LatLng(ContainerAndGlobal.getCurrentLocation().getLatitude(), ContainerAndGlobal.getCurrentLocation().getLongitude());
            else
                start = new LatLng(49.8728, 8.6512);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, zoomLevel));

            if(ContainerAndGlobal.getLastCameraPosition() != null)
            {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ContainerAndGlobal.getLastCameraPosition().target, ContainerAndGlobal.getLastCameraPosition().zoom));
                if(ContainerAndGlobal.calculateLength(ContainerAndGlobal.getLastCameraPosition().target, ContainerAndGlobal.getCurrentLocation()) < 0.1)
                    imgBtnMyLocation.setImageResource(getResources().getIdentifier("ic_baseline_my_location_24", "drawable", requireContext().getPackageName()));
                ContainerAndGlobal.setLastCameraPosition(null);
            }

            if(ContainerAndGlobal.getZoomToThisChargingStation() != null)
            {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ContainerAndGlobal.getZoomToThisChargingStation().getLocation(), zoomLevel));
                ContainerAndGlobal.setZoomToThisChargingStation(null);
            }
/*
            Here are the approximate zoom levels and what they do :
            1: World
            5: Landmass/continent
            10: City
            15: Streets
            20: Buildings
*/
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imgViewRadius = view.findViewById(R.id.imageViewRadius);
        imgBtnFavorite = view.findViewById(R.id.imageButtonFavorite);
        imgBtnReport = view.findViewById(R.id.imageButtonReport);
        imgBtnMyLocation = view.findViewById(R.id.imageButtonMyLocation);
        ImageButton imgBtnSearch = view.findViewById(R.id.imageButtonSearch);
        imgViewFavRepBackground = view.findViewById(R.id.imageViewFavRepBackground);
        spRadiusValue = view.findViewById(R.id.spinnerRadiusValue);
        backgroundY = (int) imgViewFavRepBackground.getTranslationY();
        favoriteY = (int) imgBtnFavorite.getTranslationY();
        reportY = (int) imgBtnReport.getTranslationY();
        spinnerX = (int) spRadiusValue.getTranslationX();
        locationX = (int) imgBtnMyLocation.getTranslationX();

        imgViewFavRepBackground.setVisibility(GONE);
        imgBtnFavorite.setVisibility(GONE);
        imgBtnReport.setVisibility(GONE);
        ObjectAnimator animation = ObjectAnimator.ofFloat(imgViewFavRepBackground, "translationY", 1000f);
        animation.setDuration(250);
        animation.start();
        animation = ObjectAnimator.ofFloat(imgBtnFavorite, "translationY", 1000f);
        animation.setDuration(250);
        animation.start();
        animation = ObjectAnimator.ofFloat(imgBtnReport, "translationY", 1000f);
        animation.setDuration(250);
        animation.start();

        ArrayList<String> items = new ArrayList<>();
        for(int i = 0; i < 100; i++)
        {
            items.add(i + " KM");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        spRadiusValue.setAdapter(adapter);
        spRadiusValue.setBackground(AppCompatResources.getDrawable(requireContext(), R.drawable.item_curved));

        spRadiusValue.setOnItemSelectedListener((view1, position, id, item) -> {
            if(googleMap.isMyLocationEnabled())
            {
                ContainerAndGlobal.setFilterRangeAndApply(position);
                if(updateMarker)
                    forceUpdate = true;
                updateMarker = true;
            }
            else
                Toast.makeText(getContext(), "Location is unknown", Toast.LENGTH_LONG).show();
        });

        // Implementation of favorite button
        imgBtnFavorite.setOnClickListener(v -> {
            ChargingStation tmp = ContainerAndGlobal.searchChargingStation(clickedMarker.getPosition());
            if(ContainerAndGlobal.isInFavorite(tmp))
            {
                ContainerAndGlobal.removeFavorite(tmp);
                assert tmp != null;
                assignColor(clickedMarker, tmp);
                imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_border_24", "drawable", requireContext().getPackageName()));
            }
            else
            {
                ContainerAndGlobal.addFavorite(tmp);
                clickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_24", "drawable", requireContext().getPackageName()));
            }
            ContainerAndGlobal.saveData(true, requireContext());
        });

        // Implementation of report button
        imgBtnReport.setOnClickListener(v -> {
            if(!reportChargingStation(clickedMarker))
                return;

            startActivity(new Intent(getActivity(), ReportActivity.class));
        });

        if(ContainerAndGlobal.getCurrentLocation() == null)
            imgBtnMyLocation.setVisibility(GONE);
        // Implementation of custom my location
        imgBtnMyLocation.setOnClickListener(v -> {
            LatLng tmp = new LatLng(ContainerAndGlobal.getCurrentLocation().getLatitude(), ContainerAndGlobal.getCurrentLocation().getLongitude());
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tmp, zoomLevel));
            imgBtnMyLocation.setImageResource(getResources().getIdentifier("ic_baseline_my_location_24", "drawable", requireContext().getPackageName()));
            updateLocationUI = false;
        });

        // Implementation of search button
        imgBtnSearch.setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchActivity.class)));

        // Implementation to limit total charging stations on the map
        final int size = 12;
        String[] maxView = new String[size];
        for(int i = 0; i < size; i++)
        {
            maxView[i] = String.valueOf((i+1) * 25);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.how_many_charging_station_to_show_on_the_map)
                .setItems(maxView, (dialog, which) -> limitMaxChargingStation(Integer.parseInt(maxView[which])));
        AlertDialog dialog = builder.create();
        imgViewRadius.setOnClickListener(v -> dialog.show());
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if(ContainerAndGlobal.getZoomToThisChargingStationOnPause() != null)
        {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ContainerAndGlobal.getZoomToThisChargingStationOnPause().getLocation(), zoomLevel));
            if(ContainerAndGlobal.getCurrentLocation() != null && // Check if location is turned on or it is outside the max view range and is not already in marked list
                    ContainerAndGlobal.indexOfChargingStation(ContainerAndGlobal.getZoomToThisChargingStationOnPause()) > ContainerAndGlobal.getMaxViewChargingStation() &&
                            !ContainerAndGlobal.isInMarkedList(ContainerAndGlobal.getZoomToThisChargingStationOnPause()))
            {
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(ContainerAndGlobal.getZoomToThisChargingStationOnPause().getLocation())
                        .title(ContainerAndGlobal.getZoomToThisChargingStationOnPause().getStrasse() + ' ' + ContainerAndGlobal.getZoomToThisChargingStationOnPause().getHausnummer()));
                ContainerAndGlobal.getMarkedList().add(ContainerAndGlobal.getZoomToThisChargingStationOnPause());
            }
            ContainerAndGlobal.setZoomToThisChargingStationOnPause(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(googleMap != null)
            ContainerAndGlobal.setLastCameraPosition(googleMap.getCameraPosition());
        mMapView.onDestroy();
        stopThread = true;
        try {
            markerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Mark a charging station as defective
     * @param marker from clicked location in google map
     * @return true if marker exists & the same location of charging station exists
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean reportChargingStation(Marker marker)
    {
        ContainerAndGlobal.setReportedChargingStation(ContainerAndGlobal.searchChargingStation(marker.getPosition()));
        if(ContainerAndGlobal.getReportedChargingStation() != null)
            ContainerAndGlobal.setReportedMarker(marker);
        return ContainerAndGlobal.getReportedChargingStation() != null;
    }

    /**
     * Initialize a thread function
     */
    private void threadInitialize()
    {
        markerThread = new Thread(() -> {
            while(true)
            {
                if(stopThread)
                    return;
                if(!updateMarker)
                {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(0, 100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    forceUpdate = false;
                    requireActivity().runOnUiThread(() -> googleMap.clear());
                    for(int i = 0; i < ContainerAndGlobal.getMarkedList().size(); i++)
                    {
                        if(stopThread)
                            return;
                        if(forceUpdate)
                            break;
                        ChargingStation tmp = ContainerAndGlobal.getMarkedList().get(i);
                        requireActivity().runOnUiThread(() -> googleMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                .position(tmp.getLocation())
                                .title(tmp.getStrasse() + ' ' + tmp.getHausnummer())));
                        try {
                            //noinspection BusyWait
                            Thread.sleep(0, 100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for(int i = 0 ; i < ContainerAndGlobal.getFavoriteList().size(); i++)
                    {
                        if(stopThread)
                            return;
                        if(forceUpdate)
                            break;
                        ChargingStation tmp = ContainerAndGlobal.getFavoriteList().get(i);
                        requireActivity().runOnUiThread(() -> googleMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                .position(tmp.getLocation())
                                .title(tmp.getStrasse() + ' ' + tmp.getHausnummer())));
                        try {
                            //noinspection BusyWait
                            Thread.sleep(0, 100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    int counter = 0;
                    for(int i = 0 ; i < ContainerAndGlobal.getChargingStationList().size(); i++)
                    {
                        if(stopThread)
                            return;
                        if(forceUpdate)
                            break;
                        ChargingStation tmp = ContainerAndGlobal.getChargingStationList().get(i);
                        if(ContainerAndGlobal.getCurrentLocation() != null && counter >= ContainerAndGlobal.getMaxViewChargingStation())
                            break;
                        if(!tmp.isShowMarker())
                            continue;
                        assignMarker(tmp);
                        counter++;
                        try {
                            //noinspection BusyWait
                            Thread.sleep(0, 100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(!forceUpdate)
                        updateMarker = false;
                }
            }
        });
    }

    /**
     * Limiting charging stations shown on the map excluding favorites and marked list
     * @param limit is the total charging station that should be shown
     */
    private void limitMaxChargingStation(int limit)
    {
        if(ContainerAndGlobal.getMaxViewChargingStation() == limit)
            return;
        ContainerAndGlobal.setMaxViewChargingStation(limit);
        for(int i = 0; i < ContainerAndGlobal.getMarkedList().size(); i++)
        {
            if(ContainerAndGlobal.indexOfChargingStation(ContainerAndGlobal.getMarkedList().get(i)) > ContainerAndGlobal.getMaxViewChargingStation())
            {
                ContainerAndGlobal.getMarkedList().remove(i);
                i--;
            }
        }
        if(updateMarker)
            forceUpdate = true;
        updateMarker = true;
        SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("maxChargingStations", ContainerAndGlobal.getMaxViewChargingStation());
        editor.apply();
    }

    /**
     * Assigning a marker for a charging station and automatically filters its color
     * @param chargingStation is the charging station that wants to be added
     */
    private void assignMarker(ChargingStation chargingStation)
    {
        if(chargingStation.isFiltered())
            requireActivity().runOnUiThread(() -> googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .position(chargingStation.getLocation())
                    .title(chargingStation.getStrasse() + ' ' + chargingStation.getHausnummer())));
        else
        {
            if(chargingStation.getArtDerLadeeinrichtung().equals("Normalladeeinrichtung"))
                requireActivity().runOnUiThread(() -> googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .position(chargingStation.getLocation())
                        .title(chargingStation.getStrasse() + ' ' + chargingStation.getHausnummer())));
            else
                requireActivity().runOnUiThread(() -> googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        .position(chargingStation.getLocation())
                        .title(chargingStation.getStrasse() + ' ' + chargingStation.getHausnummer())));
        }
    }

    /**
     * Assign a marker color automatically (Not for favorite)
     * @param marker is the marker that wants to be changed
     * @param chargingStation is the parameter for its filter
     */
    private void assignColor(Marker marker, ChargingStation chargingStation)
    {
        if(chargingStation.isFiltered())
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        else
        {
            if(chargingStation.getArtDerLadeeinrichtung().equals("Normalladeeinrichtung"))
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            else
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }
    }
}