package com.ddup4.autonav.data;

import android.text.TextUtils;

import com.ddup4.autonav.api.ApiInterface;
import com.google.gson.Gson;
import com.okandroid.boot.data.StorageManager;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by idonans on 2017/7/16.
 */

public class ApiManager {

    private static class InstanceHolder {
        private static final ApiManager sInstance = new ApiManager();
    }

    private static boolean sInit;

    public static ApiManager getInstance() {
        ApiManager instance = ApiManager.InstanceHolder.sInstance;
        sInit = true;
        return instance;
    }

    public static boolean isInit() {
        return sInit;
    }

    private static final String TAG = "ApiManager";

    private ApiInterface mDefaultApi;

    private static final String KEY_DEFAULT_HOST = "key.default_host";
    private String mHost;

    private ApiManager() {
        restore();

        refreshApiInterface();
    }

    public String getHost() {
        return mHost;
    }

    public void setHost(String host) {
        mHost = host;

        save();
        refreshApiInterface();
    }

    private void restore() {
        String defaultHost = StorageManager.getInstance().getSetting(KEY_DEFAULT_HOST);
        if (TextUtils.isEmpty(defaultHost)) {
            defaultHost = "192.168.0.1";
        }
        mHost = defaultHost;
    }

    private void save() {
        StorageManager.getInstance().setSetting(KEY_DEFAULT_HOST, mHost);
    }

    public ApiInterface getDefaultApi() {
        return mDefaultApi;
    }

    private void refreshApiInterface() {
        API_HOST.DEFAULT_BASE_URL = "http://" + mHost + "/autonav/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_HOST.DEFAULT_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        mDefaultApi = retrofit.create(ApiInterface.class);
    }

    public static class API_HOST {
        public static String DEFAULT_BASE_URL;
    }

}
