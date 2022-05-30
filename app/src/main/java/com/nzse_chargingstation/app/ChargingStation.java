package com.nzse_chargingstation.app;

import com.google.android.gms.maps.model.LatLng;

public class ChargingStation {
    public String getAddress() {
        return address;
    }

    public LatLng getLocation() {
        return location;
    }

    private String address;
    private LatLng location;

    public ChargingStation(String address, double latitude, double longitude) {
        this.address = address;
        this.location = new LatLng(latitude, longitude);
    }
}