package com.nzse_chargingstation.app.classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

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

    private static final ArrayList<ChargingStation> chargingStationList = new ArrayList<>();
    private static final ArrayList<ChargingStation> favoriteList = new ArrayList<>();
    private static final ArrayList<ChargingStation> filteredList = new ArrayList<>();
    private static final ArrayList<Defective> defectiveList = new ArrayList<>();
    private static double filterRange = 0;
    private static Location currentLocation = null;
    private static ChargingStation reportedChargingStation = null;
    private static ChargingStation zoomToThisChargingStation = null;
    private static ChargingStation zoomToThisChargingStationOnPause = null;
    private static Marker reportedMarker = null;
    private static boolean changedSetting = false;
    private static boolean firstTime = true;
    private static boolean firstTimeGPSEnabled = true;
    private static int maxViewRange = 10;
    public static final DecimalFormat df = new DecimalFormat("#.##");

    public static ArrayList<ChargingStation> getChargingStationList() {
        return chargingStationList;
    }

    public static ArrayList<ChargingStation> getFavoriteList() {
        return favoriteList;
    }

    public static ArrayList<ChargingStation> getFilteredList() {
        return filteredList;
    }

    public static ArrayList<Defective> getDefectiveList() {
        return defectiveList;
    }

    public static void setFilterRange(double filterRange) {
        for(int i = 0; i < chargingStationList.size(); i++)
        {
            if(calculateLength(chargingStationList.get(i).getLocation(), currentLocation) < filterRange)
            {
                chargingStationList.get(i).setFiltered(true);
            }
            else if(calculateLength(chargingStationList.get(i).getLocation(), currentLocation) > Math.max(filterRange, ContainerAndGlobal.filterRange))
                break;
            else
                chargingStationList.get(i).setFiltered(false);
        }
        ContainerAndGlobal.filterRange = filterRange;
    }

    public static Location getCurrentLocation() {
        return currentLocation;
    }

    public static void setCurrentLocation(Location currentLocation) {
        ContainerAndGlobal.currentLocation = currentLocation;
    }

    public static ChargingStation getReportedChargingStation() {
        return reportedChargingStation;
    }

    public static void setReportedChargingStation(ChargingStation reportedChargingStation) {
        ContainerAndGlobal.reportedChargingStation = reportedChargingStation;
    }

    public static ChargingStation getZoomToThisChargingStation() {
        return zoomToThisChargingStation;
    }

    public static void setZoomToThisChargingStation(ChargingStation zoomToThisChargingStation) {
        ContainerAndGlobal.zoomToThisChargingStation = zoomToThisChargingStation;
    }

    public static ChargingStation getZoomToThisChargingStationOnPause() {
        return zoomToThisChargingStationOnPause;
    }

    public static void setZoomToThisChargingStationOnPause(ChargingStation zoomToThisChargingStationOnPause) {
        ContainerAndGlobal.zoomToThisChargingStationOnPause = zoomToThisChargingStationOnPause;
    }

    public static void setReportedMarker(Marker reportedMarker) {
        ContainerAndGlobal.reportedMarker = reportedMarker;
    }

    public static boolean isChangedSetting() {
        return changedSetting;
    }

    public static void setChangedSetting(boolean changedSetting) {
        ContainerAndGlobal.changedSetting = changedSetting;
    }

    public static boolean isFirstTime() {
        return firstTime;
    }

    public static void setFirstTime(boolean firstTime) {
        ContainerAndGlobal.firstTime = firstTime;
    }

    public static boolean isFirstTimeGPSEnabled() {
        return firstTimeGPSEnabled;
    }

    public static void setFirstTimeGPSEnabled(boolean firstTimeGPSEnabled) {
        ContainerAndGlobal.firstTimeGPSEnabled = firstTimeGPSEnabled;
    }

    public static int getMaxViewRange() {
        return maxViewRange;
    }

    public static void setMaxViewRange(int maxViewRange) {
        ContainerAndGlobal.maxViewRange = maxViewRange;
    }

    /**
     * Search a charging station in the charging station list
     * @param latLng is the coordination of the charging station
     * @return return a charging station, null if it fails
     */
    public static ChargingStation searchChargingStation(LatLng latLng)
    {
        for(int i = 0; i < chargingStationList.size(); i++)
        {
            if(chargingStationList.get(i).getLocation().equals(latLng))
                return chargingStationList.get(i);
        }

        return null;
    }

    /**
     * Search a charging station in the favorites
     * @param chargingStation is the class that wants to be checked
     * @return true if it is in favorite, else false
     */
    public  static boolean isInFavorite(ChargingStation chargingStation)
    {
        for(int i = 0; i < favoriteList.size(); i++)
        {
            if(favoriteList.get(i).equals(chargingStation))
                return true;
        }

        return false;
    }

    /**
     * Add defective charging station and remove it from the normal or favorite list
     * @param defective is the defective class
     */
    public static void addDefective(Defective defective)
    {
        defectiveList.add(defective);
        reportedChargingStation = null;
        if(defective.isFavorite())
            removeFavorite(defective.getDefectiveCs());
        defective.getDefectiveCs().setShowMarker(false);

        if(reportedMarker != null)
            reportedMarker.remove();
        reportedMarker = null;
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
                if(defective.isFavorite())
                    ContainerAndGlobal.addFavorite(defective.getDefectiveCs());
                else
                    defective.getDefectiveCs().setShowMarker(true);
                return;
            }
        }
    }

    /**
     * Add a charging station to favorite list and marked showMarker false
     * @param chargingStation is the class
     */
    public static void addFavorite(ChargingStation chargingStation)
    {
        favoriteList.add(chargingStation);
        chargingStation.setShowMarker(false);
    }

    /**
     * Add a charging station to favorite list and marked showMarker false
     * @param chargingStation is the class
     */
    public static void removeFavorite(ChargingStation chargingStation)
    {
        for(int i = 0; i < favoriteList.size(); i++)
        {
            if(favoriteList.get(i).equals(chargingStation))
            {
                favoriteList.remove(i);
                chargingStation.setShowMarker(true);
                return;
            }
        }
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

    /**
     * A function to save the data from favorite list or defective list to sharedpreferences
     * @param favorite is a boolean, whether it is a favorite or defective
     */
    public static void saveData(boolean favorite, Context context)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json;
        if(favorite)
        {
            json = gson.toJson(ContainerAndGlobal.getFavoriteList());
            editor.putString("FavoriteList", json);
        }
        else
        {
            json = gson.toJson(ContainerAndGlobal.getDefectiveList());
            editor.putString("DefectiveList", json);
        }
        editor.apply();
    }
}