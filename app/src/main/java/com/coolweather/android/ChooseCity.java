package com.coolweather.android;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.coolweather.android.db.Location;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class ChooseCity extends AppCompatActivity {
    private static final String TAG = "ChooseCity";

    private ImageButton button;
    private TextView locationNow;
    private LinearLayout topCityLayout;
    private Location location = null;
    private String weatherId = null;

    public LocationClient mLocationClient = null;
    private double latitude = 0.0;
    private double longitude = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_city3);
        // 初始化各组建
        button = findViewById(R.id.button_back);
        locationNow = findViewById(R.id.text_locationNow);
        topCityLayout = findViewById(R.id.layout_topCity);

        locationNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnWeatherId();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 初始化当前位置和热门城市
        initLocation();
        initHotCity();

        // 系统设置
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setBarColor(Color.BLACK);
    }

    /**
     * 设置状态栏颜色
     * @param color
     */
    private void setBarColor(int color) {
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    /**
     * 定位当前位置
     */
    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                latitude = bdLocation.getLatitude();    //获取纬度信息
                longitude = bdLocation.getLongitude();    //获取经度信息
                Log.d(TAG, "onReceiveLocation: " + latitude + "\t" + longitude);
                if (longitude != 0.0 && latitude != 0.0) {
                    HttpUtil.sendOkHttpRequest("https://geoapi.heweather.net/v2/city/lookup?location="+longitude+","+latitude+"&number=1&"+"key=e39ae8b2838a4f84a091791ccd22ffb7", new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    locationNow.setText("定位失败");
                                }
                            });
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                            final String responseData = response.body().string();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    location = Utility.handleLocationResponse(responseData);
                                    if (location != null) {
                                        locationNow.setText(location.getName());
                                        weatherId = location.getLoId()+"";
                                    } else {
                                        locationNow.setText("定位失败");
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
        mLocationClient.start();

    }

    /**
     * 读取热门城市信息，先从数据库中读取，数据库中没有就从服务器请求
     */
    private void initHotCity() {
        final List<Location> allLocations = LitePal.findAll(Location.class);
        if (allLocations.size() == 0) {
            HttpUtil.sendOkHttpRequest("https://geoapi.heweather.net/v2/city/top?key=e39ae8b2838a4f84a091791ccd22ffb7&range=cn&number=20", new okhttp3.Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "热门城市加载错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    Utility.handleTopLocationResponse(response.body().string());
                    final List<Location> allLocation = LitePal.findAll(Location.class);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTopCity(allLocation);
                        }
                    });
                }
            });
        } else {
            List<Location> locations = LitePal.findAll(Location.class);
            showTopCity(locations);
        }
    }

    /**
     * 显示热门城市信息
     * @param locations
     */
    private void showTopCity(List<Location> locations) {
        if (locations.size() >= 20) {
            for (int i = 0 ; i < 5 ; i ++) {
                View view = LayoutInflater.from(this).inflate(R.layout.top_ctiy_item, topCityLayout, false);
                final TextView city1 = view.findViewById(R.id.text_topCityName1);
                final TextView city2 = view.findViewById(R.id.text_topCityName2);
                final TextView city3 = view.findViewById(R.id.text_topCityName3);
                final TextView city4 = view.findViewById(R.id.text_topCityName4);
                city1.setText(locations.get(4*i).getName());
                city2.setText(locations.get(4*i+1).getName());
                city3.setText(locations.get(4*i+2).getName());
                city4.setText(locations.get(4*i+3).getName());
                topCityLayout.addView(view);
                city1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        weatherId = getWeatherId(city1.getText().toString());
                        returnWeatherId();
                    }
                });
                city2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        weatherId = getWeatherId(city2.getText().toString());
                        returnWeatherId();
                    }
                });
                city3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        weatherId = getWeatherId(city3.getText().toString());
                        returnWeatherId();
                    }
                });
                city4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        weatherId = getWeatherId(city4.getText().toString());
                        returnWeatherId();
                    }
                });
            }
        }
    }

    /**
     * 从数据库获取热门城市的id
     * @param cityName
     * @return
     */
    private String getWeatherId(String cityName) {
        List<Location> locations = LitePal.where("name like ?", cityName).find(Location.class);
        if (locations.size() != 0) {
            String weatherId = locations.get(0).getLoId()+"";
            return weatherId;
        } else {
            return null;
        }
    }

    /**
     * 返回选择的城市id给天气界面
     */
    private void returnWeatherId() {
        Intent intent = new Intent();
        intent.putExtra("weatherId", weatherId);
        setResult(RESULT_OK, intent);
        finish();
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, ChooseCity.class);
        context.startActivity(intent);
    }

}