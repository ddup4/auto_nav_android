package com.ddup4.autonav.data;

import android.text.TextUtils;

import com.ddup4.autonav.api.ApiInterface;
import com.ddup4.autonav.util.ToastUtil;
import com.google.gson.Gson;
import com.okandroid.boot.data.OkHttpManager;
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

        refreshApiInterface();

        save();
    }

    private void restore() {
        String defaultHost = StorageManager.getInstance().getSetting(KEY_DEFAULT_HOST);
        if (TextUtils.isEmpty(defaultHost)) {
            defaultHost = "http://192.168.1.1:8080/autonav/";
        }
        mHost = defaultHost;
    }

    private void resetDefaultHost() {
        ToastUtil.show("服务器地址已重置为默认地址");
        mHost = "http://192.168.1.1:8080/autonav/";
    }

    private void save() {
        StorageManager.getInstance().setSetting(KEY_DEFAULT_HOST, mHost);
    }

    public ApiInterface getDefaultApi() {
        return mDefaultApi;
    }

    private void refreshApiInterface() {
        try {
            API_HOST.DEFAULT_BASE_URL = mHost;
            Retrofit retrofit = new Retrofit.Builder()
                    .client(OkHttpManager.getInstance().getOkHttpClient())
                    .baseUrl(API_HOST.DEFAULT_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(new Gson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            mDefaultApi = retrofit.create(ApiInterface.class);
        } catch (Throwable e) {
            e.printStackTrace();

            resetDefaultHost();
            refreshApiInterface();
        }
    }

    public static class API_HOST {
        public static String DEFAULT_BASE_URL;
    }

}
