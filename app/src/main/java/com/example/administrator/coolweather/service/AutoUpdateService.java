package com.example.administrator.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.example.administrator.coolweather.gson.Weather;
import com.example.administrator.coolweather.model.CoolWeatherDB;
import com.example.administrator.coolweather.util.HttpCallbackListener;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;

/**
 * Created by Administrator on 2017/6/5.
 */

public class AutoUpdateService extends Service {

    public CoolWeatherDB coolWeatherDB = CoolWeatherDB.getInstance(this);
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if(weatherString != null){
            Weather weather = Utility.handleWeatherResponse(coolWeatherDB, weatherString);
            final String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=e8c0a2140e2b4aeba6384a6e0212da49";
            HttpUtil.sendHttpRequest(weatherUrl, new HttpCallbackListener() {
                @Override
                public void onFinish(String response) {
                    Weather weather = Utility.handleWeatherResponse(coolWeatherDB, response);
                    if(weather != null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", response);
                        editor.apply();
                    }
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
