package com.ddup4.autonav.module.main;

import com.ddup4.autonav.api.entity.GpsInfo;
import com.ddup4.autonav.app.BaseView;

/**
 * Created by idonans on 2017/2/3.
 */

public interface MainView extends BaseView {

    void showReadGpsInfoConfirm(String phone);

    void showStartNavConfirm(GpsInfo gpsInfo);

}
