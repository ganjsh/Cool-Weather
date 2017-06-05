package com.example.administrator.coolweather.activity;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.coolweather.R;
import com.example.administrator.coolweather.gson.Weather;
import com.example.administrator.coolweather.model.City;
import com.example.administrator.coolweather.model.CoolWeatherDB;
import com.example.administrator.coolweather.model.County;
import com.example.administrator.coolweather.model.Province;
import com.example.administrator.coolweather.util.HttpCallbackListener;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/31.
 */

public class ChooseAreaFragment extends Fragment{
    public static final String TAG = "ChooseAreaActivity";

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private TextView mTitleText;
    private ListView mListView;
    private Button mBackBtn;
    private ProgressDialog mProgressDialog;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectProvince;
    private City selectCity;

    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        mTitleText = (TextView) view.findViewById(R.id.title_text);
        mListView = (ListView) view.findViewById(R.id.list_view);
        mBackBtn = (Button) view.findViewById(R.id.back);
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBack();
            }
        });
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        mListView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        coolWeatherDB = CoolWeatherDB.getInstance(getContext());
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectCity = cityList.get(position);
                    queryCounties();
                } else if(currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        queryProvinces();
    }

    // 查询所有省份
    public void queryProvinces() {
        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mTitleText.setText(getContext().getResources().getString(R.string.china));
            currentLevel = LEVEL_PROVINCE;
            mBackBtn.setVisibility(View.INVISIBLE);
        } else{
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    // 查询所有城市
    public void queryCities() {
        cityList = coolWeatherDB.loadCities(selectProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mTitleText.setText(selectProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
            mBackBtn.setVisibility(View.VISIBLE);
        } else{
            int provinceId = selectProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceId;
            queryFromServer(address, "city");
        }
    }

    // 查询所有县
    public void queryCounties() {
        Log.d(TAG, selectCity.getCityName() + " " + selectCity.getId());
        countyList = coolWeatherDB.loadCounties(selectCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            mTitleText.setText(selectCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else{
            int provinceId = selectProvince.getProvinceCode();
            int cityId = selectCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceId + "/" + cityId;
            queryFromServer(address, "county");
        }
    }

    // 从远程服务器上查询数据
    public void queryFromServer(final String address, final String type) {
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result;
                if (type.equals("province"))
                    result = Utility.handleProvinceResponse(coolWeatherDB, response);
                else if (type.equals("city"))
                    result = Utility.handleCityResponse(coolWeatherDB, response, selectProvince.getId());
                else
                    result = Utility.handleCountyResponse(coolWeatherDB, response, selectCity.getId());

                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if (type.equals("province"))
                                queryProvinces();
                            else if (type.equals("city"))
                                queryCities();
                            else
                                queryCounties();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载...");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    public void closeProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    public void doBack(){
        if(currentLevel == LEVEL_COUNTY)
            queryCities();
        else if(currentLevel == LEVEL_CITY)
            queryProvinces();
        else
            mBackBtn.setVisibility(View.INVISIBLE);
    }

}
