package com.traveller.android.db;

import java.io.Serializable;

public class Location implements Serializable {
    public Double lat;
    public Double longi;

    public Location(Double lat, Double longi) {
        this.lat = lat;
        this.longi = longi;
    }
}

