package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

public class ChooseCity extends AppCompatActivity {

    public LocationClient mLocationClient = null;
    private double latitude = 0.0;
    private double longitude = 0.0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_city3);
        initLocation();
    }

    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                latitude = bdLocation.getLatitude();    //获取纬度信息
                longitude = bdLocation.getLongitude();    //获取经度信息
            }
        });
        mLocationClient.start();
    }
}