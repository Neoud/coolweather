package com.coolweather.android.db;

public class Location {
    private int code;
    private int id;
    private String name;
    private double lat;
    private double lon;

    public int getCode() {
        return code;
    }

    public int getId() {
        return id;
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

    public void setCode(int code) {
        this.code = code;
    }

    public void setId(int id) {
        this.id = id;
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
