package com.ddup4.autonav.module.main;

import android.content.Context;
import android.content.Intent;

import com.ddup4.autonav.app.BaseActivity;
import com.okandroid.boot.app.ext.dynamic.DynamicFragment;

public class MainActivity extends BaseActivity {

    public static Intent startIntent(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        return starter;
    }

    @Override
    protected DynamicFragment createDynamicFragment() {
        return MainFragment.newInstance();
    }

}
