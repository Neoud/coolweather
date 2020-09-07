package com.coolweather.android;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.coolweather.android.db.Location;
import com.coolweather.android.json.WeatherDaily;
import com.coolweather.android.json.WeatherNow;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;
import org.w3c.dom.Text;
import org.w3c.dom.ls.LSException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.OkHttp;
import okhttp3.Response;
import okhttp3.internal.Util;

public class MainActivity extends AppCompatActivity {

    public SwipeRefreshLayout swipeRefreshLayout;

    final static int REQUEST_CODE = 1;
    private ScrollView weatherLayout;
    private ImageView bingPicImg;
    private TextView chooseCity;
    private TextView titleCity;
    private TextView textTemp;
    private TextView textDescription;
    private LinearLayout forecastLayout;
    private TextView textWindSpeed, textHumidity, textPrecip, textPressure, textVis, textUvIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
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
        chooseCity = findViewById(R.id.button_choose_city);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        bingPicImg = findViewById(R.id.bing_pic_img);
        chooseCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChooseCity.class);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });



        // 打开缓存文件，并提取数据
        final SharedPreferences prefs = getSharedPreferences("forecast", MODE_PRIVATE);
        String weatherNow = prefs.getString("weatherNow", null);
        String weatherDaily = prefs.getString("weatherDaily", null);
        String cityName = prefs.getString("cityName", null);
        String bingPic = prefs.getString("bing_pic", null);
        final String cityId = prefs.getString("cityId", null);
        Log.d("MainActivity", "onCreate: " + bingPic);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
        if (weatherNow != null && weatherDaily != null && cityName != null && cityId != null) {
            // 有缓存时直接解析数据
            List<WeatherDaily> daily = Utility.handleWeatherDailyResponse(weatherDaily);
            WeatherNow now = Utility.handleWeatherNowResponse(weatherNow);
            String name = Utility.handleLocationResponse(cityName).getName();
            titleCity.setText(name);
            showWeatherNow(now);
            showWeatherDaily(daily);
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                    .setMessage("暂无城市数据，请添加后再试")
                    .setCancelable(false)
                    .setPositiveButton("添加城市", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, ChooseCity.class);
                            startActivityForResult(intent, REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            dialog.show();
            // 无缓存时去服务器查询天气
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final String cityId = prefs.getString("cityId", null);
                requestWeather(cityId);
            }
        });

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("MainActivity", "onFailure: ");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic = response.body().string();
                Log.d("MainActivity", "onResponse: " + bingPic);
                SharedPreferences.Editor editor = getSharedPreferences("forecast", MODE_PRIVATE).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
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
        editor.putString("cityId", weatherId);
        editor.apply();
        HttpUtil.sendOkHttpRequest(weatherNowUrl, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "获取当天天气数据失败", Toast.LENGTH_LONG).show();
                        swipeRefreshLayout.setRefreshing(false);
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
                            Toast.makeText(getApplicationContext(), "解析数据Now错误", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
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
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseData = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<WeatherDaily> weatherDailies = Utility.handleWeatherDailyResponse(responseData);
                        if (weatherDailies != null) {
                            editor.putString("weatherDaily", responseData);
                            editor.apply();
                            showWeatherDaily(weatherDailies);
                        } else {
                            Toast.makeText(MainActivity.this, "解析Daily数据失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
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
                        swipeRefreshLayout.setRefreshing(false);
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
                            Toast.makeText(MainActivity.this, "解析name数据失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }


    /**
     * 显示传入的天气数据
     * @param weatherDaily 未来几天天气
     */
    private void showWeatherDaily(List<WeatherDaily> weatherDaily) {
        forecastLayout.removeAllViews();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    String returnData = data.getStringExtra("weatherId");
                    requestWeather(returnData);
                }
        }
    }
}