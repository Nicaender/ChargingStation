package com.nzse_chargingstation.app.classes;

public class Defective {
    private final ChargingStation defectiveCs;
    private final int indexInArray;
    private final Favorite defectiveFavorite;
    private final String reason;
    private boolean marked = false;

    public Defective(ChargingStation defectiveCs, int indexInArray, Favorite defectiveFavorite, String reason) {
        this.defectiveCs = defectiveCs;
        this.indexInArray = indexInArray;
        this.defectiveFavorite = defectiveFavorite;
        this.reason = reason;
    }

    public ChargingStation getDefectiveCs() {
        return defectiveCs;
    }

    public int getIndexInArray() {
        return indexInArray;
    }

    public Favorite getDefectiveFavorite() {
        return defectiveFavorite;
    }

    public String getReason() {
        return reason;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }
}
