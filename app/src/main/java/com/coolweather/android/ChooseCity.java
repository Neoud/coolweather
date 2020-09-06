package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
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

    private ImageButton button;
    private TextView locationNow;
    private LinearLayout topCityLayout;
    private Location location;
    private String weatherId;

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

        initLocation();
        initHotCity();
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
                if (longitude != 0.0 && latitude != 0.0) {
                    HttpUtil.sendOkHttpRequest("https://geoapi.heweather.net/v2/city/lookup?location="+longitude+","+latitude+"&number=1&"+"key=e39ae8b2838a4f84a091791ccd22ffb7", new okhttp3.Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            locationNow.setText("定位失败");
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            location = Utility.handleLocationResponse(response.body().string());
                            locationNow.setText(location.getName());
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
        List<Location> allLocations = LitePal.findAll(Location.class);
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
                }
            });
            allLocations = LitePal.findAll(Location.class);
        }
        showTopCity(allLocations);
    }

    /**
     * 显示热门城市信息
     * @param locations
     */
    private void showTopCity(List<Location> locations) {
        if (locations.size() >= 20) {
            for (int i = 0 ; i < 4 ; i ++) {
                View view = LayoutInflater.from(this).inflate(R.layout.top_ctiy_item, topCityLayout, false);
                TextView city1 = findViewById(R.id.text_topCityName1);
                TextView city2 = findViewById(R.id.text_topCityName2);
                TextView city3 = findViewById(R.id.text_topCityName3);
                TextView city4 = findViewById(R.id.text_topCityName4);
                TextView city5 = findViewById(R.id.text_topCityName5);
                city1.setText(locations.get(5*i).getName());
                city2.setText(locations.get(5*i+1).getName());
                city3.setText(locations.get(5*i+2).getName());
                city4.setText(locations.get(5*i+3).getName());
                city5.setText(locations.get(5*i+4).getName());
                topCityLayout.addView(view);
            }
        }
    }
}