package com.nzse_chargingstation.app.classes;

import java.util.Comparator;

public class DefectiveDistanceComparator implements Comparator<Defective> {
    public int compare(Defective df1, Defective df2)
    {
        double d1, d2;
        d1 = ContainerAndGlobal.calculateLength(df1.getDefectiveCs().getLocation(), ContainerAndGlobal.getCurrentLocation());
        d2 = ContainerAndGlobal.calculateLength(df2.getDefectiveCs().getLocation(), ContainerAndGlobal.getCurrentLocation());

        double distance = d1 - d2;
        if(distance < -0.00001)
            return -1;
        if(distance > 0.00001)
            return 1;
        return 0;
    }
}
