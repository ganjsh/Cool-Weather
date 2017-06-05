package com.example.administrator.coolweather.model;

/**
 * Created by Administrator on 2017/5/22.
 */

public class County {
    private int id;
    private String countyName;
    private int cityId;
    private String weatherId;

    public int getId() {
        return id;
    }

    public String getCountyName() {
        return countyName;
    }


    public int getCityId() {
        return cityId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }


    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }
}
