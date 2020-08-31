package com.coolweather.android.util;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    private static final String TAG = "Utility";

    public static boolean handleLocationResponse(String responseData) {
        if (!TextUtils.isEmpty(responseData)) {
            try {
                JSONObject object = new JSONObject(responseData);
                String status = object.get("code").toString();
                JSONArray allLocations = object.getJSONArray("location");
                for (int i = 0; i < allLocations.length(); i ++) {
                    JSONObject location = allLocations.getJSONObject(i);
                    String name = location.get("name").toString();
                    String id = location.get("id").toString();
                    String lat = location.get("lat").toString();
                    String lon  = location.get("lon").toString();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleTopLocationResponse(String responseData) {
        if (!TextUtils.isEmpty(responseData)) {
            try {
                JSONObject object = new JSONObject(responseData);
                String status = object.get("code").toString();
                JSONArray allLocations = object.getJSONArray("topCityList");
                for (int i = 0; i < allLocations.length(); i ++) {
                    JSONObject location = allLocations.getJSONObject(i);
                    String name = location.get("name").toString();
                    String id = location.get("id").toString();
                    String lat = location.get("lat").toString();
                    String lon = location.get("lon").toString();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
