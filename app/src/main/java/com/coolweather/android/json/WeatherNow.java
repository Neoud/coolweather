package com.coolweather.android.json;

public class WeatherNow {
    private int temp;
    private String text;

    public WeatherNow(){};

    public WeatherNow(int temp, String text) {
        this.temp = temp;
        this.text = text;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getTemp() {
        return temp;
    }

    public String getText() {
        return text;
    }
}
