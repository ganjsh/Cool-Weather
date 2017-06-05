package com.example.administrator.coolweather.util;

/**
 * Created by Administrator on 2017/5/22.
 */

public interface HttpCallbackListener {
    public void onFinish(String response);

    public void onError(Exception e);
}
