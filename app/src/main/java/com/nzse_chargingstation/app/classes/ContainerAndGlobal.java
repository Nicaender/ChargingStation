package com.nzse_chargingstation.app.classes;

import android.content.Context;
import android.location.Location;
import android.util.Pair;

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
import java.util.Objects;

public class ContainerAndGlobal {

    private static ArrayList<ArrayList<ChargingStation>> chargingStationHashList = new ArrayList<>();
    private static ArrayList<ArrayList<ChargingStation>> chargingStationHashListFiltered = new ArrayList<>();
    private static ArrayList<ChargingStation> chargingStationFavorites = new ArrayList<>();
    private static ArrayList<Defective> defectiveList = new ArrayList<>();
    private static ArrayList<Pair<ChargingStation, Integer>> fixedChargingStationBuffer = new ArrayList<>();
    private static double filterRange = 0;
    private static Location currentLocation = null;
    private static boolean firstTime = true;
    private static ChargingStation reportedChargingStation = null;
    private static Marker reportedMaker = null;
    private static boolean changedSetting = false;
    public static final DecimalFormat df = new DecimalFormat("#.##");
    private static final int hashSize = 1000;

    public static ArrayList<ArrayList<ChargingStation>> getChargingStationHashList() {
        return chargingStationHashList;
    }

    public static ArrayList<ArrayList<ChargingStation>> getChargingStationHashListFiltered() {
        return chargingStationHashListFiltered;
    }

    public static ArrayList<ChargingStation> getChargingStationFavorites() {
        return chargingStationFavorites;
    }

    public static ArrayList<Defective> getDefectiveList() {
        return defectiveList;
    }

    public static ArrayList<Pair<ChargingStation, Integer>> getFixedChargingStationBuffer() {
        return fixedChargingStationBuffer;
    }

