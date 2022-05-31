package com.nzse_chargingstation.app;

import android.location.Location;

import java.util.ArrayList;

public class Container {
    private static ArrayList<ChargingStation> unfiltered_list = new ArrayList<>();
    private static ArrayList<ChargingStation> filtered_list = new ArrayList<>();
    private static double range_filter = 0;
    private static Location last_location;

    /**
     *
     * @return list of unfiltered charging stations
     */
    public static ArrayList<ChargingStation> getUnfiltered_list() {
        return unfiltered_list;
    }

    /**
     *
     * @return list of filtered charging stations
     */
    public static ArrayList<ChargingStation> getFiltered_list() {
        return filtered_list;
    }

    public static double getRange_filter() {
        return range_filter;
    }

    public static void setRange_filter(double range_filter) {
        Container.range_filter = range_filter;
    }

    public static Location getLast_location() {
        return last_location;
    }

    public static void setLast_location(Location last_location) {
        Container.last_location = last_location;
    }
}