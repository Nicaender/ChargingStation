package com.nzse_chargingstation.app.classes;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ContainerAndGlobal {
    @SuppressWarnings("FieldMayBeFinal")
    private static ArrayList<ChargingStation> charging_station_list = new ArrayList<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static ArrayList<ChargingStation> charging_station_list_filtered = new ArrayList<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static ArrayList<ChargingStation> charging_station_favorites = new ArrayList<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static ArrayList<Defective> defective_list = new ArrayList<>();
    private static double filter_range = 0;
    private static Location current_location = null;
    private static boolean first_time = true;
    private static ChargingStation reported_charging_station = null;
    private static boolean changedSetting = false;
    public static final DecimalFormat df = new DecimalFormat("#.##");

    public static ArrayList<ChargingStation> getCharging_station_list() {
        return charging_station_list;
    }

    public static ArrayList<ChargingStation> getCharging_station_list_filtered() {
        return charging_station_list_filtered;
    }

    public static ArrayList<Defective> getDefective_list() {
        return defective_list;
    }

    public static ArrayList<ChargingStation> getCharging_station_favorites() {
        return charging_station_favorites;
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

    /**
     * Enabling filter whether a charging station is within range or not
     */
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

    public static boolean is_already_favorite(ChargingStation input)
    {
        if(input == null)
            return true;

        for(int i = 0; i < charging_station_favorites.size(); i++)
        {
            if(charging_station_favorites.get(i).equals(input))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Add a charging station to favorites and remove it from normal list or remove it from
     * favorites list and add it back to normal list
     * @param input is the charging station that wants to be added
     * @param add is a boolean, whether to add or to remove the charging station from favorites
     * @return true if it is possible to add or remove
     */
    public static boolean add_or_remove_favorite(ChargingStation input, boolean add)
    {
        {
            if(input == null)
                return false;

            setChangedSetting(true);
            if(add)
            {
                add_or_remove_charging_station(input, false);
                charging_station_favorites.add(input);
                return true;
            }
            else
            {
                for(int i = 0; i < charging_station_favorites.size(); i++)
                {
                    if(charging_station_favorites.get(i).equals(input))
                    {
                        add_or_remove_charging_station(input, true);
                        charging_station_favorites.remove(i);
                        return true;
                    }
                }
            }

            setChangedSetting(false);
            return false;
        }
    }

    /**
     * Add a defective charging station to the defective list and remove it from normal list.
     * Then set reported_charging_station to null. It can also remove a defective class from the
     * defective array and add that charging station back to normal list
     * @param input is the defective class that contains the charging station and the reason
     * @param add is a boolean, whether to add or to remove the defective class
     * @return true if it is possible to add or remove
     */
    public static boolean add_or_remove_defective(Defective input, boolean add)
    {
        if(input == null)
            return false;

        setChangedSetting(true);
        if(add)
        {
            defective_list.add(input);
            add_or_remove_charging_station(input.getDefective_cs(), false);
            reported_charging_station = null;
            return true;
        }
        else
        {
            for(int i = 0; i < defective_list.size(); i++)
            {
                if(defective_list.get(i).equals(input))
                {
                    add_or_remove_charging_station(input.getDefective_cs(), true);
                    defective_list.remove(i);
                    return true;
                }
            }
        }

        setChangedSetting(false);
        return false;
    }

    /**
     * Add a charging station to the normal or filtered list or remove it from either of list
     * @param input is the charging station that wants to be removed
     * @param add is the boolean, whether to add it or remove it
     * @return true if it is possible to add or remove
     */
    public static boolean add_or_remove_charging_station(ChargingStation input, boolean add)
    {
        if(input == null)
            return false;

        setChangedSetting(true);
        if(add)
        {
            if(current_location == null)
            {
                charging_station_list.add(input);
            }
            else
            {
                if(calculateLength(input.getLocation(), current_location) < filter_range)
                {
                    charging_station_list_filtered.add(input);
                }
                else
                {
                    charging_station_list.add(input);
                }
            }
            return true;
        }
        else
        {
            for(int i = 0; i < getCharging_station_list().size(); i++)
            {
                if(getCharging_station_list().get(i).equals(input))
                {
                    getCharging_station_list().remove(i);
                    return true;
                }
            }
            for(int i = 0; i < getCharging_station_list_filtered().size(); i++)
            {
                if(getCharging_station_list_filtered().get(i).equals(input))
                {
                    getCharging_station_list_filtered().remove(i);
                    return true;
                }
            }
            for(int i = 0; i < getCharging_station_favorites().size(); i++)
            {
                if(getCharging_station_favorites().get(i).equals(input))
                {
                    getCharging_station_favorites().remove(i);
                    return true;
                }
            }
        }

        setChangedSetting(false);
        return false;
    }

    /**
     * Search for a charging station using its location
     * @param marker the location of searched charging station
     * @return charging station with the same location as the marker
     */
    public static ChargingStation search_charging_station(Marker marker)
    {
        for(int i = 0; i < getCharging_station_list().size(); i++)
        {
            if(Objects.requireNonNull(marker.getPosition()).equals(getCharging_station_list().get(i).getLocation()))
            {
                return getCharging_station_list().get(i);
            }
        }
        for(int i = 0; i < getCharging_station_list_filtered().size(); i++)
        {
            if(Objects.requireNonNull(marker.getPosition()).equals(getCharging_station_list_filtered().get(i).getLocation()))
            {
                return getCharging_station_list_filtered().get(i);
            }
        }
        for(int i = 0; i < getCharging_station_favorites().size(); i++)
        {
            if(Objects.requireNonNull(marker.getPosition()).equals(getCharging_station_favorites().get(i).getLocation()))
            {
                return getCharging_station_favorites().get(i);
            }
        }

        return null;
    }

    /**
     * Calculate distance between marker and user
     * @param marker is a location from where it needs to be calculated
     * @param user is the location from the user
     * @return a calculated distance between the user and the marker
     */
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

    /**
     * Convert from degree to grad
     * @param degree is the latitude or longitude
     * @return converted value from latitude or longitude
     */
    private static double deg2grad(double degree)
    {
        double pi = 3.14;
        return (degree * (pi/180));
    }
}