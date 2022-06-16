package com.nzse_chargingstation.app.classes;

public class Defective {
    private final ChargingStation defectiveCs;
    private final boolean favorite;
    private final String reason;
    private boolean marked = false;

    public Defective(ChargingStation defectiveCs, boolean favorite, String reason) {
        this.defectiveCs = defectiveCs;
        this.favorite = favorite;
        this.reason = reason;
    }

    public ChargingStation getDefectiveCs() {
        return defectiveCs;
    }

    public boolean isFavorite() {
        return favorite;
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
