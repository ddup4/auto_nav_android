package com.ddup4.autonav.module.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.ddup4.autonav.app.BaseActivity;
import com.okandroid.boot.app.ext.dynamic.DynamicFragment;

public class MainActivity extends BaseActivity {

    private static final String EXTRA_LAST_LOCATION = "extra.LastLocation";
    private AMapLocation mLastLocation;

    public static Intent startIntent(Context context, AMapLocation aMapLocation) {
        Intent starter = new Intent(context, MainActivity.class);
        starter.putExtra(EXTRA_LAST_LOCATION, aMapLocation);
        return starter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mLastLocation = getIntent().getParcelableExtra(EXTRA_LAST_LOCATION);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected DynamicFragment createDynamicFragment() {
        return MainFragment.newInstance();
    }

    public AMapLocation getLastLocation() {
        return mLastLocation;
    }
}
