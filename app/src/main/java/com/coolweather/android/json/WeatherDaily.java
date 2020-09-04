package com.coolweather.android.json;

public class WeatherDaily {
    private String date;
    private int tempMax;
    private int tempMin;
    private String icon;
    private String text;
    private int windSpeed;
    private int humidity;
    private double precip;
    private int pressure;
    private int vis;
    private int uvIndex;

    public WeatherDaily(){};

    public WeatherDaily(String date, int tempMax, int tempMin, String icon, String text, int windSpeed, int humidity, double precip, int pressure, int vis, int uvIndex) {
        this.date = date;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.icon = icon;
        this.text = text;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
        this.precip = precip;
        this.pressure = pressure;
        this.vis = vis;
        this.uvIndex = uvIndex;
    }


    public void setDate(String date) {
        this.date = date;
    }

    public void setTempMax(int tempMax) {
        this.tempMax = tempMax;
    }

    public void setTempMin(int tempMin) {
        this.tempMin = tempMin;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setWindSpeed(int windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public void setPrecip(double precip) {
        this.precip = precip;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public void setVis(int vis) {
        this.vis = vis;
    }

    public void setUvIndex(int uvIndex) {
        this.uvIndex = uvIndex;
    }

    public String getDate() {
        return date;
    }

    public int getTempMax() {
        return tempMax;
    }

    public int getTempMin() {
        return tempMin;
    }

    public String getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public int getWindSpeed() {
        return windSpeed;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getPrecip() {
        return precip;
    }

    public int getPressure() {
        return pressure;
    }

    public int getVis() {
        return vis;
    }

    public int getUvIndex() {
        return uvIndex;
    }
}
