package com.nzse_chargingstation.app.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class MapsFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap googleMap;
    private TextView tv_radius_value;
    private static int radius_value = 0;

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
                if(ContainerAndGlobal.getCurrent_location() != null)
                    marker.setSnippet("Distance: " + ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(marker.getPosition(), ContainerAndGlobal.getCurrent_location())) + " KM, click for more info");
                else
                    marker.setSnippet("Distance: unknown");
                return false;
            });

            googleMap.setOnInfoWindowClickListener(marker -> {
                if(ContainerAndGlobal.is_already_favorite(ContainerAndGlobal.search_charging_station(marker)))
                    return;
                ContainerAndGlobal.add_or_remove_favorite(ContainerAndGlobal.search_charging_station(marker), true);
                addCSToMaps();
            });

            googleMap.setOnInfoWindowLongClickListener(marker -> {
                if(!report_charging_station(marker))
                    return;

                startActivity(new Intent(getActivity(), ReportActivity.class));
            });

            enableMyLocation();
            addCSToMaps();
            if(ContainerAndGlobal.getCurrent_location() != null)
            {
                LatLng start = new LatLng(ContainerAndGlobal.getCurrent_location().getLatitude(), ContainerAndGlobal.getCurrent_location().getLongitude());
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
        tv_radius_value = view.findViewById(R.id.textview_radius_value);
        Button btn_radius_confirm = view.findViewById(R.id.button_radius_confirm);
        ImageButton imgbtn_arrow_up = view.findViewById(R.id.imagebutton_arrow_up);
        ImageButton imgbtn_arrow_down = view.findViewById(R.id.imagebutton_arrow_down);

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
            if(googleMap.isMyLocationEnabled())
            {
                ContainerAndGlobal.setFilter_range(radius_value);
                ContainerAndGlobal.enable_filter();
                addCSToMaps();
            }
            else
                Toast.makeText(getContext(), "Location is unknown", Toast.LENGTH_LONG).show();
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
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        if(ContainerAndGlobal.isChangedSetting())
        {
            addCSToMaps();
            ContainerAndGlobal.setChangedSetting(false);
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
        for(int i = 0; i < ContainerAndGlobal.getCharging_station_list().size(); i++)
        {
            googleMap.addMarker(new MarkerOptions()
                    .position(ContainerAndGlobal.getCharging_station_list().get(i).getLocation())
                    .title(ContainerAndGlobal.getCharging_station_list().get(i).getStrasse()));
        }
        for(int i = 0; i < ContainerAndGlobal.getCharging_station_list_filtered().size(); i++)
        {
            googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .position(ContainerAndGlobal.getCharging_station_list_filtered().get(i).getLocation())
                    .title(ContainerAndGlobal.getCharging_station_list_filtered().get(i).getStrasse()));
        }
        for(int i = 0; i < ContainerAndGlobal.getCharging_station_favorites().size(); i++)
        {
            googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .position(ContainerAndGlobal.getCharging_station_favorites().get(i).getLocation())
                    .title(ContainerAndGlobal.getCharging_station_favorites().get(i).getStrasse()));
        }
    }

    /**
     * Mark a charging station as defective
     * @param marker from clicked location in google map
     * @return true if marker exists & the same location of charging station exists
     */
    private boolean report_charging_station(Marker marker)
    {
        ContainerAndGlobal.setReported_charging_station(ContainerAndGlobal.search_charging_station(marker));
        return ContainerAndGlobal.getReported_charging_station() != null;
    }
}