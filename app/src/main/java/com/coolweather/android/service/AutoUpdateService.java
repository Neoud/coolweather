package com.coolweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.ImageReader;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Toast;

import com.coolweather.android.MainActivity;
import com.coolweather.android.db.Location;
import com.coolweather.android.json.WeatherDaily;
import com.coolweather.android.json.WeatherNow;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent intent1 = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this,0, intent1, 0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences prefs = getSharedPreferences("forecast", MODE_PRIVATE);
        String weatherId = prefs.getString("cityId", null);
        if (weatherId != null) {
            String weatherNowUrl = "https://devapi.heweather.net/v7/weather/now?location=" + weatherId + "&key=e39ae8b2838a4f84a091791ccd22ffb7";
            String weatherDailyUrl = "https://devapi.heweather.net/v7/weather/3d?location=" + weatherId + "&key=e39ae8b2838a4f84a091791ccd22ffb7";
            String cityNameUrl = "https://geoapi.heweather.net/v2/city/lookup?location=" + weatherId + "&key=e39ae8b2838a4f84a091791ccd22ffb7";
            final SharedPreferences.Editor editor = getSharedPreferences("forecast", MODE_PRIVATE).edit();
            HttpUtil.sendOkHttpRequest(weatherNowUrl, new okhttp3.Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Toast.makeText(getApplicationContext(), "获取当天天气数据失败", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseData = response.body().string();
                    WeatherNow weatherNow = Utility.handleWeatherNowResponse(responseData);
                    if (weatherNow != null) {
                        editor.putString("weatherNow", responseData);
                        editor.apply();
                    } else {
                        Toast.makeText(getApplicationContext(), "解析数据Now错误", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            HttpUtil.sendOkHttpRequest(weatherDailyUrl, new okhttp3.Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Toast.makeText(getApplicationContext(), "获取未来几天数据失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseData = response.body().string();
                    List<WeatherDaily> weatherDailies = Utility.handleWeatherDailyResponse(responseData);
                    if (weatherDailies != null) {
                        editor.putString("weatherDaily", responseData);
                        editor.apply();
                    } else {
                        Toast.makeText(getApplicationContext(), "解析Daily数据失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            HttpUtil.sendOkHttpRequest(cityNameUrl, new okhttp3.Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Toast.makeText(getApplicationContext(), "获取当天其他数据失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final String responseData = response.body().string();

                    Location location = Utility.handleLocationResponse(responseData);
                    if (location != null) {
                        editor.putString("cityName", responseData);
                        editor.apply();
                    } else {
                        Toast.makeText(getApplicationContext(), "解析name数据失败", Toast.LENGTH_SHORT).show();
                    }}
            });
        }
    }
}
