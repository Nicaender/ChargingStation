package com.nzse_chargingstation.app.classes;

public class Defective {
    private final ChargingStation defective_cs;
    private final String reason;
    private boolean marked = false;
    private boolean isInFavorite = false;

    public Defective(ChargingStation defective_cs, String reason, boolean isInFavorite) {
        this.defective_cs = defective_cs;
        this.reason = reason;
        this.isInFavorite = isInFavorite;
    }

    public ChargingStation getDefective_cs() {
        return defective_cs;
    }

    public String getReason() {
        return reason;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public boolean isInFavorite() {
        return isInFavorite;
    }
}
