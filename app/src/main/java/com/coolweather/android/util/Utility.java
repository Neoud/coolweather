package com.coolweather.android.util;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.coolweather.android.db.Location;
import com.coolweather.android.json.WeatherDaily;
import com.coolweather.android.json.WeatherNow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Utility {

    private static final String TAG = "Utility";


    /**
     * 处理服务端返回的Location信息，
     * @param responseData 服务端返回的消息
     * @return 地理位置信息Location实例
     */
    public static Location handleLocationResponse(String responseData) {
        Location rLocation = null;
        if (!TextUtils.isEmpty(responseData)) {
            try {
                JSONObject object = new JSONObject(responseData);
                String status = object.get("code").toString();
                if (status.equals("200")) {
                    JSONArray allLocations = object.getJSONArray("location");
                    for (int i = 0; i < allLocations.length(); i ++) {
                        JSONObject location = allLocations.getJSONObject(i);
                        String name = location.get("name").toString();
                        int id = location.getInt("id");
                        Double lat = location.getDouble("lat");
                        Double lon  = location.getDouble("lon");
                        Location location1 = new Location();
                        location1.setLoId(id);
                        location1.setName(name);
                        location1.setLon(lon);
                        location1.setLat(lat);
                        rLocation = location1;
                    }
                } else {
                    return rLocation;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return rLocation;
    }


    /**
     * 处理服务端返回的热门城市信息，用LitePal存储数据
     * @param responseData 服务端返回的数据
     * @return 获取、存储数据成功与否
     */
    public static boolean handleTopLocationResponse(String responseData) {
        if (!TextUtils.isEmpty(responseData)) {
            try {
                JSONObject object = new JSONObject(responseData);
                String status = object.get("code").toString();
                JSONArray allLocations = object.getJSONArray("topCityList");
                for (int i = 0; i < allLocations.length(); i ++) {
                    JSONObject location = allLocations.getJSONObject(i);
                    String name = location.getString("name").toString();
                    int id = location.getInt("id");
                    Double lat = location.getDouble("lat");
                    Double lon = location.getDouble("lon");
                    Location location1 = new Location();
                    location1.setLoId(id);
                    location1.setName(name);
                    location1.setLat(lat);
                    location1.setLon(lon);
                    location1.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 处理服务端返回的当日天气信息
     * @param responseData 返回的天气JSON数据
     * @return 天气信息WeatherNow的实例
     */
    public static WeatherNow handleWeatherNowResponse(String responseData) {
        WeatherNow weatherNow = null;
        if (!TextUtils.isEmpty(responseData)) {
            try {
                JSONObject object = new JSONObject(responseData);
                String code = object.getString("code");
                if ("200".equals(code)) {
                    JSONObject now = object.getJSONObject("now");
                    int temp = now.getInt("temp");
                    String text = now.getString("text").toString();
                    weatherNow = new WeatherNow(temp, text);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return weatherNow;
    }


    /**
     * 处理服务端返回的未来几天的天气信息
     * @param responseData 返回的天气数据
     * @return 未来几天天气信息的列表List<WeatherDaily>的实例
     */
    public static List<WeatherDaily> handleWeatherDailyResponse(String responseData) {
        List<WeatherDaily> weatherDaily = null;
        List<WeatherDaily> dailies = new ArrayList<>();
        if (!TextUtils.isEmpty(responseData)) {
            try {
                JSONObject object = new JSONObject(responseData);
                String code = object.getString("code");
                if ("200".equals(code)) {
                    JSONArray allDaily = object.getJSONArray("daily");
                    for (int i = 0; i < allDaily.length(); i ++) {
                        JSONObject daily = allDaily.getJSONObject(i);
                        String fxDate = daily.getString("fxDate").toString();
                        int tempMax = daily.getInt("tempMax");
                        int tempMin = daily.getInt("tempMin");
                        String icon = daily.getString("iconDay").toString();
                        String text = daily.getString("textDay").toString();
                        int windSpeed = daily.getInt("windSpeedDay");
                        int humidity = daily.getInt("humidity");
                        double precip = daily.getDouble("precip");
                        int pressure = daily.getInt("pressure");
                        int vis = daily.getInt("vis");
                        int uvIndex = daily.getInt("uvIndex");
                        WeatherDaily weatherDaily1 = new WeatherDaily(fxDate, tempMax, tempMin, icon, text
                                , windSpeed, humidity, precip, pressure, vis, uvIndex);
                        dailies.add(weatherDaily1);
                    }
                    weatherDaily = dailies;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return weatherDaily;
    }
}
