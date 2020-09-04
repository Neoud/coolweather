package com.coolweather.android.db;

import org.litepal.crud.LitePalSupport;

public class Location extends LitePalSupport {
    private int loId;
    private String name;
    private double lat;
    private double lon;


    public int getLoId() {
        return loId;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLoId(int loId) {
        this.loId = loId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
