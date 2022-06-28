package com.nzse_chargingstation.app.fragments;

import static android.view.View.GONE;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.nzse_chargingstation.app.R;
import com.nzse_chargingstation.app.activities.InfoActivity;
import com.nzse_chargingstation.app.activities.ReportActivity;
import com.nzse_chargingstation.app.activities.SearchActivity;
import com.nzse_chargingstation.app.classes.ChargingStation;
import com.nzse_chargingstation.app.classes.ContainerAndGlobal;
import com.nzse_chargingstation.app.classes.InfoWindowAdapter;
import com.nzse_chargingstation.app.classes.PolylineCreator;
import com.nzse_chargingstation.app.classes.RoutePlan;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class MapsFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap googleMap;
    private ImageButton imgBtnFavorite, imgBtnReport, imgBtnMyLocation, imgBtnAddToRoute;
    private ImageView imgViewMenuBackground;
    private MaterialSpinner spRadiusValue;
    private Marker clickedMarker;
    private Polyline currentPolyline;
    private Thread markerThread, polylineThread, navigatedPolylineThread;
    private final Semaphore markerSignal = new Semaphore(0), polylineSignal = new Semaphore(0), navigatedPolylineSignal = new Semaphore(0);
    private String url;
    private boolean stopThread = false, updateLocationUI = true;
    private int lastShownIndex, favoriteY, reportY, routeY, spinnerX, locationX, backgroundY;
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

        try {
            // Initialize threads, then runs the threads
            markerThreadInitialize();
            polylineThreadInitialize();
            navigatedPolylineThreadInitialize();
            markerThread.start();
            polylineThread.start();
            navigatedPolylineThread.start();
            stopThread = false;

            // Create the google map
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

                // Enable dark mode on google map
                if(ContainerAndGlobal.isDarkmode())
                    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_in_night));
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                // Enable custom window adapter
                InfoWindowAdapter markerInfoWindowAdapter = new InfoWindowAdapter(requireContext());
                googleMap.setInfoWindowAdapter(markerInfoWindowAdapter);

                // Triggered when user click any marker on the map
                googleMap.setOnMarkerClickListener(marker -> {
                    clickedMarker = marker;
                    if(ContainerAndGlobal.getCurrentLocation() != null)
                        marker.setSnippet("Distance: " + ContainerAndGlobal.df.format(ContainerAndGlobal.calculateLength(marker.getPosition(), ContainerAndGlobal.getCurrentLocation())) + " KM, click for more info");
                    else
                        marker.setSnippet("Distance: unknown");

                    ContainerAndGlobal.setClickedChargingStation(ContainerAndGlobal.searchChargingStation(marker.getPosition()));

                    // Toggle the favorite icon whether the charging station is in favorite
                    if(ContainerAndGlobal.isInFavorite(ContainerAndGlobal.getClickedChargingStation()))
                        imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_24", "drawable", requireContext().getPackageName()));
                    else
                        imgBtnFavorite.setImageResource(getResources().getIdentifier("ic_baseline_favorite_border_24", "drawable", requireContext().getPackageName()));

                    // Show the icons if it is invisible
                    if(imgViewMenuBackground.getVisibility() == View.GONE)
                    {
                        imgViewMenuBackground.setVisibility(View.VISIBLE);
                        imgBtnFavorite.setVisibility(View.VISIBLE);
                        imgBtnReport.setVisibility(View.VISIBLE);
                    }

                    // Animate the icons entry
                    animate(true);

                    return false;
                });

                // Triggered when user click the info window
                googleMap.setOnInfoWindowClickListener(marker -> {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), zoomLevel));
                    // Show direction from current location to the selected charging station
                    LatLng start = new LatLng(ContainerAndGlobal.getCurrentLocation().getLatitude(), ContainerAndGlobal.getCurrentLocation().getLongitude());
                    url = getUrl(start, marker.getPosition());
                    polylineSignal.release();
                });

                // Triggered when user long click the info window
                googleMap.setOnInfoWindowLongClickListener(marker -> startActivity(new Intent(getActivity(), InfoActivity.class)));

                // Triggered when the info window is closed
                googleMap.setOnInfoWindowCloseListener(marker -> animate(false));

                // Triggered when the camera is idle, allowing the location ui to be updated
                googleMap.setOnCameraIdleListener(() -> updateLocationUI = true);

                // Triggered when the camera is moving, removing the dot on location ui
                googleMap.setOnCameraMoveListener(() -> {
                    if(updateLocationUI && ContainerAndGlobal.getCurrentLocation() != null)
                        imgBtnMyLocation.setImageResource(getResources().getIdentifier("ic_baseline_location_searching_24", "drawable", requireContext().getPackageName()));
                });

                // Get user location, starting the threads, after that put markers on the map
                enableMyLocation();
                markerSignal.release();

                // Moves camera to user location
                LatLng start;
                if(ContainerAndGlobal.getCurrentLocation() != null)
                    start = new LatLng(ContainerAndGlobal.getCurrentLocation().getLatitude(), ContainerAndGlobal.getCurrentLocation().getLongitude());
                else
                    start = new LatLng(49.8728, 8.6512);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, zoomLevel));

                // When user changes menu, it will regain the last camera position
                if(ContainerAndGlobal.getLastCameraPosition() != null)
                {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ContainerAndGlobal.getLastCameraPosition().target, ContainerAndGlobal.getLastCameraPosition().zoom));
                    if(ContainerAndGlobal.calculateLength(ContainerAndGlobal.getLastCameraPosition().target, ContainerAndGlobal.getCurrentLocation()) < 0.1)
                        imgBtnMyLocation.setImageResource(getResources().getIdentifier("ic_baseline_my_location_24", "drawable", requireContext().getPackageName()));
                    ContainerAndGlobal.setLastCameraPosition(null);
                }

                // When user clicked charging station on favorite, it will moves the camera to that charging station
                if(ContainerAndGlobal.getZoomToThisChargingStation() != null)
                {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ContainerAndGlobal.getZoomToThisChargingStation().getLocation(), zoomLevel));
                    ContainerAndGlobal.setZoomToThisChargingStation(null);
                }

                // When user clicked navigate on route plan, it will automatically created a polyline from the beginning to the end
                if(ContainerAndGlobal.getNavigateRoutePlan() != null)
                {
                    navigatedPolylineSignal.release();
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Initialization
            ImageView imgViewRadius = view.findViewById(R.id.imageViewRadius);
            imgBtnFavorite = view.findViewById(R.id.imageButtonFavorite);
            imgBtnReport = view.findViewById(R.id.imageButtonReport);
            imgBtnAddToRoute = view.findViewById(R.id.imageButtonAddToRoute);
            imgBtnMyLocation = view.findViewById(R.id.imageButtonMyLocation);
            SearchView svSearchBar = view.findViewById(R.id.searchViewSearchBar);
            imgViewMenuBackground = view.findViewById(R.id.imageViewMenuBackground);
            spRadiusValue = view.findViewById(R.id.spinnerRadiusValue);
            backgroundY = (int) imgViewMenuBackground.getTranslationY();
            favoriteY = (int) imgBtnFavorite.getTranslationY();
            reportY = (int) imgBtnReport.getTranslationY();
            routeY = (int) imgBtnAddToRoute.getTranslationY();
            spinnerX = (int) spRadiusValue.getTranslationX();
            locationX = (int) imgBtnMyLocation.getTranslationX();

            // Hides the favorite and report ui when the map is ready for the first time
            imgViewMenuBackground.setVisibility(GONE);
            imgBtnFavorite.setVisibility(GONE);
            imgBtnReport.setVisibility(GONE);

            // Animate the item secretly to hide it
            ObjectAnimator animation = ObjectAnimator.ofFloat(imgViewMenuBackground, "translationY", 1000f);
            animation.setDuration(250);
            animation.start();
            animation = ObjectAnimator.ofFloat(imgBtnFavorite, "translationY", 1000f);
            animation.setDuration(250);
            animation.start();
            animation = ObjectAnimator.ofFloat(imgBtnReport, "translationY", 1000f);
            animation.setDuration(250);
            animation.start();
            animation = ObjectAnimator.ofFloat(imgBtnAddToRoute, "translationY", 1000f);
            animation.setDuration(250);
            animation.start();

            // Implementation of radius filter spinner
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
                    markerSignal.release();
                }
                else
                    Toast.makeText(getContext(), requireContext().getResources().getString(R.string.location_is_unknown), Toast.LENGTH_SHORT).show();
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
                ContainerAndGlobal.saveData(1, requireContext());
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
            svSearchBar.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), SearchActivity.class));
                requireActivity().overridePendingTransition(0, 0);
                svSearchBar.clearFocus();
            });
            svSearchBar.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                if(hasFocus)
                    startActivity(new Intent(getActivity(), SearchActivity.class));
                requireActivity().overridePendingTransition(0, 0);
                svSearchBar.clearFocus();
            });

            // Implementation to limit total charging stations on the map
            final int size = 12;
            String[] maxView = new String[size];
            for(int i = 0; i < size; i++)
            {
                maxView[i] = String.valueOf((i+1) * 25);
            }
            AlertDialog.Builder builderLimit = new AlertDialog.Builder(requireActivity());
            builderLimit.setTitle(R.string.how_many_charging_station_to_show_on_the_map)
                    .setItems(maxView, (dialog, which) -> limitMaxChargingStation(Integer.parseInt(maxView[which])));
            AlertDialog dialogLimit = builderLimit.create();
            dialogLimit.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.item_curved));
            imgViewRadius.setOnClickListener(v -> dialogLimit.show());

            // Implementation of add to route image button
            AlertDialog.Builder builderRoute = new AlertDialog.Builder(requireActivity());
            AlertDialog.Builder builderAddRoute = new AlertDialog.Builder(requireActivity());
            imgBtnAddToRoute.setOnClickListener(v -> {
                String[] routeList = new String[ContainerAndGlobal.getRoutePlanList().size()];
                for(int i = 0; i < ContainerAndGlobal.getRoutePlanList().size(); i++)
                {
                    routeList[i] = ContainerAndGlobal.getRoutePlanList().get(i).getName();
                }
                builderRoute.setTitle(R.string.route_plan_list)
                        .setItems(routeList, (dialog1, which) -> {
                            ContainerAndGlobal.getRoutePlanList().get(which).getChargingStationRoutes().add(ContainerAndGlobal.searchChargingStation(clickedMarker.getPosition()));
                            ContainerAndGlobal.saveData(3, requireContext());
                            Toast.makeText(requireContext(), getString(R.string.successfully_added), Toast.LENGTH_SHORT).show();
                        })
                        .setNeutralButton(getString(R.string.add_new_route_plan), (dialog, which) -> {
                            builderAddRoute.setTitle(R.string.route_plan_name_question);

                            // Set up the input
                            final EditText input = new EditText(requireContext());
                            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            builderAddRoute.setView(input);
                            // Set up the buttons
                            builderAddRoute.setPositiveButton(getString(R.string.builder_positive_button), (dialog2, which2) -> {
                                if(!input.getText().toString().isEmpty())
                                {
                                    ContainerAndGlobal.getRoutePlanList().add(new RoutePlan(input.getText().toString()));
                                    ContainerAndGlobal.getRoutePlanList().get(ContainerAndGlobal.getRoutePlanList().size()-1).getChargingStationRoutes().add(ContainerAndGlobal.searchChargingStation(clickedMarker.getPosition()));
                                    ContainerAndGlobal.saveData(3, requireContext());
                                    Toast.makeText(requireContext(), getString(R.string.successfully_added), Toast.LENGTH_SHORT).show();
                                }
                            });
                            builderAddRoute.setNegativeButton(getString(R.string.builder_negative_button), (dialog2, which2) -> dialog2.cancel());
                            AlertDialog dialogAddRoute = builderAddRoute.create();
                            dialogAddRoute.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.item_curved));
                            dialogAddRoute.show();
                        });
                AlertDialog dialogRoute = builderRoute.create();
                dialogRoute.getWindow().setBackgroundDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.item_curved));
                dialogRoute.show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        try {
            // Moves camera to this charging station when user searched this charging station
            if(ContainerAndGlobal.getZoomToThisChargingStationOnPause() != null)
            {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ContainerAndGlobal.getZoomToThisChargingStationOnPause().getLocation(), zoomLevel));

                if(showOnMap(ContainerAndGlobal.getZoomToThisChargingStationOnPause())) {
                    assignMarker(ContainerAndGlobal.getZoomToThisChargingStationOnPause());
                    ContainerAndGlobal.getMarkedList().add(ContainerAndGlobal.getZoomToThisChargingStationOnPause());
                }
                ContainerAndGlobal.setZoomToThisChargingStationOnPause(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        try {
            // Save last camera position before destroying the fragment, when map is ready
            if(googleMap != null)
                ContainerAndGlobal.setLastCameraPosition(googleMap.getCameraPosition());
            mMapView.onDestroy();

            // Initialize thread stop protocol
            stopThread = true;
            markerSignal.release();
            polylineSignal.release();
            navigatedPolylineSignal.release();
            try {
                markerThread.join();
                polylineThread.join();
                navigatedPolylineThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
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
     * Initialize a marker thread that manages all the marker creation and deletion
     */
    private void markerThreadInitialize()
    {
        try {
            markerThread = new Thread(() -> {
                while(true)
                {
                    // Wait for the signal from map
                    try {
                        markerSignal.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Stop thread if needed to stop
                    if(stopThread)
                        return;

                    // Clear all markers beforehand
                    requireActivity().runOnUiThread(() -> googleMap.clear());

                    // Put marked list markers
                    for(int i = 0; i < ContainerAndGlobal.getMarkedList().size(); i++)
                    {
                        if(stopThread)
                            return;
                        if(markerSignal.availablePermits() > 0)
                            break;
                        ChargingStation tmp = ContainerAndGlobal.getMarkedList().get(i);
                        assignMarker(tmp);
                        try {
                            //noinspection BusyWait
                            Thread.sleep(0, 100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // Put favorite list markers
                    for(int i = 0 ; i < ContainerAndGlobal.getFavoriteList().size(); i++)
                    {
                        if(stopThread)
                            return;
                        if(markerSignal.availablePermits() > 0)
                            break;
                        ChargingStation tmp = ContainerAndGlobal.getFavoriteList().get(i);
                        assignMarker(tmp);
                        try {
                            //noinspection BusyWait
                            Thread.sleep(0, 100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // Put normal list markers
                    int counter = 0;
                    for(int i = 0 ; i < ContainerAndGlobal.getChargingStationList().size(); i++)
                    {
                        if(stopThread)
                            return;
                        if(markerSignal.availablePermits() > 0)
                            break;
                        ChargingStation tmp = ContainerAndGlobal.getChargingStationList().get(i);
                        if(ContainerAndGlobal.getCurrentLocation() != null && counter >= ContainerAndGlobal.getMaxViewChargingStation()) {
                            lastShownIndex = i-1;
                            break;
                        }
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
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializing polyline thread that manages all the direction route creation and deletion
     */
    private void polylineThreadInitialize()
    {
        try {
            polylineThread = new Thread(() -> {
                PolylineCreator polylineCreator = new PolylineCreator();
                while(true)
                {
                    // Wait for the signal from map
                    try {
                        polylineSignal.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(stopThread)
                        return;
                    PolylineOptions polyline = polylineCreator.createPolyline(url, -1);
                    url = null;
                    // Put the route line on the map, delete the older one if it exists
                    if(currentPolyline != null)
                        requireActivity().runOnUiThread(() -> currentPolyline.remove());
                    requireActivity().runOnUiThread(() -> currentPolyline = googleMap.addPolyline(polyline));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigatedPolylineThreadInitialize()
    {
        try {
            navigatedPolylineThread = new Thread(() -> {
                while(true) {
                    // Wait for the signal from map
                    try {
                        navigatedPolylineSignal.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(stopThread)
                        return;

                    RoutePlan tmp = ContainerAndGlobal.getNavigateRoutePlan();
                    ContainerAndGlobal.setNavigateRoutePlan(null);
                    PolylineCreator polylineCreator = new PolylineCreator();
                    if(ContainerAndGlobal.getCurrentLocation() != null && tmp.getChargingStationRoutes().size() > 0 && !ContainerAndGlobal.isInDefective(tmp.getChargingStationRoutes().get(0)))
                    {
                        LatLng startLocation = new LatLng(ContainerAndGlobal.getCurrentLocation().getLatitude(), ContainerAndGlobal.getCurrentLocation().getLongitude());
                        final String tmpUrl = getUrl(startLocation, tmp.getChargingStationRoutes().get(0).getLocation());
                        final PolylineOptions tmpPolyline = polylineCreator.createPolyline(tmpUrl, 1);
                        requireActivity().runOnUiThread(() -> {
                            googleMap.addPolyline(tmpPolyline);
                            imgBtnMyLocation.performClick();
                        });
                    }
                    for(int i = 0; i < tmp.getChargingStationRoutes().size()-1; i++)
                    {
                        if(stopThread)
                            return;
                        int next = i+1;
                        if(ContainerAndGlobal.isInDefective(tmp.getChargingStationRoutes().get(i)))
                            continue;
                        while(ContainerAndGlobal.isInDefective(tmp.getChargingStationRoutes().get(next))) {
                            next++;
                            if(next >= tmp.getChargingStationRoutes().size())
                                break;
                        }
                        if(next >= tmp.getChargingStationRoutes().size())
                            break;

                        if(showOnMap(tmp.getChargingStationRoutes().get(i))) {
                            assignMarker(tmp.getChargingStationRoutes().get(i));
                            ContainerAndGlobal.getMarkedList().add(tmp.getChargingStationRoutes().get(i));
                        }
                        if(next == tmp.getChargingStationRoutes().size()-1)
                        {
                            if(showOnMap(tmp.getChargingStationRoutes().get(next))) {
                                assignMarker(tmp.getChargingStationRoutes().get(next));
                                ContainerAndGlobal.getMarkedList().add(tmp.getChargingStationRoutes().get(next));
                            }
                        }
                        final String tmpUrl = getUrl(tmp.getChargingStationRoutes().get(i).getLocation(), tmp.getChargingStationRoutes().get(next).getLocation());
                        final PolylineOptions tmpPolyline = polylineCreator.createPolyline(tmpUrl, i%2);
                        requireActivity().runOnUiThread(() -> googleMap.addPolyline(tmpPolyline));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Limiting charging stations shown on the map excluding favorites and marked list
     * @param limit is the total charging station that should be shown
     */
    private void limitMaxChargingStation(int limit)
    {
        try {
            if(ContainerAndGlobal.getMaxViewChargingStation() == limit)
                return;
            ContainerAndGlobal.setMaxViewChargingStation(limit);
            for(int i = 0; i < ContainerAndGlobal.getMarkedList().size(); i++)
            {
                if(ContainerAndGlobal.indexOfChargingStation(ContainerAndGlobal.getMarkedList().get(i)) < lastShownIndex)
                {
                    ContainerAndGlobal.getMarkedList().remove(i);
                    i--;
                }
            }
            markerSignal.release();
            SharedPreferences sharedPreferences = this.requireActivity().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("maxChargingStations", ContainerAndGlobal.getMaxViewChargingStation());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Assigning a marker for a charging station and automatically filters its color
     * @param chargingStation is the charging station that wants to be added
     */
    private void assignMarker(ChargingStation chargingStation)
    {
        try {
            if(ContainerAndGlobal.isInFavorite(chargingStation))
                requireActivity().runOnUiThread(() -> googleMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                        .position(chargingStation.getLocation())
                        .title(chargingStation.getStrasse() + ' ' + chargingStation.getHausnummer())));
            else {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Assign a marker color automatically (Not for favorite)
     * @param marker is the marker that wants to be changed
     * @param chargingStation is the parameter for its filter
     */
    private void assignColor(Marker marker, ChargingStation chargingStation)
    {
        try {
            if(chargingStation.isFiltered())
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            else
            {
                if(chargingStation.getArtDerLadeeinrichtung().equals("Normalladeeinrichtung"))
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                else
                    marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get an url of direction from one location to another location
     * @param origin is the beginning location
     * @param dest is the target location
     * @return an url in the form of a string
     */
    private String getUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + "driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.maps_api_key);
    }

    /**
     * Check whether location is on, and charging station is not on the map
     * @param chargingStation is the input class
     * @return true if it is not shown yet, else false
     */
    private boolean showOnMap(ChargingStation chargingStation)
    {
        return ContainerAndGlobal.getCurrentLocation() != null
                && ContainerAndGlobal.indexOfChargingStation(chargingStation) > lastShownIndex
                && !ContainerAndGlobal.isInMarkedList(chargingStation)
                && !ContainerAndGlobal.isInFavorite(chargingStation)
                && !ContainerAndGlobal.isInDefective(chargingStation);
    }

    /**
     * Help function to animate some ui when entering or exiting
     * @param enter is a boolean, whether it is for entry animation
     */
    private void animate(boolean enter)
    {
        try {
            ObjectAnimator animation;
            if(enter)
            {
                animation = ObjectAnimator.ofFloat(imgViewMenuBackground, "translationY", backgroundY);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnFavorite, "translationY", favoriteY);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnReport, "translationY", reportY);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnAddToRoute, "translationY", routeY);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(spRadiusValue, "translationX", -1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnMyLocation, "translationX", 1000f);
            }
            else
            {
                animation = ObjectAnimator.ofFloat(imgViewMenuBackground, "translationY", 1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnFavorite, "translationY", 1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnReport, "translationY", 1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnAddToRoute, "translationY", 1000f);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(spRadiusValue, "translationX", spinnerX);
                animation.setDuration(250);
                animation.start();
                animation = ObjectAnimator.ofFloat(imgBtnMyLocation, "translationX", locationX);
            }
            animation.setDuration(250);
            animation.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}