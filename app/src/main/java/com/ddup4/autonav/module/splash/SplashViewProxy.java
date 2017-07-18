package com.ddup4.autonav.module.splash;

import android.Manifest;

import com.amap.api.location.AMapLocation;
import com.ddup4.autonav.app.BaseViewProxy;
import com.okandroid.boot.app.ext.dynamic.DynamicViewData;
import com.okandroid.boot.thread.Threads;

/**
 * Created by idonans on 2017/2/3.
 */

public class SplashViewProxy extends BaseViewProxy<SplashView> {

    public SplashViewProxy(SplashView splashView) {
        super(splashView);
    }

    @Override
    protected DynamicViewData onInitBackground() {
        return new ViewData();
    }

    class ViewData implements DynamicViewData {
    }

    @Override
    public void onReady() {
        SplashView view = getView();
        if (view == null) {
            return;
        }

        Threads.postUi(new Runnable() {
            @Override
            public void run() {
                requestAllPermissions();
            }
        }, 2000L);
    }

    private void requestAllPermissions() {
        SplashView view = getView();
        if (view == null) {
            return;
        }

        String[] permissions = {Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.WAKE_LOCK};

        view.requestAllPermission(permissions);
    }

    public void onAllPermissionReady() {
        requestCurrentLocation();
    }

    private void requestCurrentLocation() {
        SplashView view = getView();
        if (view == null) {
            return;
        }

        view.requestLocation();
    }

    public void onCurrentLocationFound(AMapLocation aMapLocation) {
        directToMain(aMapLocation);
    }

    private void directToMain(AMapLocation aMapLocation) {
        SplashView view = getView();
        if (view == null) {
            return;
        }
        view.directToMain(aMapLocation);
    }

}
