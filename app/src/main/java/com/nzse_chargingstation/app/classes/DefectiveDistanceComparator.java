package com.nzse_chargingstation.app.classes;

import java.util.Comparator;

/**
 * A distance comparator for defective class to measure two charging stations, which one is closer to the user.
 */
public class DefectiveDistanceComparator implements Comparator<Defective> {
    public int compare(Defective df1, Defective df2) {
        double d1, d2;
        d1 = ContainerAndGlobal.calculateLength(df1.getDefectiveCs().getPosition(), ContainerAndGlobal.getCurrentLocation());
        d2 = ContainerAndGlobal.calculateLength(df2.getDefectiveCs().getPosition(), ContainerAndGlobal.getCurrentLocation());

        double distance = d1 - d2;
        if(distance < -0.00001)
            return -1;
        if(distance > 0.00001)
            return 1;
        return 0;
    }
}
