package com.nzse_chargingstation.app.classes;

import com.nzse_chargingstation.app.R;

public class Defective {
    private final ChargingStation defective_cs;
    private final String reason;
    private boolean fixed = false;
    private String technician = "mark";

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

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public String getTechnician() {
        return technician;
    }

    public void setTechnician(String technician) {
        this.technician = technician;
    }
}
