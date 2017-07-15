package com.ddup4.autonav.module.main;

import com.ddup4.autonav.app.BaseViewProxy;
import com.okandroid.boot.app.ext.dynamic.DynamicViewData;

/**
 * Created by idonans on 2017/2/3.
 */

public class MainViewProxy extends BaseViewProxy<MainView> {

    public MainViewProxy(MainView mainView) {
        super(mainView);
    }

    @Override
    protected DynamicViewData onInitBackground() {
        // TODO
        return new ViewData();
    }

    class ViewData implements DynamicViewData {
    }

    @Override
    public void onReady() {
        MainView view = getView();
        if (view == null) {
            return;
        }

        // TODO
    }

}
