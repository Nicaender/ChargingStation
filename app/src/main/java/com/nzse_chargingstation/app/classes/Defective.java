package com.nzse_chargingstation.app.classes;

import com.nzse_chargingstation.app.R;

public class Defective {
    private final ChargingStation defective_cs;
    private final String reason;
    private boolean marked = false;

    public Defective(ChargingStation defective_cs, String reason) {
        this.defective_cs = defective_cs;
        this.reason = reason;
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
}