    public static void setFilterRange(double filterRange) {
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

    public static Marker getReportedMaker() {
        return reportedMaker;
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
        // keluarin dari filter
        for(int i = 0; i < chargingStationHashListFiltered.size(); i++)
        {
            for(int j = 0; j < chargingStationHashListFiltered.get(i).size(); j++)
            {
                if(calculateLength(chargingStationHashListFiltered.get(i).get(j).getLocation(), currentLocation) > filterRange)
                {
                    chargingStationHashList.get(i).add(chargingStationHashListFiltered.get(i).get(j));
                    chargingStationHashListFiltered.get(i).remove(j);
                    j--;
                }
            }
        }

        // masukin ke filter
        for(int i = 0; i < chargingStationHashList.size(); i++)
        {
            for(int j = 0; j < chargingStationHashList.get(i).size(); j++)
            {
                if(calculateLength(chargingStationHashList.get(i).get(j).getLocation(), currentLocation) < filterRange)
                {
                    chargingStationHashListFiltered.get(i).add(chargingStationHashList.get(i).get(j));
                    chargingStationHashList.get(i).remove(j);
                    j--;
                }
            }
        }
    }

    /**
     * check whether a charging station is already in a favorite
     * @param input is the charging station that wants to be checked
     * @return true if it is already or not found, return false if its not yet in favorite
     */
    public static boolean isAlreadyFavorite(ChargingStation input)
    {
        if(input == null)
            return true;

        for(int i = 0; i < chargingStationFavorites.size(); i++)
        {
            if(chargingStationFavorites.get(i).equals(input))
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
     * @return -1 = error, 1 = normal list, 2 = filtered list, 4 = fav list
     */
    public static int addOrRemoveFavorite(ChargingStation input, boolean add)
    {
        {
            if(input == null)
                return -1;

            if(add)
            {
                if(addOrRemoveChargingStation(input, false) == 0)
                chargingStationFavorites.add(input);
                return 4;
            }
            else
            {
                for(int i = 0; i < chargingStationFavorites.size(); i++)
                {
                    if(chargingStationFavorites.get(i).equals(input))
                    {
                        chargingStationFavorites.remove(i);
                        return addOrRemoveChargingStation(input, true);
                        // return value whether it is filtered
                    }
                }
            }

            return -1;
        }
    }

    /**
     * Add a defective charging station to the defective list and remove it from normal list.
     * Then set reported_charging_station to null. It can also remove a defective class from the
     * defective array and add that charging station back to normal list
     * @param input is the defective class that contains the charging station and the reason
     * @param add is a boolean, whether to add or to remove the defective class
     * @return -1 = error, 1 = normal list, 2 = filtered list, 3 = defective list, 4 = fav list
     */
    public static int addOrRemoveDefective(Defective input, boolean add)
    {
        if(input == null)
            return -1;

        if(add)
        {
            defectiveList.add(input);
            addOrRemoveChargingStation(input.getDefective_cs(), false);
            reportedChargingStation = null;
            reportedMaker.remove();
            reportedMaker = null;
            return 3;
        }
        else
        {
            for(int i = 0; i < defectiveList.size(); i++)
            {
                if(defectiveList.get(i).equals(input))
                {
                    defectiveList.remove(i);
                    if(input.isInFavorite())
                        return addOrRemoveFavorite(input.getDefective_cs(), true);
                    return addOrRemoveChargingStation(input.getDefective_cs(), true);
                    // return the value whether it is added to normal or filtered list or
                    // favorite list
                }
            }
        }

        return -1;
    }

    /**
     * Add a charging station to either the normal list or the filtered one using hash key by its
     * address
     * @param input is the charging station that wants to be removed or to be added
     * @param add is the boolean, whether to add it or remove it
     * @return -1 = error, 1 = normal list, 2 = filtered list, 0 = removed
     */
    public static int addOrRemoveChargingStation(ChargingStation input, boolean add)
    {
        if(input == null)
            return -1;

        int key = hashing(input.getStrasse());
        if(add)
        {
            while(key >= chargingStationHashList.size())
                chargingStationHashList.add(new ArrayList<>());
            while(key >= chargingStationHashListFiltered.size())
                chargingStationHashListFiltered.add(new ArrayList<>());

            if(currentLocation == null)
            {
                chargingStationHashList.get(key).add(input);
            }
            else
            {
                if(calculateLength(input.getLocation(), currentLocation) < filterRange)
                {
                    chargingStationHashListFiltered.get(key).add(input);
                    return 2;
                }
                else
                {
                    chargingStationHashList.get(key).add(input);
                }
            }
            return 1;
        }
        else
        {
            for(int i = 0; i < chargingStationHashList.get(key).size(); i++)
            {
                if(chargingStationHashList.get(key).get(i).equals(input))
                {
                    chargingStationHashList.get(key).remove(i);
                    return 0;
                }
            }
            for(int i = 0; i < chargingStationHashListFiltered.get(key).size(); i++)
            {
                if(chargingStationHashListFiltered.get(key).get(i).equals(input))
                {
                    chargingStationHashListFiltered.get(key).remove(i);
                    return 0;
                }
            }
            for(int i = 0; i < getChargingStationFavorites().size(); i++)
            {
                if(getChargingStationFavorites().get(i).equals(input))
                {
                    getChargingStationFavorites().remove(i);
                    return 0;
                }
            }
        }

        return -1;
    }

    /**
     * Search for a charging station using its location and then hash
     * @param marker the location of searched charging station
     * @return charging station with the same location as the marker
     */
    public static ChargingStation searchChargingStation(Marker marker)
    {
        int key = hashing(Objects.requireNonNull(marker.getTitle()));
        if(key == -1)
            return null;
        for(int i = 0; i < chargingStationHashList.get(key).size(); i++)
        {
            if(marker.getPosition().equals(chargingStationHashList.get(key).get(i).getLocation()))
                return chargingStationHashList.get(key).get(i);
        }
        for(int i = 0; i < chargingStationHashListFiltered.get(key).size(); i++)
        {
            if(marker.getPosition().equals(chargingStationHashListFiltered.get(key).get(i).getLocation()))
                return chargingStationHashListFiltered.get(key).get(i);
        }
        for(int i = 0; i < chargingStationFavorites.size(); i++)
        {
            if(marker.getPosition().equals(chargingStationFavorites.get(i).getLocation()))
                return chargingStationFavorites.get(i);
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
                (int) Integer.parseInt(chargingstation.get("Postleitzahl").toString()),
                (String) chargingstation.get("Ort"),
                (String) chargingstation.get("Bundesland"),
                (String) chargingstation.get("Kreis/kreisfreie Stadt"),
                (double) Double.parseDouble(chargingstation.get("Breitengrad").toString().replace(",",".")),
                (double) Double.parseDouble(chargingstation.get("Längengrad").toString().replace(",",".")),
                (String) chargingstation.get("Inbetriebnahmedatum"),
                (double) Double.parseDouble(chargingstation.get("Anschlussleistung").toString().replace(",",".")),
                (String) chargingstation.get("Art der Ladeeinrichung"),
                (int) Integer.parseInt(chargingstation.get("Anzahl Ladepunkte").toString()),
                (String) chargingstation.get("Steckertypen1"),
                (double) Double.parseDouble(chargingstation.get("P1 [kW]").toString().replace(",",".")),
                (String) chargingstation.get("Public Key1"),
                (String) chargingstation.get("Steckertypen2"),
                (chargingstation.get("P2 [kW]").toString().isEmpty() ? 0.0  : (double) Double.parseDouble(chargingstation.get("P2 [kW]").toString().replace(",","."))),
                (String) chargingstation.get("Public Key2"),
                (String) chargingstation.get("Steckertypen3"),
                (chargingstation.get("P3 [kW]").toString().isEmpty() ? 0.0  : (double) Double.parseDouble(chargingstation.get("P3 [kW]").toString().replace(",","."))) ,
                (String) chargingstation.get("Public Key3"),
                (String) chargingstation.get("Steckertypen4"),
                (chargingstation.get("P4 [kW]").toString().isEmpty() ? 0.0  : (double) Double.parseDouble(chargingstation.get("P4 [kW]").toString().replace(",","."))),
                (String) chargingstation.get("Public Key4")
        );

        addOrRemoveChargingStation(tmpChargingStation, true);
    }

    /**
     * convert from string into an integer (hashing)
     * @param name is the string that will be converted into an int
     * @return position of the item inside 2 dimensional array
     */
    public static int hashing(String name)
    {
        double result = -1;
        for(int i = 0; i < name.length(); i++)
        {
            result = result + (int) name.charAt(i);
        }

        return (int) (result % hashSize);
    }
}