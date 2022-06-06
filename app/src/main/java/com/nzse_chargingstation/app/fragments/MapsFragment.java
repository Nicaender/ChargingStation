package com.nzse_chargingstation.app.fragments;

import static android.view.View.GONE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.activities.ReportActivity;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.Favorite;

public class MapsFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap googleMap;
    private TextView tvRadiusValue, tvRadius, tvKM;
    private ImageButton imgBtnArrowUp;
    private ImageButton imgBtnArrowDown;
    private ImageButton imgBtnFavorite;
    private ImageButton imgBtnReport;
    private Marker clickedMarker;
    private static int radiusValue = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_maps, container, false);

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

                if(ContainerAndGlobal.searchInFavorites(marker.getPosition()) != -1)
                    imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_24", "drawable", requireContext().getPackageName()));
                else
                    imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_border_24", "drawable", requireContext().getPackageName()));
                tvRadiusValue.setVisibility(GONE);
                tvRadius.setVisibility(GONE);
                tvKM.setVisibility(GONE);
                imgBtnArrowUp.setVisibility(GONE);
                imgBtnArrowDown.setVisibility(GONE);
                imgBtnFavorite.setVisibility(View.VISIBLE);
                imgBtnReport.setVisibility(View.VISIBLE);

                return false;
            });

            googleMap.setOnInfoWindowClickListener(marker -> {
                int index = ContainerAndGlobal.searchInFavorites(marker.getPosition());
                if(index == -1) // -1 means that charging station is not in favorite list
                {
                    int indexCs = ContainerAndGlobal.indexSearchInList(marker.getPosition());
                    Favorite tmp = new Favorite(ContainerAndGlobal.getChargingStationList().get(indexCs), indexCs);
                    ContainerAndGlobal.getChargingStationList().remove(indexCs);
                    ContainerAndGlobal.getFavoriteList().add(tmp);
                    googleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                            .position(tmp.getFavoriteCs().getLocation())
                            .title(tmp.getFavoriteCs().getStrasse()));
                }
                else
                {
                    int filtered = ContainerAndGlobal.addChargingStation(ContainerAndGlobal.getFavoriteList().get(index).getIndexInArray(), ContainerAndGlobal.getFavoriteList().get(index).getFavoriteCs());
                    if(filtered == 2)
                        googleMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                .position(ContainerAndGlobal.getFavoriteList().get(index).getFavoriteCs().getLocation())
                                .title(ContainerAndGlobal.getFavoriteList().get(index).getFavoriteCs().getStrasse()));
                    else if(filtered == 1)
                        googleMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                                .position(ContainerAndGlobal.getFavoriteList().get(index).getFavoriteCs().getLocation())
                                .title(ContainerAndGlobal.getFavoriteList().get(index).getFavoriteCs().getStrasse()));
                    ContainerAndGlobal.getFavoriteList().remove(index);
                }
                marker.remove();
            });

            googleMap.setOnInfoWindowLongClickListener(marker -> {
                if(!reportChargingStation(marker))
                    return;

                startActivity(new Intent(getActivity(), ReportActivity.class));
            });

            googleMap.setOnInfoWindowCloseListener(marker -> {
                tvRadiusValue.setVisibility(View.VISIBLE);
                tvRadius.setVisibility(View.VISIBLE);
                tvKM.setVisibility(View.VISIBLE);
                imgBtnArrowUp.setVisibility(View.VISIBLE);
                imgBtnArrowDown.setVisibility(View.VISIBLE);
                imgBtnFavorite.setVisibility(GONE);
                imgBtnReport.setVisibility(GONE);
            });

            enableMyLocation();
            addCSToMaps();
            if(ContainerAndGlobal.getCurrentLocation() != null)
            {
                LatLng start = new LatLng(ContainerAndGlobal.getCurrentLocation().getLatitude(), ContainerAndGlobal.getCurrentLocation().getLongitude());
                float zoomLevel = (float) 15.0;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, zoomLevel));
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
        tvRadiusValue = view.findViewById(R.id.textViewRadiusValue);
        tvRadius = view.findViewById(R.id.textViewRadius);
        tvKM = view.findViewById(R.id.textViewKM);
        imgBtnArrowUp = view.findViewById(R.id.imageButtonArrowUp);
        imgBtnArrowDown = view.findViewById(R.id.imageButtonArrowDown);
        imgBtnFavorite = view.findViewById(R.id.imageButtonFavorite);
        imgBtnReport = view.findViewById(R.id.imageButtonReport);

        // Setting the start value of filter range
        if(radiusValue < 10)
        {
            String tmp = "0" + radiusValue;
            tvRadiusValue.setText(tmp);
        }
        else
            tvRadiusValue.setText(String.valueOf(radiusValue));

        // Implementation of increment from radius_value
        imgBtnArrowUp.setOnClickListener(v -> {
            if(radiusValue < 99)
            {
                radiusValue++;
                if(radiusValue < 10)
                {
                    String tmp = "0" + radiusValue;
                    tvRadiusValue.setText(tmp);
                }
                else
                    tvRadiusValue.setText(String.valueOf(radiusValue));
            }
            if(googleMap.isMyLocationEnabled())
            {
                ContainerAndGlobal.setFilterRange(radiusValue);
                ContainerAndGlobal.enableFilter();
                addCSToMaps();
            }
            else
                Toast.makeText(getContext(), "Location is unknown", Toast.LENGTH_LONG).show();
        });

        // Implementation of decrement from radius_value
        imgBtnArrowDown.setOnClickListener(v -> {
            if(radiusValue > 0)
            {
                radiusValue--;
                if(radiusValue < 10)
                {
                    String tmp = "0" + radiusValue;
                    tvRadiusValue.setText(tmp);
                }
                else
                    tvRadiusValue.setText(String.valueOf(radiusValue));
            }
            if(googleMap.isMyLocationEnabled())
            {
                ContainerAndGlobal.setFilterRange(radiusValue);
                ContainerAndGlobal.enableFilter();
                addCSToMaps();
            }
            else
                Toast.makeText(getContext(), "Location is unknown", Toast.LENGTH_LONG).show();
        });

        // Implementation of favorite button
        imgBtnFavorite.setOnClickListener(v -> {
            int index = ContainerAndGlobal.searchInFavorites(clickedMarker.getPosition());
            if(index == -1) // -1 means that charging station is not in favorite list
            {
                int indexCs = ContainerAndGlobal.indexSearchInList(clickedMarker.getPosition());
                Favorite tmp = new Favorite(ContainerAndGlobal.getChargingStationList().get(indexCs), indexCs);
                ContainerAndGlobal.getChargingStationList().remove(indexCs);
                ContainerAndGlobal.getFavoriteList().add(tmp);
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                        .position(tmp.getFavoriteCs().getLocation())
                        .title(tmp.getFavoriteCs().getStrasse()));
            }
            else
            {
                int filtered = ContainerAndGlobal.addChargingStation(ContainerAndGlobal.getFavoriteList().get(index).getIndexInArray(), ContainerAndGlobal.getFavoriteList().get(index).getFavoriteCs());
                if(filtered == 2)
                    googleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .position(ContainerAndGlobal.getFavoriteList().get(index).getFavoriteCs().getLocation())
                            .title(ContainerAndGlobal.getFavoriteList().get(index).getFavoriteCs().getStrasse()));
                else if(filtered == 1)
                    googleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                            .position(ContainerAndGlobal.getFavoriteList().get(index).getFavoriteCs().getLocation())
                            .title(ContainerAndGlobal.getFavoriteList().get(index).getFavoriteCs().getStrasse()));
                ContainerAndGlobal.getFavoriteList().remove(index);
            }
            clickedMarker.remove();
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
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
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
     * Clear all markers on map and then loop for adding all charging stations as marker in map.
     */
    private void addCSToMaps() {
        googleMap.clear();
        for(int i = 0 ; i < ContainerAndGlobal.getChargingStationList().size(); i++)
        {
            if(ContainerAndGlobal.calculateLength(ContainerAndGlobal.getChargingStationList().get(i).getLocation(), ContainerAndGlobal.getCurrentLocation()) > ContainerAndGlobal.getMaxViewRange())
                break;
            if(ContainerAndGlobal.getChargingStationList().get(i).isFiltered())
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .position(ContainerAndGlobal.getChargingStationList().get(i).getLocation())
                        .title(ContainerAndGlobal.getChargingStationList().get(i).getStrasse()));
            else
                googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                        .position(ContainerAndGlobal.getChargingStationList().get(i).getLocation())
                        .title(ContainerAndGlobal.getChargingStationList().get(i).getStrasse()));
        }
        for(int i = 0 ; i < ContainerAndGlobal.getFavoriteList().size(); i++)
        {
            googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .position(ContainerAndGlobal.getFavoriteList().get(i).getFavoriteCs().getLocation())
                    .title(ContainerAndGlobal.getFavoriteList().get(i).getFavoriteCs().getStrasse()));
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
        {
            ContainerAndGlobal.setReportedMaker(marker);
        }
        return ContainerAndGlobal.getReportedChargingStation() != null;
    }
}