package com.ddup4.autonav.module.splash;

import com.amap.api.location.AMapLocation;
import com.ddup4.autonav.app.BaseView;

/**
 * Created by idonans on 2017/2/3.
 */

public interface SplashView extends BaseView {

    boolean directToMain(AMapLocation aMapLocation);

    void requestAllPermission(String[] permissions);

    void requestLocation();

}
