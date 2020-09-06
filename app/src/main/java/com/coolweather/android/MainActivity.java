package com.coolweather.android;

import androidx.appcompat.app.ActionBar;
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
import org.w3c.dom.ls.LSException;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttp;
import okhttp3.Response;
import okhttp3.internal.Util;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

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
        // 初始化各控件
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

        // 打开缓存文件，并提取数据
        SharedPreferences prefs = getSharedPreferences("forecast", MODE_PRIVATE);
        String weatherNow = prefs.getString("weatherNow", null);
        String weatherDaily = prefs.getString("weatherDaily", null);
        String cityName = prefs.getString("cityName", null);
        if (weatherNow != null && weatherDaily != null && cityName != null) {
            // 有缓存时直接解析数据
            List<WeatherDaily> daily = Utility.handleWeatherDailyResponse(weatherDaily);
            WeatherNow now = Utility.handleWeatherNowResponse(weatherNow);
            String name = Utility.handleLocationResponse(cityName).getName();
            titleCity.setText(name);
            showWeatherNow(now);
            showWeatherDaily(daily);
        } else {
            // 无缓存时去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            requestWeather("101010100");
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }


    /**
     * 根据城市ID请求城市天气信息：当天信息、未来几天天气信息、城市名
     * @param weatherId 城市ID
     */
    public void requestWeather(final String weatherId) {
        String weatherNowUrl = "https://devapi.heweather.net/v7/weather/now?location=" + weatherId + "&key=e39ae8b2838a4f84a091791ccd22ffb7";
        String weatherDailyUrl = "https://devapi.heweather.net/v7/weather/3d?location=" + weatherId + "&key=e39ae8b2838a4f84a091791ccd22ffb7";
        String cityNameUrl = "https://geoapi.heweather.net/v2/city/lookup?location=" + weatherId + "&key=e39ae8b2838a4f84a091791ccd22ffb7";
        final SharedPreferences.Editor editor = getSharedPreferences("forecast", MODE_PRIVATE).edit();
        HttpUtil.sendOkHttpRequest(weatherNowUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "获取当天天气数据失败", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseData = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WeatherNow weatherNow = Utility.handleWeatherNowResponse(responseData);
                        if (weatherNow != null) {
                            editor.putString("weatherNow", responseData);
                            editor.apply();
                            showWeatherNow(weatherNow);
                        } else {
                            Toast.makeText(getApplicationContext(), "解析数据错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        HttpUtil.sendOkHttpRequest(weatherDailyUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "获取未来几天数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseData = response.body().string();
                Log.d(TAG, "onResponse: " + responseData);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<WeatherDaily> weatherDailies = Utility.handleWeatherDailyResponse(responseData);
                        if (weatherDailies != null) {
                            editor.putString("weatherDaily", responseData);
                            editor.apply();
                            showWeatherDaily(weatherDailies);
                        } else {
                            Toast.makeText(MainActivity.this, "解析数据失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        HttpUtil.sendOkHttpRequest(cityNameUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "获取当天其他数据失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseData = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Location location = Utility.handleLocationResponse(responseData);
                        if (location != null) {
                            editor.putString("cityName", responseData);
                            editor.apply();
                            titleCity.setText(location.getName());
                        } else {
                            Toast.makeText(MainActivity.this, "解析数据失败", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }


    /**
     * 显示传入的天气数据
     * @param weatherDaily 未来几天天气
     */
    private void showWeatherDaily(List<WeatherDaily> weatherDaily) {
        for (WeatherDaily daily : weatherDaily) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView textFxDate = view.findViewById(R.id.text_fxDate);
            TextView textDescriptionDaily = view.findViewById(R.id.text_description_daily);
            TextView textMaxTemp = view.findViewById(R.id.text_max_temp);
            TextView textMinTemp = view.findViewById(R.id.text_min_temp);
            textFxDate.setText(daily.getDate());
            textDescriptionDaily.setText(daily.getText()+"");
            textMaxTemp.setText(daily.getTempMax()+"℃");
            textMinTemp.setText(daily.getTempMin()+"℃");
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

    private void showWeatherNow(WeatherNow weatherNow) {
        textTemp.setText(weatherNow.getTemp()+"℃");
        textDescription.setText(weatherNow.getText());
    }

}