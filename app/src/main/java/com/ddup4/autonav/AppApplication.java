package com.ddup4.autonav;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by idonans on 2017/7/15.
 */

public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }

        AppInit.init(this);
    }

}
