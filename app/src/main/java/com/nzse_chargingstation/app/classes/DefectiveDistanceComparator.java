package com.nzse_chargingstation.app.classes;

import java.util.Comparator;

public class DefectiveDistanceComparator implements Comparator<Defective> {
    public int compare(Defective df1, Defective df2)
    {
        double distance = (ContainerAndGlobal.calculateLength(df1.getDefectiveCs().getLocation(), ContainerAndGlobal.getCurrentLocation()) - ContainerAndGlobal.calculateLength(df2.getDefectiveCs().getLocation(), ContainerAndGlobal.getCurrentLocation()));
        if(distance < -0.00001)
            return -1;
        if(distance > 0.00001)
            return 1;
        return 0;
    }
}
