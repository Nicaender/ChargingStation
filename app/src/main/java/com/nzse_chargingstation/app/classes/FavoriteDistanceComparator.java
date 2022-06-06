package com.nzse_chargingstation.app.classes;

import java.util.Comparator;

public class FavoriteDistanceComparator implements Comparator<Favorite> {
    public int compare(Favorite fv1, Favorite fv2)
    {
        double distance = (ContainerAndGlobal.calculateLength(fv1.getFavoriteCs().getLocation(), ContainerAndGlobal.getCurrentLocation()) - ContainerAndGlobal.calculateLength(fv2.getFavoriteCs().getLocation(), ContainerAndGlobal.getCurrentLocation()));
        if(distance < -0.00001)
            return -1;
        if(distance > 0.00001)
            return 1;
        return 0;
    }
}
