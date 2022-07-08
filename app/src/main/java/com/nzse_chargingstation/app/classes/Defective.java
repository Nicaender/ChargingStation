package com.nzse_chargingstation.app.classes;

/**
 * A defective class that contains the defective charging station with the reason.
 */
public class Defective {
    private final ChargingStation defectiveCs;
    private final String reason;
    private boolean marked = false;

    public Defective(ChargingStation defectiveCs, String reason) {
        this.defectiveCs = defectiveCs;
        this.reason = reason;
    }

    public ChargingStation getDefectiveCs() {
        return defectiveCs;
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
