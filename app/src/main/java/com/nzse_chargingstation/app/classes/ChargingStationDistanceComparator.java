package com.nzse_chargingstation.app.classes;

import java.util.Comparator;

public class ChargingStationDistanceComparator implements Comparator<ChargingStation> {
    public int compare(ChargingStation cs1, ChargingStation cs2)
    {
        double distance = (ContainerAndGlobal.calculateLength(cs1.getLocation(), ContainerAndGlobal.getCurrentLocation()) - ContainerAndGlobal.calculateLength(cs2.getLocation(), ContainerAndGlobal.getCurrentLocation()));
        if(distance < -0.00001)
            return -1;
        if(distance > 0.00001)
            return 1;
        return 0;
    }
}
