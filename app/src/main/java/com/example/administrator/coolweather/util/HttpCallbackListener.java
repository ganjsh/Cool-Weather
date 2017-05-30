package com.example.administrator.coolweather.util;

/**
 * Created by sysu on 2017/5/30.
 */

public interface HttpCallbackListener {
    public void onFinish(String response);

    public void onError(Exception e);
}
