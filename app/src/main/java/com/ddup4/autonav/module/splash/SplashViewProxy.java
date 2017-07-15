package com.ddup4.autonav.module.splash;

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
                tryDirectToMain();
            }
        }, 2000L);
    }

    private void tryDirectToMain() {
        SplashView view = getView();
        if (view == null) {
            return;
        }
        view.directToMain();
    }

}
