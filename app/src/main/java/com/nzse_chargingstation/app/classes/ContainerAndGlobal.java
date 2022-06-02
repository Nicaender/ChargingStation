package com.nzse_chargingstation.app.classes;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ContainerAndGlobal {
    @SuppressWarnings("FieldMayBeFinal")
    private static ArrayList<ChargingStation> charging_station_list = new ArrayList<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static ArrayList<ChargingStation> charging_station_list_filtered = new ArrayList<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static ArrayList<Defective> defective_list = new ArrayList<>();
    private static double filter_range = 0;
    private static Location current_location = null;
    private static boolean first_time = true;
    private static ChargingStation reported_charging_station;
    private static boolean changedSetting = false;

    public static ArrayList<ChargingStation> getCharging_station_list() {
        return charging_station_list;
    }

    public static ArrayList<ChargingStation> getCharging_station_list_filtered() {
        return charging_station_list_filtered;
    }

    public static ArrayList<Defective> getDefective_list() {
        return defective_list;
    }

    public static void setFilter_range(double filter_range) {
        ContainerAndGlobal.filter_range = filter_range;
    }

    public static Location getCurrent_location() {
        return current_location;
    }

    public static void setCurrent_location(Location current_location) {
        ContainerAndGlobal.current_location = current_location;
    }

    public static boolean isFirst_time() {
        return first_time;
    }

    public static void setFirst_time(boolean first_time) {
        ContainerAndGlobal.first_time = first_time;
    }

    public static ChargingStation getReported_charging_station() {
        return reported_charging_station;
    }

    public static void setReported_charging_station(ChargingStation reported_charging_station) {
        ContainerAndGlobal.reported_charging_station = reported_charging_station;
    }

    public static boolean isChangedSetting() {
        return changedSetting;
    }

    public static void setChangedSetting(boolean changedSetting) {
        ContainerAndGlobal.changedSetting = changedSetting;
    }

    public static void enable_filter()
    {
        for(int i = 0; i < charging_station_list_filtered.size(); i++)
        {
            if(calculateLength(charging_station_list_filtered.get(i).getLocation(), current_location) > filter_range)
            {
                charging_station_list.add(charging_station_list_filtered.get(i));
                charging_station_list_filtered.remove(i);
                i--;
            }
        }
        for(int i = 0; i < charging_station_list.size(); i++)
        {
            if(calculateLength(charging_station_list.get(i).getLocation(), current_location) < filter_range)
            {
                charging_station_list_filtered.add(charging_station_list.get(i));
                charging_station_list.remove(i);
                i--;
            }
        }
    }

    // Calculate distance between marker and user
    public static double calculateLength(LatLng marker, Location user)
    {
        double lat1 = deg2grad(marker.latitude);
        double lat2 = deg2grad(user.getLatitude());
        double long1 = deg2grad(marker.longitude);
        double long2 = deg2grad(user.getLongitude());

        double deltalat = (lat2-lat1)/2;
        double deltalong = (long2-long1)/2;

        return (2 * 6371 * Math.asin(Math.sqrt(Math.sin(deltalat)*Math.sin(deltalat)+Math.cos(lat1)*Math.cos(lat2)*(Math.sin(deltalong)*Math.sin(deltalong)))));
    }

    private static double deg2grad(double degree)
    {
        double pi = 3.14;
        return (degree * (pi/180));
    }
}