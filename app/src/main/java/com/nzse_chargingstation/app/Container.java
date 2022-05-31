package com.nzse_chargingstation.app;

import java.util.ArrayList;

public class Container {
    private static ArrayList<ChargingStation> unfiltered_list = new ArrayList<>();
    private static ArrayList<ChargingStation> filtered_list = new ArrayList<>();

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
}