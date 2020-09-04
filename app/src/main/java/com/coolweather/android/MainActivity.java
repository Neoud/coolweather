package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.coolweather.android.db.Location;
import com.coolweather.android.json.WeatherDaily;
import com.coolweather.android.json.WeatherNow;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttp;
import okhttp3.Response;
import okhttp3.internal.Util;

public class MainActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView textTemp;
    private TextView textDescription;
    private LinearLayout forecastLayout;
    private TextView textWindSpeed, textHumidity, textPrecip, textPressure, textVis, textUvIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        textTemp = findViewById(R.id.text_temp);
        textDescription = findViewById(R.id.text_description);
        forecastLayout = findViewById(R.id.layout_forecast);
        textWindSpeed = findViewById(R.id.text_windSpped);
        textHumidity = findViewById(R.id.text_humidity);
        textPrecip = findViewById(R.id.text_precip);
        textPressure = findViewById(R.id.text_pressure);
        textVis = findViewById(R.id.text_vis);
        textUvIndex = findViewById(R.id.text_uvIndex);

        SharedPreferences prefs = getSharedPreferences("forecast", MODE_PRIVATE);
        String weatherNow = prefs.getString("weatherNow", null);
        String weatherDaily = prefs.getString("weatherDaily", null);
        String cityName = prefs.getString("cityName", null);
        if (weatherNow != null && weatherDaily != null && cityName != null) {
            List<WeatherDaily> daily = Utility.handleWeatherDailyResponse(weatherDaily);
            WeatherNow now = Utility.handleWeatherNowResponse(weatherNow);
            showForecast(cityName, now, daily);
        } else {
            String weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }

    }

    public void requestWeather(final String weatherId) {
        String weatherNowUrl = "https://devapi.heweather.net/v7/weather/now?location=" + weatherId + "&key=e39ae8b2838a4f84a091791ccd22ffb7";
        String weatherDailyUrl = "https://devapi.heweather.net/v7/weather/7d?location=" + weatherId + "&key=e39ae8b2838a4f84a091791ccd22ffb7";
        String cityNameUrl = "https://geoapi.heweather.net/v2/city/lookup?location=" + weatherId + "&key=e39ae8b2838a4f84a091791ccd22ffb7";
        final String[] responseNowData = {null};
        final String[] responseDailyData = {null};
        final String[] cityNameData = {null};
        HttpUtil.sendOkHttpRequest(weatherNowUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                responseNowData[0] = null;
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                responseNowData[0] = response.body().string();
            }
        });
        HttpUtil.sendOkHttpRequest(weatherDailyUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                responseDailyData[0] = null;
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                responseDailyData[0] = response.body().string();
            }
        });
        HttpUtil.sendOkHttpRequest(cityNameUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                cityNameData[0] = null;
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Location location = Utility.handleLocationResponse(response.body().string());
                cityNameData[0] = location.getName();
            }
        });

        WeatherNow weatherNow = Utility.handleWeatherNowResponse(responseNowData[0]);
        List<WeatherDaily> weatherDaily = Utility.handleWeatherDailyResponse(responseDailyData[0]);
        if (weatherNow != null && weatherDaily != null) {
            SharedPreferences.Editor editor = getSharedPreferences("forecast", MODE_PRIVATE).edit();
            editor.putString("weatherNow", responseNowData[0]);
            editor.putString("weatherDaily", responseDailyData[0]);
            editor.putString("cityName", cityNameData[0]);
            editor.apply();
            showForecast(cityNameData[0], weatherNow, weatherDaily);
        } else {
            Toast.makeText(getApplicationContext(), "获取数据错误", Toast.LENGTH_LONG).show();
        }
    }

    private void showForecast(String cityName, WeatherNow weatherNow, List<WeatherDaily> weatherDaily) {
        titleCity.setText(cityName);
        textTemp.setText(weatherNow.getTemp());
        textDescription.setText(weatherNow.getText());

        for (WeatherDaily daily : weatherDaily) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView textFxDate = view.findViewById(R.id.text_fxDate);
            TextView textDescriptionDaily = view.findViewById(R.id.text_description_daily);
            TextView textMaxTemp = view.findViewById(R.id.text_max_temp);
            TextView textMinTemp = view.findViewById(R.id.text_min_temp);
            textFxDate.setText(daily.getDate()+"");
            textDescriptionDaily.setText(daily.getText()+"");
            textMaxTemp.setText(daily.getTempMax()+"");
            textMinTemp.setText(daily.getTempMin()+"");
            forecastLayout.addView(view);
        }

        WeatherDaily daily = weatherDaily.get(0);
        textWindSpeed.setText(daily.getWindSpeed()+"");
        textHumidity.setText(daily.getHumidity()+"");
        textPrecip.setText(daily.getPrecip()+"");
        textPressure.setText(daily.getPressure()+"");
        textVis.setText(daily.getVis()+"");
        textUvIndex.setText(daily.getUvIndex()+"");
    }

}