package com.nzse_chargingstation.app.classes;

import com.google.android.gms.maps.model.LatLng;

public class ChargingStation {

    public ChargingStation(String address, double latitude, double longitude) {
        this.address = address;
        this.location = new LatLng(latitude, longitude);
    }

    private final String address;
    private final LatLng location;

    public String getAddress() {
        return address;
    }

    public LatLng getLocation() {
        return location;
    }
}