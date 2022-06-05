package com.nzse_chargingstation.app.classes;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ContainerAndGlobal {

    @SuppressWarnings("FieldMayBeFinal")
    private static ArrayList<ChargingStation> chargingStationList = new ArrayList<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static ArrayList<Favorite> favoriteList = new ArrayList<>();
    @SuppressWarnings("FieldMayBeFinal")
    private static ArrayList<Defective> defectiveList = new ArrayList<>();
    private static double filterRange = 0;
    private static Location currentLocation = null;
    private static boolean firstTime = true;
    private static ChargingStation reportedChargingStation = null;
    private static Marker reportedMaker = null;
    private static boolean changedSetting = false;
    public static final DecimalFormat df = new DecimalFormat("#.##");

    public static ArrayList<ChargingStation> getChargingStationList() {
        return chargingStationList;
    }

    public static ArrayList<Favorite> getFavoriteList() {
        return favoriteList;
    }

    public static ArrayList<Defective> getDefectiveList() {
        return defectiveList;
    }

    public static void setFilterRange(double filterRange) {
        for(int i = 0; i < chargingStationList.size(); i++)
        {
            if(calculateLength(chargingStationList.get(i).getLocation(), currentLocation) < ContainerAndGlobal.filterRange)
            {
                chargingStationList.get(i).setFiltered(false);
            }
            else
                break;
        }
        ContainerAndGlobal.filterRange = filterRange;
    }

    public static Location getCurrentLocation() {
        return currentLocation;
    }

    public static void setCurrentLocation(Location currentLocation) {
        ContainerAndGlobal.currentLocation = currentLocation;
    }

    public static boolean isFirstTime() {
        return firstTime;
    }

    public static void setFirstTime(boolean firstTime) {
        ContainerAndGlobal.firstTime = firstTime;
    }

    public static ChargingStation getReportedChargingStation() {
        return reportedChargingStation;
    }

    public static void setReportedChargingStation(ChargingStation reportedChargingStation) {
        ContainerAndGlobal.reportedChargingStation = reportedChargingStation;
    }

    public static void setReportedMaker(Marker reportedMaker) {
        ContainerAndGlobal.reportedMaker = reportedMaker;
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
    public static void enableFilter()
    {
        for(int i = 0; i < chargingStationList.size(); i++)
        {
            if(calculateLength(chargingStationList.get(i).getLocation(), currentLocation) < filterRange)
            {
                chargingStationList.get(i).setFiltered(true);
            }
            else
                return;
        }
    }

    /**
     * Search charging station from all lists except defective list
     * @param latLng is the coordination from a charging station
     * @return the searched charging station
     */
    public static ChargingStation searchChargingStationEverywhere(LatLng latLng)
    {
        int indexNormal = indexSearchInList(latLng);
        if(indexNormal == -1)
        {
            return favoriteList.get(searchInFavorites(latLng)).getFavoriteCs();
        }
        return chargingStationList.get(indexNormal);
    }

    /**
     *
     * @param latLng is the coordination from a charging station
     * @return the index of that charging station from the list
     */
    public static int indexSearchInList(LatLng latLng)
    {
        //set first to first index
        int first = 0;
        //set last to last elements in array
        int last=ContainerAndGlobal.getChargingStationList().size()-1;
        //calculate mid of the array
        int mid = (first + last)/2;
        //while first and last do not overlap
        while( first <= last ){
            //if the mid < key, then key to be searched is in the first half of array
            if ( calculateLength(ContainerAndGlobal.getChargingStationList().get(mid).getLocation(), currentLocation) < calculateLength(latLng, currentLocation) ){
                first = mid + 1;
            }else if ( calculateLength(ContainerAndGlobal.getChargingStationList().get(mid).getLocation(), currentLocation) == calculateLength(latLng, currentLocation) ){
                return mid;
            }else{
                //the key is to be searched in the second half of the array
                last = mid - 1;
            }
            mid = (first + last)/2;
        }
        for(int i = 0; i < chargingStationList.size(); i++)
        {
            if(chargingStationList.get(i).getLocation().equals(latLng))
                return i;
        }

        return -1;
    }

    /**
     *
     * @param latLng is the coordinate from a charging station in google map
     * @return -1 if it is not in favorite or it's index in favorite list
     */
    public static int searchInFavorites(LatLng latLng)
    {
        for(int i = 0; i < favoriteList.size(); i++)
        {
            if(favoriteList.get(i).getFavoriteCs().getLocation().equals(latLng))
            {
                return i;
            }
        }

        return -1;
    }

    /**
     *
     * @param defective is a class consist of charging station, the reason, index, and whether
     *                  it is in favorite or not
     */
    public static void addDefective(Defective defective)
    {
        defectiveList.add(defective);
        reportedChargingStation = null;
        reportedMaker.remove();
        reportedMaker = null;
    }

    /**
     * remove the defective class from defective and automatically add to normal list or fav list
     * @param defective is the defective that will be removed
     */
    public static void removeDefective(Defective defective)
    {
        for(int i = 0; i < defectiveList.size(); i++)
        {
            if(defectiveList.get(i).equals(defective))
            {
                defectiveList.remove(i);
                if(defective.getDefectiveFavorite() != null)
                {
                    ContainerAndGlobal.getFavoriteList().add(defective.getDefectiveFavorite());
                }
                else
                {
                    addChargingStation(defective.getIndexInArray(), defective.getDefectiveCs());
                }
                return;
            }
        }
    }

    /**
     * add a charging station based on index and automatically assign if it is filtered
     * @param input is a charging station
     * @param index is the previous index in array (-1 if it is a new charging station)
     * @return 1 if it is not filtered, 2 if it is filtered
     */
    public static int addChargingStation(int index, ChargingStation input)
    {
        if(index == -1)
        {
            chargingStationList.add(input);
        }
        else
        {
            chargingStationList.add(index, input);
            if(currentLocation != null && calculateLength(input.getLocation(), currentLocation) < filterRange)
            {
                input.setFiltered(true);
                return 2;
            }
            else
                input.setFiltered(false);
        }
        return 1;
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

    /**
     * Get JSON data in string format
     * @param context is the activity that runs it
     * @param textFileName is the name of the JSON file
     * @return the JSON file as a string file
     */
    public static String getJSONData(Context context, String textFileName) {
        String strJSON;
        StringBuilder buf = new StringBuilder();
        InputStream json;
        try {
            json = context.getAssets().open(textFileName);

            BufferedReader in = new BufferedReader(new InputStreamReader(json, StandardCharsets.UTF_8));

            while ((strJSON = in.readLine()) != null) {
                buf.append(strJSON);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buf.toString();
    }

    /**
     * Convert from JSON file into a java class
     * @param chargingstation is the JSON object of the charging station
     * @throws JSONException if it is error
     */
    public static void parseLadesaeuleObject(JSONObject chargingstation) throws JSONException {
        ChargingStation tmpChargingStation = new ChargingStation(
                (String)chargingstation.get("Betreiber"),
                (String) chargingstation.get("Straße"),
                (String) chargingstation.get("Hausnummer"),
                (String) chargingstation.get("Adresszusatz"),
                Integer.parseInt(chargingstation.get("Postleitzahl").toString()),
                (String) chargingstation.get("Ort"),
                (String) chargingstation.get("Bundesland"),
                (String) chargingstation.get("Kreis/kreisfreie Stadt"),
                Double.parseDouble(chargingstation.get("Breitengrad").toString().replace(",",".")),
                Double.parseDouble(chargingstation.get("Längengrad").toString().replace(",",".")),
                (String) chargingstation.get("Inbetriebnahmedatum"),
                Double.parseDouble(chargingstation.get("Anschlussleistung").toString().replace(",",".")),
                (String) chargingstation.get("Art der Ladeeinrichung"),
                Integer.parseInt(chargingstation.get("Anzahl Ladepunkte").toString()),
                (String) chargingstation.get("Steckertypen1"),
                Double.parseDouble(chargingstation.get("P1 [kW]").toString().replace(",",".")),
                (String) chargingstation.get("Public Key1"),
                (String) chargingstation.get("Steckertypen2"),
                (chargingstation.get("P2 [kW]").toString().isEmpty() ? 0.0  : Double.parseDouble(chargingstation.get("P2 [kW]").toString().replace(",","."))),
                (String) chargingstation.get("Public Key2"),
                (String) chargingstation.get("Steckertypen3"),
                (chargingstation.get("P3 [kW]").toString().isEmpty() ? 0.0  : Double.parseDouble(chargingstation.get("P3 [kW]").toString().replace(",","."))) ,
                (String) chargingstation.get("Public Key3"),
                (String) chargingstation.get("Steckertypen4"),
                (chargingstation.get("P4 [kW]").toString().isEmpty() ? 0.0  : Double.parseDouble(chargingstation.get("P4 [kW]").toString().replace(",","."))),
                (String) chargingstation.get("Public Key4")
        );

        chargingStationList.add(tmpChargingStation);
    }
}