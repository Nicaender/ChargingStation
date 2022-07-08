package com.nzse_chargingstation.app.classes;

import java.util.Comparator;

/**
 * A distance comparator class to measure two charging stations, which one is closer to the user.
 */
public class ChargingStationDistanceComparator implements Comparator<ChargingStation> {
    public int compare(ChargingStation cs1, ChargingStation cs2) {
        if(ContainerAndGlobal.getCurrentLocation() == null)
            return 0;
        double distance = (ContainerAndGlobal.calculateLength(cs1.getPosition(), ContainerAndGlobal.getCurrentLocation()) - ContainerAndGlobal.calculateLength(cs2.getPosition(), ContainerAndGlobal.getCurrentLocation()));
        if(distance < -0.00001)
            return -1;
        if(distance > 0.00001)
            return 1;
        return 0;
    }
}
