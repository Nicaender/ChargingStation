package com.nzse_chargingstation.app.fragments;

import static android.view.View.GONE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.activities.ReportActivity;
import com.nzse_chargingstation.app.classes.ChargingStation;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;

import java.util.ArrayList;

public class MapsFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap googleMap;
    private ImageView imgViewRadius;
    private ImageButton imgBtnFavorite;
    private ImageButton imgBtnReport;
    private MaterialSpinner spRadiusValue;
    private Marker clickedMarker;
    private Thread markerThread;
    private boolean stopThread = false, updateMarker = false, forceUpdate = false;
    private int favoriteX, reportX, spinnerX, eyeX;

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

            googleMap.setOnMarkerClickListener(marker -> {
                // Triggered when user click any marker on the map
                clickedMarker = marker;
                if(ContainerAndGlobal.getCurrentLocation() != null)
                    marker.setSnippet("Distance: " + ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(marker.getPosition(), ContainerAndGlobal.getCurrentLocation())) + " KM, click for more info");
                else
                    marker.setSnippet("Distance: unknown");

                if(ContainerAndGlobal.indexSearchFavorites(marker.getPosition()) != -1)
                    imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_24", "drawable", requireContext().getPackageName()));
                else
                    imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_border_24", "drawable", requireContext().getPackageName()));
                if(imgBtnFavorite.getVisibility() == View.GONE)
                {
                    imgBtnFavorite.setVisibility(View.VISIBLE);
                    imgBtnReport.setVisibility(View.VISIBLE);
                }
                ObjectAnimator animation = ObjectAnimator.ofFloat(imgBtnFavorite, "translationX", favoriteX);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnReport, "translationX", reportX);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(spRadiusValue, "translationX", 1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgViewRadius, "translationX", 1000f);
                animation.setDuration(250);
                animation.start();

                return false;
            });

            googleMap.setOnInfoWindowClickListener(marker -> {
                int index = ContainerAndGlobal.indexSearchFavorites(marker.getPosition());
                if(index == -1) // -1 means that charging station is not in favorite list
                {
                    int indexCs = ContainerAndGlobal.indexSearchChargingStation(marker.getPosition());
                    ContainerAndGlobal.getFavoriteList().add(ContainerAndGlobal.getChargingStationList().get(indexCs));
                    ContainerAndGlobal.getChargingStationList().remove(indexCs);
                    ContainerAndGlobal.saveData(true, requireContext());
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    if(ContainerAndGlobal.indexSearchFavorites(marker.getPosition()) != -1)
                        imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_24", "drawable", requireContext().getPackageName()));
                    else
                        imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_border_24", "drawable", requireContext().getPackageName()));
                }
                else
                {
                    int filtered = ContainerAndGlobal.addChargingStation(ContainerAndGlobal.getFavoriteList().get(index).getMyIndex(), ContainerAndGlobal.getFavoriteList().get(index));
                    if(filtered == 2)
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    else if(filtered == 1)
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    ContainerAndGlobal.getFavoriteList().remove(index);
                    ContainerAndGlobal.saveData(true, requireContext());
                    if(ContainerAndGlobal.indexSearchFavorites(marker.getPosition()) != -1)
                        imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_24", "drawable", requireContext().getPackageName()));
                    else
                        imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_border_24", "drawable", requireContext().getPackageName()));
                }
            });

            googleMap.setOnInfoWindowLongClickListener(marker -> {
                if(!reportChargingStation(marker))
                    return;

                startActivity(new Intent(getActivity(), ReportActivity.class));
            });

            googleMap.setOnInfoWindowCloseListener(marker -> {
                ObjectAnimator animation = ObjectAnimator.ofFloat(imgBtnFavorite, "translationX", -1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnReport, "translationX", -1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(spRadiusValue, "translationX", spinnerX);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgViewRadius, "translationX", eyeX);
                animation.setDuration(250);
                animation.start();
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
            float zoomLevel = (float) 15.0;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, zoomLevel));
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
        imgViewRadius =  view.findViewById(R.id.imageViewRadius);
        imgBtnFavorite = view.findViewById(R.id.imageButtonFavorite);
        imgBtnReport = view.findViewById(R.id.imageButtonReport);
        spRadiusValue = view.findViewById(R.id.spinnerRadiusValue);
        favoriteX = (int) imgBtnFavorite.getTranslationX();
        reportX = (int) imgBtnReport.getTranslationX();
        eyeX = (int) imgViewRadius.getTranslationX();
        spinnerX = (int) spRadiusValue.getTranslationX();

        imgBtnFavorite.setVisibility(GONE);
        imgBtnReport.setVisibility(GONE);
        ObjectAnimator animation = ObjectAnimator.ofFloat(imgBtnFavorite, "translationX", -1000f);
        animation.setDuration(250);
        animation.start();
        animation = ObjectAnimator.ofFloat(imgBtnReport, "translationX", -1000f);
        animation.setDuration(250);
        animation.start();

        ArrayList<String> items = new ArrayList<>();
        for(int i = 0; i < 100; i++)
        {
            items.add(i + " KM");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        spRadiusValue.setAdapter(adapter);

        spRadiusValue.setOnItemSelectedListener((view1, position, id, item) -> {
            if(googleMap.isMyLocationEnabled())
            {
                ContainerAndGlobal.setFilterRange(position);
                if(updateMarker)
                    forceUpdate = true;
                updateMarker = true;
            }
            else
                Toast.makeText(getContext(), "Location is unknown", Toast.LENGTH_LONG).show();
        });

        // Implementation of favorite button
        imgBtnFavorite.setOnClickListener(v -> {
            int index = ContainerAndGlobal.indexSearchFavorites(clickedMarker.getPosition());
            if(index == -1) // -1 means that charging station is not in favorite list
            {
                int indexCs = ContainerAndGlobal.indexSearchChargingStation(clickedMarker.getPosition());
                ContainerAndGlobal.getFavoriteList().add(ContainerAndGlobal.getChargingStationList().get(indexCs));
                ContainerAndGlobal.getChargingStationList().remove(indexCs);
                ContainerAndGlobal.saveData(true, requireContext());
                clickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                if(ContainerAndGlobal.indexSearchFavorites(clickedMarker.getPosition()) != -1)
                    imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_24", "drawable", requireContext().getPackageName()));
                else
                    imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_border_24", "drawable", requireContext().getPackageName()));
            }
            else
            {
                int filtered = ContainerAndGlobal.addChargingStation(ContainerAndGlobal.getFavoriteList().get(index).getMyIndex(), ContainerAndGlobal.getFavoriteList().get(index));
                if(filtered == 2)
                    clickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                else if(filtered == 1)
                    clickedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                ContainerAndGlobal.getFavoriteList().remove(index);
                ContainerAndGlobal.saveData(true, requireContext());
                if(ContainerAndGlobal.indexSearchFavorites(clickedMarker.getPosition()) != -1)
                    imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_24", "drawable", requireContext().getPackageName()));
                else
                    imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_border_24", "drawable", requireContext().getPackageName()));
            }
        });

        // Implementation of report button
        imgBtnReport.setOnClickListener(v -> {
            if(!reportChargingStation(clickedMarker))
                return;

            startActivity(new Intent(getActivity(), ReportActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if(updateMarker)
            forceUpdate = true;
        updateMarker = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        stopThread = true;
        try {
            markerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        ContainerAndGlobal.setReportedChargingStation(ContainerAndGlobal.searchChargingStationEverywhere(marker.getPosition()));
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
                    for(int i = 0 ; i < ContainerAndGlobal.getChargingStationList().size(); i++)
                    {
                        if(stopThread)
                            return;
                        if(forceUpdate)
                            break;
                        ChargingStation tmp = ContainerAndGlobal.getChargingStationList().get(i);
                        if(ContainerAndGlobal.getCurrentLocation() != null && ContainerAndGlobal.calculateLength(tmp.getLocation(), ContainerAndGlobal.getCurrentLocation()) > ContainerAndGlobal.getMaxViewRange())
                            break;
                        if(tmp.isFiltered())
                            requireActivity().runOnUiThread(() -> googleMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                    .position(tmp.getLocation())
                                    .title(tmp.getStrasse() + ' ' + tmp.getHausnummer())));
                        else
                            requireActivity().runOnUiThread(() -> googleMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                    .position(tmp.getLocation())
                                    .title(tmp.getStrasse() + ' ' + tmp.getHausnummer())));
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
}