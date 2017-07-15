package com.ddup4.autonav.module.splash;

import android.view.View;
import android.view.Window;

import com.ddup4.autonav.app.BaseActivity;
import com.okandroid.boot.app.ext.dynamic.DynamicFragment;
import com.okandroid.boot.widget.ContentFullView;

public class SplashActivity extends BaseActivity {

    @Override
    protected void updateSystemUi() {
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                window.getDecorView().getSystemUiVisibility()
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    protected View createDefaultContentView() {
        return new ContentFullView(this);
    }

    @Override
    protected DynamicFragment createDynamicFragment() {
        return SplashFragment.newInstance();
    }

}