package com.nzse_chargingstation.app.classes;

public class Favorite {
    private final ChargingStation favoriteCs;
    private final int indexInArray;

    public Favorite(ChargingStation favoriteCs, int indexInArray) {
        this.favoriteCs = favoriteCs;
        this.indexInArray = indexInArray;
    }

    public ChargingStation getFavoriteCs() {
        return favoriteCs;
    }

    public int getIndexInArray() {
        return indexInArray;
    }
}
